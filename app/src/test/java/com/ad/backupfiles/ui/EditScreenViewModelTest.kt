package com.ad.backupfiles.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.ad.backupfiles.FakeRepository
import com.ad.backupfiles.MainDispatcherRule
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.data.entity.toDto
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.smb.api.SMBClientApi
import com.ad.backupfiles.ui.editScreen.EditScreenDestination
import com.ad.backupfiles.ui.editScreen.EditScreenViewModel
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.toSmbServerEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/*
 * @author : Arshdeep Dhillon
 * @created : 18-Dec-23
*/

@OptIn(ExperimentalCoroutinesApi::class)
class EditScreenViewModelTest {
    private lateinit var mockStateHandle: SavedStateHandle
    private lateinit var vmUnderTest: EditScreenViewModel
    private lateinit var fakeRepository: FakeRepository<SmbServerInfo>
    private val invalidServerData = SmbServerData()
    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = UnconfinedTestDispatcher(scheduler)
    private lateinit var serverData1: SmbServerData
    private lateinit var serverData2: SmbServerData

    @MockK
    private lateinit var mockSmbServerApi: SmbServerInfoApi

    @MockK
    private lateinit var mockSmbClientApi: SMBClientApi

    @get: Rule
    val dispatcherRule = MainDispatcherRule(testDispatcher)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        fakeRepository = FakeRepository()
        mockStateHandle = SavedStateHandle().apply { set(EditScreenDestination.argKey, 0L) }
        serverData1 = SmbServerData(
            serverAddress = "1.1.1.1",
            username = "someuser1",
            password = "somepassword1",
            sharedFolderName = "shared_folder1",
        )
        serverData2 = SmbServerData(
            serverAddress = "2.2.2.2",
            username = "someuser2",
            password = "somepassword2",
            sharedFolderName = "shared_folder2",
        )
    }

    private fun getVMState() = vmUnderTest.viewState
        .onStart { println("${Thread.currentThread().name}: Shared Flow Started") }
        .onEach { println("${Thread.currentThread().name}: Emitted Received: $it") }
        .onCompletion { println("${Thread.currentThread().name}: Shared Flow Completed") }

    @Test
    fun test_initial_state_is_returned_on_valid_data() = runTest {
        coEvery { mockSmbServerApi.getSmbServerStream(any()) } returns flowOf(serverData1.toSmbServerEntity())
        vmUnderTest = EditScreenViewModel(mockStateHandle, mockSmbServerApi, mockSmbClientApi)

        getVMState().test {
            val currentState = awaitItem()
            assertEquals(true, currentState.isValid)
            assertEquals(serverData1, currentState.currentUiData)
        }
    }

    @Test
    fun test_initial_state_is_returned_on_invalid_data() = runTest {
        coEvery { mockSmbServerApi.getSmbServerStream(any()) } returns flowOf(invalidServerData.toSmbServerEntity())
        vmUnderTest = EditScreenViewModel(mockStateHandle, mockSmbServerApi, mockSmbClientApi)

        getVMState().test {
            val currentState = awaitItem()
            assertEquals(false, currentState.isValid)
            assertEquals(invalidServerData, currentState.currentUiData)
        }
    }

    @Test
    fun test_state_updates_when_data_changes() = runTest {
        coEvery { mockSmbServerApi.getSmbServerStream(any()) } returns fakeRepository.flow
        vmUnderTest = EditScreenViewModel(mockStateHandle, mockSmbServerApi, mockSmbClientApi)
        fakeRepository.emit(serverData1.toSmbServerEntity())

        // Check the original data matches
        getVMState().test {
            val currentState = awaitItem()
            assertEquals(true, currentState.isValid)
            assertEquals(serverData1, currentState.currentUiData)
        }

        // 'Fake' user entered another valid data. Verify state updated and UI state is still valid
        vmUnderTest.updateUiState(serverData2)
        getVMState().test {
            val currentState = awaitItem()
            assertEquals(true, currentState.isValid)
            assertEquals(serverData2, currentState.currentUiData)
        }

        // 'Fake' user entered invalid data. Verify  state updated and UI state invalid
        vmUnderTest.updateUiState(invalidServerData)
        getVMState().test {
            val currentState = awaitItem()
            assertEquals(false, currentState.isValid)
            assertEquals(invalidServerData, currentState.currentUiData)
        }
    }

    @Test
    fun test_data_is_saved_only_on_valid_state() = runTest {
        coEvery { mockSmbServerApi.getSmbServerStream(any()) } returns fakeRepository.flow
        coEvery { mockSmbServerApi.upsertSmbServer(any()) } returns Unit
        vmUnderTest = EditScreenViewModel(mockStateHandle, mockSmbServerApi, mockSmbClientApi)
        fakeRepository.emit(serverData1.toSmbServerEntity())

        getVMState().test {
            vmUnderTest.saveChanges()
            val currentState = awaitItem()
            assertEquals(true, currentState.isValid)

            vmUnderTest.updateUiState(invalidServerData)
            vmUnderTest.saveChanges()
            assertEquals(false, awaitItem().isValid)
        }
        // Verify upsertSmbServer() was only called once on any argument
        coVerify(exactly = 1) { mockSmbServerApi.upsertSmbServer(any()) }

        // Verify upsertSmbServer() was never called with invalidServerData argument
        coVerify(exactly = 0) { mockSmbServerApi.upsertSmbServer(invalidServerData) }
    }

    @Test
    fun test_resaving_data_should_not_change_state() = runTest {
        coEvery { mockSmbServerApi.getSmbServerStream(any()) } returns fakeRepository.flow
        coEvery { mockSmbServerApi.upsertSmbServer(any()) } returns Unit
        vmUnderTest = EditScreenViewModel(mockStateHandle, mockSmbServerApi, mockSmbClientApi)
        fakeRepository.emit(serverData1.toSmbServerEntity())

        getVMState().test {
            vmUnderTest.saveChanges()
            vmUnderTest.saveChanges()
            val currentState = awaitItem()
            assertEquals(true, currentState.isValid)
            assertEquals(serverData1, currentState.currentUiData)
        }
        // Verify upsertSmbServer() was only called twice on any argument.
        coVerify(exactly = 2) { mockSmbServerApi.upsertSmbServer(any()) }
        coVerify(exactly = 2) { mockSmbServerApi.upsertSmbServer(serverData1) }

        vmUnderTest.updateUiState(invalidServerData)
        getVMState().test {
            vmUnderTest.saveChanges()
            vmUnderTest.saveChanges()
            val currentState = awaitItem()
            assertEquals(false, currentState.isValid)
            assertEquals(invalidServerData, currentState.currentUiData)
        }
        // Verify upsertSmbServer() was never called with invalidServerData argument
        coVerify(exactly = 0) { mockSmbServerApi.upsertSmbServer(invalidServerData) }
    }

    @Test
    fun test_true_flag_returned_only_when_connection_established_successfully() = runTest {
        coEvery { mockSmbServerApi.getSmbServerStream(any()) } returns fakeRepository.flow
        coEvery { mockSmbClientApi.canConnect(serverData1.toSmbServerEntity().toDto()) } returns true
        coEvery { mockSmbClientApi.canConnect(invalidServerData.toSmbServerEntity().toDto()) } returns false
        vmUnderTest = EditScreenViewModel(mockStateHandle, mockSmbServerApi, mockSmbClientApi)
        fakeRepository.emit(serverData1.toSmbServerEntity())

        assert(vmUnderTest.canConnectToServer())
        vmUnderTest.updateUiState(invalidServerData)
        assertFalse(vmUnderTest.canConnectToServer())

        coVerify(exactly = 1) { mockSmbClientApi.canConnect(invalidServerData.toSmbServerEntity().toDto()) }
        coVerify(exactly = 1) { mockSmbClientApi.canConnect(serverData1.toSmbServerEntity().toDto()) }
        coVerify(exactly = 2) { mockSmbClientApi.canConnect(any()) }
    }
}
