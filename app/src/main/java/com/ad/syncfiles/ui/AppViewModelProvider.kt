package com.ad.syncfiles.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ad.syncfiles.SyncFilesApplicationEntryPoint
import com.ad.syncfiles.ui.home.HomeViewModel
import com.ad.syncfiles.ui.smbServer.AddScreenViewModel
import com.ad.syncfiles.ui.smbServer.DetailScreenViewModel
import com.ad.syncfiles.ui.smbServer.EditScreenViewModel
import com.ad.syncfiles.ui.smbServer.SharedContentScreenViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {

        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(syncFilesApplication().container.smbServerRepository)
        }

        // Initializer for AddScreenViewModel
        initializer {
            AddScreenViewModel(
                syncFilesApplication().container.smbServerRepository
            )
        }
        // Initializer for DetailScreenViewModel
        initializer {
            DetailScreenViewModel(
                this.createSavedStateHandle(),
                syncFilesApplication().container.smbServerRepository
            )
        }
        // Initializer for EditScreenViewModel
        initializer {
            EditScreenViewModel(
                this.createSavedStateHandle(),
                syncFilesApplication().container.smbServerRepository
            )
        }
        // Initializer for SharedContentScreenViewModel
        initializer {
            SharedContentScreenViewModel(
                this.createSavedStateHandle(),
                syncFilesApplication().container.smbServerRepository,
                syncFilesApplication().container.saveDirectoryRepository,
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