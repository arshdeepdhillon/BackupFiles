package com.ad.backupfiles.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.ad.backupfiles.FakeRepository
import com.ad.backupfiles.MainDispatcherRule
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.ui.detailServerScreen.DetailScreenDestination
import com.ad.backupfiles.ui.detailServerScreen.DetailScreenUiState
import com.ad.backupfiles.ui.detailServerScreen.DetailServerViewModel
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.toSmbServerEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/*
 * @author : Arshdeep Dhillon
 * @created : 17-Dec-23
*/

@OptIn(ExperimentalCoroutinesApi::class)
class DetailServerViewModelTest {
    private lateinit var mockStateHandle: SavedStateHandle
    private lateinit var vmUnderTest: DetailServerViewModel
    private lateinit var fakeRepository: FakeRepository<SmbServerInfo>
    private val defaultServerData = SmbServerData()
    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = UnconfinedTestDispatcher(scheduler)
    private lateinit var serverData1: SmbServerData
    private lateinit var serverData2: SmbServerData
    private lateinit var expected2: DetailScreenUiState

    @MockK
    private lateinit var mockSmbServerApi: SmbServerInfoApi

    @get: Rule
    val dispatcherRule = MainDispatcherRule(testDispatcher)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        fakeRepository = FakeRepository()
        mockStateHandle = SavedStateHandle().apply { set(DetailScreenDestination.argKey, 0L) }
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
        expected2 = createUiData(serverData2)
    }

    private fun createUiData(smbServerData: SmbServerData): DetailScreenUiState =
        DetailScreenUiState(smbServerData)

    private fun getVMState() = vmUnderTest.viewState
        .onStart { println("${Thread.currentThread().name}: Shared Flow Started") }
        .onEach { println("${Thread.currentThread().name}: Emitted Received: $it") }
        .onCompletion { println("${Thread.currentThread().name}: Shared Flow Completed") }

    @Test
    fun test_initial_state_is_returned() = runTest {
        vmUnderTest = DetailServerViewModel(mockStateHandle, mockSmbServerApi)
        every { mockSmbServerApi.getSmbServerStream(any()) } returns fakeRepository.flow

        getVMState().test {
            assertEquals(defaultServerData, awaitItem().serverInfo)
        }
    }

    @Test
    fun test_latest_smb_changes_are_updated_in_viewstate() = runTest {
        every { mockSmbServerApi.getSmbServerStream(any()) } returns fakeRepository.flow
        vmUnderTest = DetailServerViewModel(mockStateHandle, mockSmbServerApi)

        getVMState().test {
            assertEquals(defaultServerData, awaitItem().serverInfo)
        }

        fakeRepository.emit(serverData1.toSmbServerEntity()) // StateIn will conflate to latest emitted value; serverData2
        fakeRepository.emit(serverData2.toSmbServerEntity())
        getVMState().test {
            val actualServerData2 = awaitItem()
            assertEquals(expected2, actualServerData2)
            assertEquals(expected2.serverInfo, actualServerData2.serverInfo)
        }

        verify(exactly = 1) {
            mockSmbServerApi.getSmbServerStream(any())
        }
    }

    @Test
    fun test_correct_viewstate_is_used_for_deletion() = runTest {
        every { mockSmbServerApi.getSmbServerStream(any()) } returns fakeRepository.flow
        coEvery { mockSmbServerApi.deleteSmbServer(any()) } returns Unit
        vmUnderTest = DetailServerViewModel(mockStateHandle, mockSmbServerApi)

        getVMState().test {
            assertEquals(defaultServerData, awaitItem().serverInfo)
        }

        fakeRepository.emit(serverData1.toSmbServerEntity())
        vmUnderTest.deleteSmbServer()
        coVerify(exactly = 1) {
            mockSmbServerApi.deleteSmbServer(serverData1.toSmbServerEntity())
        }

        fakeRepository.emit(serverData2.toSmbServerEntity())
        getVMState().test {
            val actualServerData2 = awaitItem()
            assertEquals(expected2, actualServerData2)
            assertEquals(expected2.serverInfo, actualServerData2.serverInfo)
        }
        vmUnderTest.deleteSmbServer()
        coVerify(exactly = 1) {
            mockSmbServerApi.deleteSmbServer(serverData2.toSmbServerEntity())
        }
    }
}
