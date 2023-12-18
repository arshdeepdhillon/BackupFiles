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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
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
    private lateinit var viewModel: DetailServerViewModel
    private lateinit var testRepository: TestRepository<SmbServerInfo>
    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @get: Rule
    val dispatcherRule = TestDispatcherRule(testDispatcher)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        testRepository = TestRepository()
        mockStateHandle = SavedStateHandle().apply { set(DetailScreenDestination.argKey, 0L) }
        viewModel = DetailServerViewModel(mockStateHandle, mockSmbServerApi)
        every { mockSmbServerApi.getSmbServerStream(any()) } returns testRepository.flow
    }

    private fun createUiData(smbServerData: SmbServerData): DetailScreenUiState =
        DetailScreenUiState(smbServerData)

    private fun getVMState() = viewModel.viewState

    @Test
    fun test_nothing_is_returned_on_nonexistent_smb_id() = runTest {
        val expected = SmbServerData()
        val viewState = getVMState()
            .onCompletion { println("SHARED FLOW COMPLETED") }
            .onEach { println("onEach: $it") }
            .stateIn(
                scope = backgroundScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = DetailScreenUiState(),
            )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewState.collect {
                println("Collected: $it")
            }
        }

        viewState.test {
            // First value emitted will be the 'initialValue' of viewState in DetailServerViewModel
            assertEquals(expected, awaitItem().serverInfo)
        }
    }

    @Ignore(value = "StateIn not emitting call values during test")
    @Test
    fun test_correct_smb_info() = runTest {
        val expected = SmbServerData()
        val expected1 = createUiData(
            SmbServerData(
                serverAddress = "1.1.1.1",
                username = "someuser1",
                password = "somepassword1",
                sharedFolderName = "shared_folder1",
            ),
        )
        val expected2 = createUiData(
            SmbServerData(
                serverAddress = "2.2.2.2",
                username = "someuser2",
                password = "somepassword2",
                sharedFolderName = "shared_folder2",
            ),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            getVMState().collect()
        }

        testRepository.emit(expected1.serverInfo.toSmbServerEntity())
        getVMState().test {
            // First value emitted will be the 'initialValue' of viewState in DetailServerViewModel
            assertEquals(expected, awaitItem().serverInfo)
            assertEquals(expected1.serverInfo, awaitItem().serverInfo)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
