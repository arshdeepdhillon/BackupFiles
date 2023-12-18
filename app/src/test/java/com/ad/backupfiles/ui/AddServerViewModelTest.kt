package com.ad.backupfiles.ui

import app.cash.turbine.test
import com.ad.backupfiles.TestDispatcherRule
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.ui.addServerScreen.AddServerViewModel
import com.ad.backupfiles.ui.utils.SMBServerUiState
import com.ad.backupfiles.ui.utils.SmbServerData
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * @created : 16-Dec-23
*/

@OptIn(ExperimentalCoroutinesApi::class)
class AddServerViewModelTest {
    @MockK
    private lateinit var mockSmbServerApi: SmbServerInfoApi

    private lateinit var viewModel: AddServerViewModel
    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    @get: Rule
    val dispatcherRule = TestDispatcherRule(testDispatcher)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    private fun getVMState() = viewModel.viewState

    @Test
    fun test_default_view_state() = runTest {
        viewModel = AddServerViewModel(mockSmbServerApi)
        val expected = SMBServerUiState()

        getVMState().test {
            val actualState: SMBServerUiState = awaitItem()
            assertEquals(expected.isValid, actualState.isValid)
            assertEquals(expected.currentUiData, actualState.currentUiData)
        }
    }

    @Test
    fun test_data_required_for_valid_state() = runTest {
        viewModel = AddServerViewModel(mockSmbServerApi)
        val expected = SmbServerData(serverAddress = "192.168.10.10")

        viewModel.updateUiState(expected)
        getVMState().test {
            val actualViewState: SMBServerUiState = awaitItem()
            assert(actualViewState.isValid)
            assertEquals(expected, actualViewState.currentUiData)
        }
    }

    @Test
    fun test_state_updates_on_data_change() = runTest {
        viewModel = AddServerViewModel(mockSmbServerApi)
        val expected1 = SmbServerData(serverAddress = "192.168.10.10")
        val expected2 = SmbServerData(serverAddress = "192.168.100.100")
        val expected3 = SmbServerData(serverAddress = "")

        viewModel.updateUiState(expected1)
        getVMState().test {
            val actualViewState: SMBServerUiState = awaitItem()
            assert(actualViewState.isValid)
            assertEquals(expected1, actualViewState.currentUiData)
        }

        viewModel.updateUiState(expected2)
        getVMState().test {
            val actualViewState: SMBServerUiState = awaitItem()
            assert(actualViewState.isValid)
            assertEquals(expected2, actualViewState.currentUiData)
        }

        viewModel.updateUiState(expected3)
        getVMState().test {
            val actualViewState: SMBServerUiState = awaitItem()
            assertFalse(actualViewState.isValid)
            assertEquals(expected3, actualViewState.currentUiData)
        }
    }

    @Test
    fun test_state_not_saved_on_invalid_state() = runTest {
        viewModel = AddServerViewModel(mockSmbServerApi)
        val expected = SmbServerData(serverAddress = "")

        coEvery { mockSmbServerApi.upsertSmbServer(any()) } returns mockk()
        viewModel.updateUiState(expected)
        viewModel.save()
        verify {
            mockSmbServerApi wasNot called
        }
    }

    @Test
    fun test_state_saves_on_valid_data_change() = runTest {
        viewModel = AddServerViewModel(mockSmbServerApi)
        val expected1 = SmbServerData(serverAddress = "192.168.10.101")
        val expected2 = SmbServerData(serverAddress = "192.168.100.102")

        coEvery { mockSmbServerApi.upsertSmbServer(any()) } returns mockk()

        viewModel.updateUiState(expected1)
        viewModel.save()
        viewModel.updateUiState(expected2)
        viewModel.save()
        coVerify {
            mockSmbServerApi.upsertSmbServer(expected1)
            mockSmbServerApi.upsertSmbServer(expected2)
        }
    }
}
