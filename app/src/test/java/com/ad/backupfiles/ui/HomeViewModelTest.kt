package com.ad.backupfiles.ui

import app.cash.turbine.test
import com.ad.backupfiles.FakeRepository
import com.ad.backupfiles.MainDispatcherRule
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.ui.homeScreen.HomeViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
 * @created : 16-Dec-23
*/

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var vmUnderTest: HomeViewModel
    private lateinit var fakeRepository: FakeRepository<List<SmbServerInfo>>
    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    @MockK
    private lateinit var mockSmbServerApi: SmbServerInfoApi

    @get: Rule
    val dispatcherRule = MainDispatcherRule(testDispatcher)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        fakeRepository = FakeRepository()
    }

    private fun getVMState() = vmUnderTest.viewState
        .onStart { println("${Thread.currentThread().name}: Shared Flow Started") }
        .onEach { println("${Thread.currentThread().name}: Emitted Received: $it") }
        .onCompletion { println("${Thread.currentThread().name}: Shared Flow Completed") }

    @Test
    fun test_initial_state_is_returned() = runTest {
        vmUnderTest = HomeViewModel(mockSmbServerApi)
        every { mockSmbServerApi.getAllSmbServersAscStream() } returns fakeRepository.flow

        getVMState().test {
            // First value emitted will be the 'initialValue' of this homeViewState
            assert(awaitItem().sharedServers.isEmpty())
        }
    }

    @Test
    fun test_smb_changes_are_correctly_shown() = runTest {
        val savedSmbs: List<SmbServerInfo> = (1L..5).map { i ->
            SmbServerInfo(
                smbServerId = i,
                serverAddress = "192.168.100.$i",
                username = "test_username_$i",
                password = "testpassword_$i",
                sharedFolderName = "share_folder_$i",
            )
        }.toList()
        every { mockSmbServerApi.getAllSmbServersAscStream() } returns fakeRepository.flow
        vmUnderTest = HomeViewModel(mockSmbServerApi)

        getVMState().test {
            // First value emitted will be the 'initialValue' of this homeViewState
            assert(awaitItem().sharedServers.isEmpty())
        }

        // Verify the original smbs returned are correct
        fakeRepository.emit(savedSmbs)
        getVMState().test {
            val actualSmbs = awaitItem().sharedServers
            assertEquals(savedSmbs.size, actualSmbs.size)
            assertEquals(savedSmbs, actualSmbs)
        }

        // Modify the original smbs and verify newly returned smbs match
        val modifiedSmbs = savedSmbs.map {
            it.copy(
                serverAddress = "10.10.10",
                sharedFolderName = "new" + it.sharedFolderName,
                password = "newpassword",
                username = "newusername",
            )
        }
        fakeRepository.emit(modifiedSmbs)
        getVMState().test {
            val actualSmbs = awaitItem().sharedServers
            assertEquals(modifiedSmbs.size, actualSmbs.size)
            assertEquals(modifiedSmbs, actualSmbs)
        }
    }
}
