package com.ad.backupfiles.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.ad.backupfiles.TestDispatcherRule
import com.ad.backupfiles.TestRepository
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.ui.detailServerScreen.DetailScreenDestination
import com.ad.backupfiles.ui.detailServerScreen.DetailScreenUiState
import com.ad.backupfiles.ui.detailServerScreen.DetailServerViewModel
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.toSmbServerEntity
import io.mockk.MockKAnnotations
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
    @MockK
    private lateinit var mockSmbServerApi: SmbServerInfoApi

    private lateinit var mockStateHandle: SavedStateHandle
    private lateinit var vmUnderTest: DetailServerViewModel
    private lateinit var testRepository: TestRepository<SmbServerInfo>
    private val defaultServerData = SmbServerData()
    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = UnconfinedTestDispatcher(scheduler)

    @get: Rule
    val dispatcherRule = TestDispatcherRule(testDispatcher)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        testRepository = TestRepository()
        mockStateHandle = SavedStateHandle().apply { set(DetailScreenDestination.argKey, 0L) }
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
        every { mockSmbServerApi.getSmbServerStream(any()) } returns testRepository.flow

        getVMState().test {
            assertEquals(defaultServerData, awaitItem().serverInfo)
        }
    }

    @Test
    fun test_latest_smb_changes_are_updated_in_viewstate() = runTest {
        val serverData1 = SmbServerData(
            serverAddress = "1.1.1.1",
            username = "someuser1",
            password = "somepassword1",
            sharedFolderName = "shared_folder1",
        )
        val serverData2 = SmbServerData(
            serverAddress = "2.2.2.2",
            username = "someuser2",
            password = "somepassword2",
            sharedFolderName = "shared_folder2",
        )
        val expected2 = createUiData(serverData2)
        every { mockSmbServerApi.getSmbServerStream(any()) } returns testRepository.flow
        vmUnderTest = DetailServerViewModel(mockStateHandle, mockSmbServerApi)

        getVMState().test {
            assertEquals(defaultServerData, awaitItem().serverInfo)
        }

        testRepository.emit(serverData1.toSmbServerEntity()) // StateIn will conflate to latest emitted value; serverData2
        testRepository.emit(serverData2.toSmbServerEntity())
        getVMState().test {
            val actualServerData2 = awaitItem()
            assertEquals(expected2, actualServerData2)
            assertEquals(expected2.serverInfo, actualServerData2.serverInfo)
        }

        verify(exactly = 1) {
            mockSmbServerApi.getSmbServerStream(any())
        }
    }
}
