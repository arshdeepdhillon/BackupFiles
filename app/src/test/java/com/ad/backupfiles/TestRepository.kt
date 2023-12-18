package com.ad.backupfiles

import kotlinx.coroutines.flow.MutableSharedFlow

/*
 * @author : Arshdeep Dhillon
 * @created : 16-Dec-23
*/

/**
 *  Allows the ability to control when the StateFlow emits.
 *  @see <a href="https://medium.com/@erik.r.yverling/unit-testing-ui-state-in-android-viewmodels-b19973311900">Testing ViewModels using StateFlow</a>
 */
class TestRepository<T> {
    val flow = MutableSharedFlow<T>()
    suspend fun emit(value: T) {
        println("${Thread.currentThread().name}: Emitting $value")
        flow.emit(value)
    }
}
