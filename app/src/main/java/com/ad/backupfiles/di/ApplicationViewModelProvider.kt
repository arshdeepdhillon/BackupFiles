package com.ad.backupfiles.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ad.backupfiles.AppEntryPoint
import com.ad.backupfiles.ui.addServerScreen.AddServerViewModel
import com.ad.backupfiles.ui.detailServerScreen.DetailServerViewModel
import com.ad.backupfiles.ui.editScreen.EditScreenViewModel
import com.ad.backupfiles.ui.homeScreen.HomeViewModel
import com.ad.backupfiles.ui.savedDirectoriesScreen.SavedDirectoriesScreenViewModel

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

object AppViewModelFactory {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(AppEntryPoint.appModule.smbServerApi)
        }
        initializer {
            AddServerViewModel(
                AppEntryPoint.appModule.smbServerApi,
            )
        }
        initializer {
            DetailServerViewModel(
                this.createSavedStateHandle(),
                AppEntryPoint.appModule.smbServerApi,
            )
        }
        initializer {
            EditScreenViewModel(
                this.createSavedStateHandle(),
                AppEntryPoint.appModule.smbServerApi,
                AppEntryPoint.appModule.smbClientApi,
            )
        }
        initializer {
            SavedDirectoriesScreenViewModel(
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
