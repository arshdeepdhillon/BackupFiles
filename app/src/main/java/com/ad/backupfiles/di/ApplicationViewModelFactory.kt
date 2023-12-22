package com.ad.backupfiles.di

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ad.backupfiles.AppEntryPoint
import com.ad.backupfiles.ui.addServerScreen.AddServerViewModel
import com.ad.backupfiles.ui.detailServerScreen.DetailServerViewModel
import com.ad.backupfiles.ui.editScreen.EditScreenViewModel
import com.ad.backupfiles.ui.homeScreen.HomeViewModel
import com.ad.backupfiles.ui.savedDirectoriesScreen.SavedDirectoriesViewModel
import com.ad.backupfiles.ui.savedDirectoriesScreen.SyncTrackerViewModel

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

object ApplicationViewModelFactory {
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
            SavedDirectoriesViewModel(
                this.createSavedStateHandle(),
                AppEntryPoint.appModule,
            )
        }
        initializer {
            SyncTrackerViewModel()
        }
    }
}
