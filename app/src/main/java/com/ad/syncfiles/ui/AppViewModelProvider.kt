package com.ad.syncfiles.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ad.syncfiles.SyncFilesApplicationEntryPoint
import com.ad.syncfiles.ui.home.HomeViewModel
import com.ad.syncfiles.ui.sharedDevice.SDEntryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {

        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(syncFilesApplication().container.smbServerRepository)
        }

        // Initializer for SDEntryViewModel
        initializer {
            SDEntryViewModel(
                syncFilesApplication().container.smbServerRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [SyncFilesApplicationEntryPoint].
 */
fun CreationExtras.syncFilesApplication(): SyncFilesApplicationEntryPoint =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as SyncFilesApplicationEntryPoint)