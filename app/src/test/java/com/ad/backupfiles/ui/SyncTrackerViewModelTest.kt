package com.ad.backupfiles.ui

import android.os.Build
import androidx.annotation.RequiresApi
import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.ui.savedDirectoriesScreen.SyncTrackerViewModel
import io.mockk.MockKAnnotations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.time.Instant

/*
 * @author : Arshdeep Dhillon
 * @created : 18-Dec-23
*/

@OptIn(ExperimentalCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.O)
class SyncTrackerViewModelTest {
    private lateinit var vmUnderTest: SyncTrackerViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    companion object {
        private lateinit var directories: MutableList<Pair<Boolean, DirectoryDto>>

        @JvmStatic
        @BeforeClass
        fun initData() {
            directories = (1..100).map {
                Pair(
                    (it and 1) == 0,
                    DirectoryDto(
                        dirId = it.toLong(),
                        dirPath = "",
                        dirName = "test_directory_name_$it",
                        smbServerId = it.toLong(),
                        lastSynced = Instant.now().epochSecond,
                    ),
                )
            }.toCollection(ArrayList())
        }
    }

    @Test
    fun test_initial_state_is_returned() = runTest {
        vmUnderTest = SyncTrackerViewModel()
        assert(vmUnderTest.directoriesToSync().isEmpty())
    }

    @Test
    fun test_selected_directories_should_only_sync() = runTest {
        vmUnderTest = SyncTrackerViewModel()
        val selectedDirs = directories.filter { it.first }

        selectedDirs.forEach { vmUnderTest.onDirectorySelected(it) }
        assertEquals(selectedDirs.size, vmUnderTest.directoriesToSync().size)
        assertEquals(selectedDirs.map { it.second.dirId }, vmUnderTest.directoriesToSync())
    }

    @Test
    fun test_unselected_directories_do_not_sync() = runTest {
        vmUnderTest = SyncTrackerViewModel()
        val selectedDirs = directories.filter { it.first }
        val unselectedDirs = directories.filter { !it.first }

        directories.forEach { vmUnderTest.onDirectorySelected(it) }
        assertEquals(selectedDirs.size, vmUnderTest.directoriesToSync().size)
        assertEquals(selectedDirs.map { it.second.dirId }, vmUnderTest.directoriesToSync())
        assertNotEquals(unselectedDirs.map { it.second.dirId }, vmUnderTest.directoriesToSync())
    }

    @Test
    fun test_selection_state_is_cleared_when_manually_triggered() = runTest {
        vmUnderTest = SyncTrackerViewModel()

        directories.forEach { vmUnderTest.onDirectorySelected(it) }
        vmUnderTest.onClearSelectionState()
        assertEquals(0, vmUnderTest.directoriesToSync().size)
    }
}
