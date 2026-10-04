package com.example.ui

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DoubleTapAction
import com.example.data.ScreenType
import com.example.data.WallpaperConfig
import com.example.data.WallpaperPreferencesRepository
import com.example.data.local.SavedVideoEntity
import com.example.data.repository.VideoLibraryRepository
import com.example.video.VideoPresetManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WallpaperPreferencesRepository.getInstance(application)
    private val presetManager = VideoPresetManager.getInstance(application)
    private val videoLibraryRepository = VideoLibraryRepository.getInstance(application)

    val configState: StateFlow<WallpaperConfig> = repository.configFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.getConfig()
        )

    val libraryVideos: StateFlow<List<SavedVideoEntity>> = videoLibraryRepository.allVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    // 0: Studio Live, 1: Video Library, 2: Presets & Speed, 3: Layout & FX
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _showApplyDialog = MutableStateFlow(false)
    val showApplyDialog: StateFlow<Boolean> = _showApplyDialog.asStateFlow()

    init {
        // Pre-seed preset video loops into library if first launch
        viewModelScope.launch {
            videoLibraryRepository.preseedPresetsIfEmpty(presetManager)
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun openApplyDialog() {
        _showApplyDialog.value = true
    }

    fun closeApplyDialog() {
        _showApplyDialog.value = false
    }

    fun updateConfig(update: (WallpaperConfig) -> WallpaperConfig) {
        repository.updateConfig(update)
    }

    fun uploadVideo(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            val result = videoLibraryRepository.importVideo(uri)
            _isImporting.value = false

            result.onSuccess { saved ->
                Toast.makeText(getApplication(), "Saved \"${saved.title}\" to Video Library!", Toast.LENGTH_SHORT).show()
                // Automatically set as active dynamic wallpaper
                selectVideoFromLibrary(saved)
            }.onFailure { err ->
                Toast.makeText(getApplication(), "Upload failed: ${err.message ?: "Invalid MP4 file"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun selectVideoFromLibrary(video: SavedVideoEntity) {
        updateConfig {
            it.copy(
                customVideoPath = video.filePath,
                customVideoTitle = video.title,
                customVideoUri = null
            )
        }
        Toast.makeText(getApplication(), "Set \"${video.title}\" as active wallpaper!", Toast.LENGTH_SHORT).show()
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            videoLibraryRepository.toggleFavorite(id, isFavorite)
        }
    }

    fun renameVideo(id: Long, newTitle: String) {
        viewModelScope.launch {
            videoLibraryRepository.renameVideo(id, newTitle)
            // If currently active video was renamed, update config title as well
            if (configState.value.customVideoTitle != newTitle) {
                updateConfig { it.copy(customVideoTitle = newTitle) }
            }
        }
    }

    fun deleteVideo(video: SavedVideoEntity) {
        viewModelScope.launch {
            videoLibraryRepository.deleteVideo(video)
            // If the deleted video was currently active, fall back to default preset
            if (configState.value.customVideoPath == video.filePath) {
                updateConfig {
                    it.copy(
                        customVideoPath = null,
                        customVideoTitle = null,
                        selectedPresetId = "synthwave_horizon"
                    )
                }
            }
            Toast.makeText(getApplication(), "Removed video from library", Toast.LENGTH_SHORT).show()
        }
    }

    fun preseedPresets() {
        viewModelScope.launch {
            videoLibraryRepository.preseedPresetsIfEmpty(presetManager)
            Toast.makeText(getApplication(), "Curated preset loops loaded to library!", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleScreenMode() {
        val current = configState.value.activePreviewScreen
        val next = if (current == ScreenType.LOCK_SCREEN) ScreenType.HOME_SCREEN else ScreenType.LOCK_SCREEN
        updateConfig { it.copy(activePreviewScreen = next) }
    }

    fun handleDoubleTapAction(action: DoubleTapAction) {
        when (action) {
            DoubleTapAction.PAUSE_PLAY -> {
                Toast.makeText(getApplication(), "Double-tap: Video Play/Pause toggled", Toast.LENGTH_SHORT).show()
            }
            DoubleTapAction.MUTE_UNMUTE -> {
                val newMute = !configState.value.isMuted
                updateConfig { it.copy(isMuted = newMute) }
                val msg = if (newMute) "Wallpaper Audio Muted" else "Wallpaper Audio Unmuted"
                Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
            }
            DoubleTapAction.NEXT_PRESET -> {
                val currentVideos = libraryVideos.value
                if (currentVideos.isNotEmpty()) {
                    val currentIndex = currentVideos.indexOfFirst { it.filePath == configState.value.customVideoPath }
                    val nextIndex = (currentIndex + 1) % currentVideos.size
                    selectVideoFromLibrary(currentVideos[nextIndex])
                } else {
                    val presets = presetManager.presets
                    val curr = presets.indexOfFirst { it.id == configState.value.selectedPresetId }
                    val next = (curr + 1) % presets.size
                    updateConfig {
                        it.copy(
                            selectedPresetId = presets[next].id,
                            customVideoPath = null,
                            customVideoTitle = null
                        )
                    }
                    Toast.makeText(getApplication(), "Switched to ${presets[next].title}", Toast.LENGTH_SHORT).show()
                }
            }
            DoubleTapAction.BURST -> {
                Toast.makeText(getApplication(), "Interactive Touch Burst!", Toast.LENGTH_SHORT).show()
            }
            DoubleTapAction.NONE -> {}
        }
    }
}
