package com.ad.backupfiles.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/*
 * @author : Arshdeep Dhillon
 * @created : 10-Dec-23
*/

/**
 * Mocks the main dispatcher with a [TestDispatcher].
 *
 * **Note: Use this TestWatcher in instrumentation tests.**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(name = "MyTestDispatcher"),
) : TestWatcher() {
    override fun starting(description: Description) {
        // During testing, mock the main dispatcher using this test dispatcher
        // which will be used throughout the application
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
