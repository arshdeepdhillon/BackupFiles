package com.ad.backupfiles.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ad.backupfiles.AppEntryPoint
import com.ad.backupfiles.ui.addSmbScreen.AddServerViewModel
import com.ad.backupfiles.ui.homeScreen.HomeViewModel
import com.ad.backupfiles.ui.smbServer.DetailScreenViewModel
import com.ad.backupfiles.ui.smbServer.EditScreenViewModel
import com.ad.backupfiles.ui.smbServer.SharedContentScreenViewModel

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

object AppViewModelFactory {
    val Factory = viewModelFactory {

        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(AppEntryPoint.appModule.smbServerApi)
        }

        // Initializer for AddScreenViewModel
        initializer {
            AddServerViewModel(
                AppEntryPoint.appModule.smbServerApi,
            )
        }
        // Initializer for DetailScreenViewModel
        initializer {
            DetailScreenViewModel(
                this.createSavedStateHandle(),
                AppEntryPoint.appModule,
            )
        }
        // Initializer for EditScreenViewModel
        initializer {
            EditScreenViewModel(
                this.createSavedStateHandle(),
                AppEntryPoint.appModule,
            )
        }
        // Initializer for SharedContentScreenViewModel
        initializer {
            SharedContentScreenViewModel(
                this.createSavedStateHandle(),
                AppEntryPoint.appModule,
            )
        }
    }
}

/**
 * Extension function to query for [Application] object and returns an instance of [AppEntryPoint].
 */
fun CreationExtras.backupFilesApplication(): AppEntryPoint =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as AppEntryPoint)
