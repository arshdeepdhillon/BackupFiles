package com.ad.backupfiles.ui.savedDirectoriesScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ad.backupfiles.data.entity.DirectoryDto

/**
 * ViewModel responsible for tracking and managing the synchronization state of selected directories.
 *
 * This ViewModel allows users to select or deselect directories for synchronization and provides
 * information about the selected directories when needed.
 *
 * **Note: Caller must clear the selection state by calling [onClearSelectionState]**
 */
class SyncTrackerViewModel : ViewModel() {

    private val TAG = SyncTrackerViewModel::class.java.simpleName

    /** A mutable set containing the Ids of selected directories.*/
    private var selectedDirectoryIds = mutableListOf<Long>()

    /**
     * Handles the selection or deselection of a directory item.
     *
     * @param item A Pair representing selection status of the folder
     * @property Pair.first  folder is selected if true, otherwise it's unselected
     * @property Pair.second [DirectoryDto] contains details about the folder.
     */
    fun onDirectorySelected(item: Pair<Boolean, DirectoryDto>) {
        if (item.first) {
            selectedDirectoryIds.add(item.second.dirId)
        } else {
            selectedDirectoryIds.remove(item.second.dirId)
        }
    }

    /**
     * Retrieves the list of directory IDs that are selected for synchronization.
     *
     * @return A MutableList containing the IDs of selected directories.
     */
    fun directoriesToSync(): MutableList<Long> {
        return selectedDirectoryIds
    }

    /**
     * Clears the selection state, removing all selected directories.
     *
     * This function is responsible for clearing the selection state of the ViewModel,
     * removing all directories that have been selected for synchronization. It should be
     * called when the user performs an action to clear or reset the selection state.
     */
    fun onClearSelectionState() {
        // Do appropriate cleanup related to selected directories here.
        selectedDirectoryIds.clear()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: selectedDirectoryIds: ${selectedDirectoryIds.size}")
    }
}
