package com.example.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.example.data.DoubleTapAction
import com.example.data.WallpaperConfig
import com.example.data.WallpaperPreferencesRepository
import com.example.video.VideoPresetManager
import com.example.video.VideoSynthesizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class DynamicVideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    private inner class VideoEngine : Engine(), SurfaceHolder.Callback {
        private val TAG = "VideoWallpaperEngine"
        private var mediaPlayer: MediaPlayer? = null
        private lateinit var repository: WallpaperPreferencesRepository
        private lateinit var presetManager: VideoPresetManager
        private var configJob: Job? = null
        private var fallbackRenderJob: Job? = null
        private var currentConfig = WallpaperConfig()
        private var lastTapTime = 0L
        private var isPlaybackActive = true
        private var surfaceWidth = 0
        private var surfaceHeight = 0
        private val engineScope = CoroutineScope(Dispatchers.Main)

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            surfaceHolder.addCallback(this)

            repository = WallpaperPreferencesRepository.getInstance(applicationContext)
            presetManager = VideoPresetManager.getInstance(applicationContext)
            currentConfig = repository.getConfig()

            // Observe live changes made from the app
            configJob = engineScope.launch {
                repository.configFlow.collect { newConfig ->
                    val videoChanged = newConfig.selectedPresetId != currentConfig.selectedPresetId ||
                            newConfig.customVideoPath != currentConfig.customVideoPath
                    val audioChanged = newConfig.isMuted != currentConfig.isMuted ||
                            newConfig.volume != currentConfig.volume
                    val speedChanged = newConfig.playbackSpeed != currentConfig.playbackSpeed

                    currentConfig = newConfig

                    if (videoChanged) {
                        reloadVideo(surfaceHolder)
                    } else {
                        if (audioChanged) updateAudio()
                        if (speedChanged) updateSpeed()
                    }
                }
            }
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceCreated")
            reloadVideo(holder)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            surfaceWidth = width
            surfaceHeight = height
            Log.d(TAG, "surfaceChanged: ${width}x${height}")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceDestroyed")
            stopFallbackRender()
            releaseMediaPlayer()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                if (mediaPlayer != null) {
                    if (isPlaybackActive) {
                        try {
                            mediaPlayer?.start()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error resuming video: ${e.message}")
                            reloadVideo(surfaceHolder)
                        }
                    }
                } else if (fallbackRenderJob == null) {
                    reloadVideo(surfaceHolder)
                }
            } else {
                try {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                    }
                } catch (_: Exception) {}
            }
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN) {
                val now = System.currentTimeMillis()
                val isDoubleTap = (now - lastTapTime) < 320L
                lastTapTime = now

                if (currentConfig.touchHaptics) {
                    triggerHaptic()
                }

                if (isDoubleTap) {
                    handleDoubleTap()
                }
            }
        }

        private fun triggerHaptic() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator?.vibrate(
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(20)
                    }
                }
            } catch (_: Exception) {}
        }

        private fun handleDoubleTap() {
            when (currentConfig.doubleTapAction) {
                DoubleTapAction.PAUSE_PLAY -> {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            player.pause()
                            isPlaybackActive = false
                        } else {
                            player.start()
                            isPlaybackActive = true
                        }
                    }
                }
                DoubleTapAction.MUTE_UNMUTE -> {
                    val newMute = !currentConfig.isMuted
                    repository.updateConfig { it.copy(isMuted = newMute) }
                }
                DoubleTapAction.NEXT_PRESET -> {
                    val presetList = presetManager.presets
                    val currentIndex = presetList.indexOfFirst { it.id == currentConfig.selectedPresetId }
                    val nextIndex = (currentIndex + 1) % presetList.size
                    repository.updateConfig {
                        it.copy(
                            selectedPresetId = presetList[nextIndex].id,
                            customVideoPath = null
                        )
                    }
                }
                DoubleTapAction.BURST, DoubleTapAction.NONE -> {
                    // Burst effect is handled via canvas overlay in interactive preview
                }
            }
        }

        private fun reloadVideo(holder: SurfaceHolder) {
            stopFallbackRender()
            releaseMediaPlayer()

            val customPath = currentConfig.customVideoPath
            val customFile = if (!customPath.isNullOrEmpty()) File(customPath) else null

            val targetFile: File? = if (customFile != null && customFile.exists() && customFile.length() > 0) {
                customFile
            } else {
                presetManager.getPresetFile(currentConfig.selectedPresetId)
            }

            if (targetFile != null && targetFile.exists() && targetFile.length() > 1000) {
                initMediaPlayer(holder, targetFile)
            } else {
                // If MP4 is being generated, run smooth Canvas animation fallback
                startFallbackRender(holder, currentConfig.selectedPresetId)
            }
        }

        private fun initMediaPlayer(holder: SurfaceHolder, videoFile: File) {
            try {
                val surface = holder.surface
                if (surface == null || !surface.isValid) {
                    Log.w(TAG, "Surface is not valid, fallback to Canvas render")
                    startFallbackRender(holder, currentConfig.selectedPresetId)
                    return
                }

                mediaPlayer = MediaPlayer().apply {
                    // In WallpaperService, use setSurface() rather than setDisplay()
                    // because MySurfaceHolder throws "Wallpapers do not support keep screen on"
                    setSurface(surface)
                    setDataSource(applicationContext, Uri.fromFile(videoFile))
                    isLooping = true

                    val vol = if (currentConfig.isMuted) 0f else currentConfig.volume
                    setVolume(vol, vol)

                    setOnPreparedListener { mp ->
                        updateSpeed()
                        mp.start()
                        this@VideoEngine.isPlaybackActive = true
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra. Switching to fallback.")
                        startFallbackRender(holder, currentConfig.selectedPresetId)
                        true
                    }

                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to init MediaPlayer: ${e.message}")
                startFallbackRender(holder, currentConfig.selectedPresetId)
            }
        }

        private fun updateAudio() {
            try {
                val vol = if (currentConfig.isMuted) 0f else currentConfig.volume
                mediaPlayer?.setVolume(vol, vol)
            } catch (_: Exception) {}
        }

        private fun updateSpeed() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val params = mediaPlayer?.playbackParams ?: PlaybackParams()
                    params.speed = currentConfig.playbackSpeed
                    mediaPlayer?.playbackParams = params
                }
            } catch (_: Exception) {}
        }

        private fun startFallbackRender(holder: SurfaceHolder, presetId: String) {
            stopFallbackRender()
            fallbackRenderJob = engineScope.launch(Dispatchers.Default) {
                var frame = 0
                val totalFrames = 60
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                while (isActive) {
                    if (isVisible) {
                        var canvas: Canvas? = null
                        try {
                            canvas = holder.lockCanvas()
                            if (canvas != null) {
                                VideoSynthesizer.renderFrame(canvas, presetId, frame, totalFrames, paint)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Canvas lock failed: ${e.message}")
                        } finally {
                            if (canvas != null) {
                                try {
                                    holder.unlockCanvasAndPost(canvas)
                                } catch (_: Exception) {}
                            }
                        }
                        frame = (frame + 1) % totalFrames
                    }
                    delay(33L) // ~30 fps
                }
            }
        }

        private fun stopFallbackRender() {
            fallbackRenderJob?.cancel()
            fallbackRenderJob = null
        }

        private fun releaseMediaPlayer() {
            try {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            player.pause()
                        }
                    } catch (_: Exception) {}
                    try {
                        player.reset()
                    } catch (_: Exception) {}
                    player.release()
                }
            } catch (_: Exception) {}
            mediaPlayer = null
        }

        override fun onDestroy() {
            super.onDestroy()
            configJob?.cancel()
            stopFallbackRender()
            releaseMediaPlayer()
        }
    }
}
