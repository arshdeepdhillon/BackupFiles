package com.ad.backupfiles.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ad.backupfiles.BackupFilesApplicationEntryPoint
import com.ad.backupfiles.ui.home.HomeViewModel
import com.ad.backupfiles.ui.smbServer.AddScreenViewModel
import com.ad.backupfiles.ui.smbServer.DetailScreenViewModel
import com.ad.backupfiles.ui.smbServer.EditScreenViewModel
import com.ad.backupfiles.ui.smbServer.SharedContentScreenViewModel

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

object AppViewModelProvider {
    val Factory = viewModelFactory {

        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(backupFilesApplication().container.smbServerRepo)
        }

        // Initializer for AddScreenViewModel
        initializer {
            AddScreenViewModel(
                backupFilesApplication().container.smbServerRepo
            )
        }
        // Initializer for DetailScreenViewModel
        initializer {
            DetailScreenViewModel(
                this.createSavedStateHandle(),
                backupFilesApplication().container.smbServerRepo
            )
        }
        // Initializer for EditScreenViewModel
        initializer {
            EditScreenViewModel(
                this.createSavedStateHandle(),
                backupFilesApplication().container.smbServerRepo
            )
        }
        // Initializer for SharedContentScreenViewModel
        initializer {
            SharedContentScreenViewModel(
                this.createSavedStateHandle(),
                backupFilesApplication().container.directoryRepo,
                backupFilesApplication().applicationContext
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [BackupFilesApplicationEntryPoint].
 */
fun CreationExtras.backupFilesApplication(): BackupFilesApplicationEntryPoint =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as BackupFilesApplicationEntryPoint)