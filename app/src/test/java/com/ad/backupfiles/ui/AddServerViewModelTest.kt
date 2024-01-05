package com.ad.backupfiles.ui

import app.cash.turbine.test
import com.ad.backupfiles.MainDispatcherRule
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
 * @created : 16-Dec-23
*/

@OptIn(ExperimentalCoroutinesApi::class)
class AddServerViewModelTest {
    private lateinit var vmUnderTest: AddServerViewModel
    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    @MockK
    private lateinit var mockSmbServerApi: SmbServerInfoApi

    @get: Rule
    val dispatcherRule = MainDispatcherRule(testDispatcher)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        vmUnderTest = AddServerViewModel(mockSmbServerApi)
    }

    private fun getVMState() = vmUnderTest.viewState
        .onStart { println("${Thread.currentThread().name}: Shared Flow Started") }
        .onEach { println("${Thread.currentThread().name}: Emitted Received: $it") }
        .onCompletion { println("${Thread.currentThread().name}: Shared Flow Completed") }

    @Test
    fun test_initial_state_is_returned() = runTest {
        val expected = SMBServerUiState()

        getVMState().test {
            val actualState: SMBServerUiState = awaitItem()
            assertEquals(expected.isValid, actualState.isValid)
            assertEquals(expected.currentUiData, actualState.currentUiData)
        }
    }

    @Test
    fun test_data_required_for_valid_state() = runTest {
        val expected = SmbServerData(serverAddress = "192.168.10.10")

        vmUnderTest.updateUiState(expected)
        getVMState().test {
            val actualViewState: SMBServerUiState = awaitItem()
            assert(actualViewState.isValid)
            assertEquals(expected, actualViewState.currentUiData)
        }
    }

    @Test
    fun test_state_updates_on_data_change() = runTest {
        val expected1 = SmbServerData(serverAddress = "192.168.10.10")
        val expected2 = SmbServerData(serverAddress = "192.168.100.100")
        val expected3 = SmbServerData(serverAddress = "")

        vmUnderTest.updateUiState(expected1)
        getVMState().test {
            val actualViewState: SMBServerUiState = awaitItem()
            assert(actualViewState.isValid)
            assertEquals(expected1, actualViewState.currentUiData)
        }

        vmUnderTest.updateUiState(expected2)
        getVMState().test {
            val actualViewState: SMBServerUiState = awaitItem()
            assert(actualViewState.isValid)
            assertEquals(expected2, actualViewState.currentUiData)
        }

        vmUnderTest.updateUiState(expected3)
        getVMState().test {
            val actualViewState: SMBServerUiState = awaitItem()
            assertFalse(actualViewState.isValid)
            assertEquals(expected3, actualViewState.currentUiData)
        }
    }

    @Test
    fun test_state_not_saved_on_invalid_state() = runTest {
        val expected = SmbServerData(serverAddress = "")

        coEvery { mockSmbServerApi.upsertSmbServer(any()) } returns mockk()
        vmUnderTest.updateUiState(expected)
        vmUnderTest.save()
        verify {
            mockSmbServerApi wasNot called
        }
    }

    @Test
    fun test_state_saves_on_valid_data_change() = runTest {
        val expected1 = SmbServerData(serverAddress = "192.168.10.101")
        val expected2 = SmbServerData(serverAddress = "192.168.100.102")

        coEvery { mockSmbServerApi.upsertSmbServer(any()) } returns mockk()

        vmUnderTest.updateUiState(expected1)
        vmUnderTest.save()
        vmUnderTest.updateUiState(expected2)
        vmUnderTest.save()
        coVerify {
            mockSmbServerApi.upsertSmbServer(expected1)
            mockSmbServerApi.upsertSmbServer(expected2)
        }
    }
}
