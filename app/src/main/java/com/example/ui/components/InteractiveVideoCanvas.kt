package com.example.ui.components

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.DoubleTapAction
import com.example.data.ScalingMode
import com.example.data.VideoFilterType
import com.example.data.WallpaperConfig
import com.example.video.VideoPresetManager
import com.example.video.VideoSynthesizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InteractiveVideoCanvas(
    config: WallpaperConfig,
    onDoubleTapTriggered: (DoubleTapAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val presetManager = remember { VideoPresetManager.getInstance(context) }

    var isVideoReady by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var textureSurface by remember { mutableStateOf<Surface?>(null) }
    var viewWidth by remember { mutableStateOf(0) }
    var viewHeight by remember { mutableStateOf(0) }
    var videoWidth by remember { mutableStateOf(0) }
    var videoHeight by remember { mutableStateOf(0) }

    // Active touch animations list
    val activeTouches = remember { mutableStateListOf<ActiveTouch>() }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    fun safeReleasePlayer(player: MediaPlayer?) {
        try {
            if (player != null) {
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
    }

    // Clean up media player when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            safeReleasePlayer(mediaPlayer)
            mediaPlayer = null
        }
    }

    // React to video source changes
    fun prepareVideo(surface: Surface) {
        val customPath = config.customVideoPath
        val customFile = if (!customPath.isNullOrEmpty()) File(customPath) else null

        val videoFile: File? = if (customFile != null && customFile.exists() && customFile.length() > 0) {
            customFile
        } else {
            presetManager.getPresetFile(config.selectedPresetId)
        }

        if (videoFile != null && videoFile.exists() && videoFile.length() > 1000) {
            try {
                safeReleasePlayer(mediaPlayer)
                mediaPlayer = null

                val player = MediaPlayer().apply {
                    setSurface(surface)
                    setDataSource(context, Uri.fromFile(videoFile))
                    isLooping = true

                    val vol = if (config.isMuted) 0f else config.volume
                    setVolume(vol, vol)

                    setOnVideoSizeChangedListener { _, width, height ->
                        videoWidth = width
                        videoHeight = height
                    }

                    setOnPreparedListener { mp ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val params = mp.playbackParams ?: PlaybackParams()
                                params.speed = config.playbackSpeed
                                mp.playbackParams = params
                            } catch (_: Exception) {}
                        }
                        mp.start()
                        isVideoReady = true
                    }

                    setOnErrorListener { _, _, _ ->
                        isVideoReady = false
                        true
                    }

                    prepareAsync()
                }
                mediaPlayer = player
            } catch (e: Exception) {
                Log.e("InteractiveVideoCanvas", "Failed to start player: ${e.message}")
                isVideoReady = false
            }
        } else {
            isVideoReady = false
        }
    }

    // Update audio and speed whenever config changes
    LaunchedEffect(config.isMuted, config.volume) {
        val vol = if (config.isMuted) 0f else config.volume
        try { mediaPlayer?.setVolume(vol, vol) } catch (_: Exception) {}
    }

    LaunchedEffect(config.playbackSpeed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = mediaPlayer?.playbackParams ?: PlaybackParams()
                params.speed = config.playbackSpeed
                mediaPlayer?.playbackParams = params
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(config.selectedPresetId, config.customVideoPath) {
        textureSurface?.let { prepareVideo(it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInteropFilter { motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    val now = System.currentTimeMillis()
                    val isDoubleTap = (now - lastTapTime) < 320L
                    lastTapTime = now

                    // Add touch effect
                    val touchColor = when (config.selectedPresetId) {
                        "synthwave_horizon" -> Color(0xFF38BDF8)
                        "cosmic_nebula" -> Color(0xFFA855F7)
                        "aurora_dream" -> Color(0xFF10B981)
                        else -> Color(0xFFF43F5E)
                    }
                    activeTouches.add(
                        ActiveTouch(
                            id = now,
                            x = motionEvent.x,
                            y = motionEvent.y,
                            effectType = config.touchEffect,
                            color = touchColor
                        )
                    )

                    if (isDoubleTap) {
                        onDoubleTapTriggered(config.doubleTapAction)
                    }
                }
                false
            }
    ) {
        // TextureView for hardware video playback
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                            viewWidth = width
                            viewHeight = height
                            val surface = Surface(st)
                            textureSurface = surface
                            prepareVideo(surface)
                            applyScaleTransform(this@apply, width, height, videoWidth, videoHeight, config.scalingMode)
                        }

                        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
                            viewWidth = width
                            viewHeight = height
                            applyScaleTransform(this@apply, width, height, videoWidth, videoHeight, config.scalingMode)
                        }

                        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                            textureSurface?.release()
                            textureSurface = null
                            safeReleasePlayer(mediaPlayer)
                            mediaPlayer = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                    }
                }
            },
            update = { textureView ->
                applyScaleTransform(textureView, viewWidth, viewHeight, videoWidth, videoHeight, config.scalingMode)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Smooth Canvas fallback if video is preparing or generating
        if (!isVideoReady) {
            CanvasFallbackAnimation(presetId = config.selectedPresetId)
        }

        // Color Tint Filter Overlay
        if (config.filterType != VideoFilterType.NONE && config.filterType.colorTint != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(config.filterType.colorTint).copy(alpha = config.filterIntensity))
            )
        }

        // Touch FX overlay
        TouchFxRenderer(
            activeTouches = activeTouches,
            onComplete = { id -> activeTouches.removeAll { it.id == id } }
        )
    }
}

private fun applyScaleTransform(
    view: TextureView,
    viewWidth: Int,
    viewHeight: Int,
    videoWidth: Int,
    videoHeight: Int,
    scalingMode: ScalingMode
) {
    if (viewWidth <= 0 || viewHeight <= 0) return

    val matrix = Matrix()
    when (scalingMode) {
        ScalingMode.STRETCH -> {
            // Default fills entire TextureView
        }
        ScalingMode.CROP_FILL -> {
            if (videoWidth > 0 && videoHeight > 0) {
                val scaleX = viewWidth.toFloat() / videoWidth.toFloat()
                val scaleY = viewHeight.toFloat() / videoHeight.toFloat()
                val maxScale = maxOf(scaleX, scaleY)
                val scaledW = videoWidth * maxScale
                val scaledH = videoHeight * maxScale
                val dx = (viewWidth - scaledW) / 2f
                val dy = (viewHeight - scaledH) / 2f
                matrix.setScale(maxScale, maxScale)
                matrix.postTranslate(dx, dy)
            }
        }
        ScalingMode.FIT_CENTER -> {
            if (videoWidth > 0 && videoHeight > 0) {
                val scaleX = viewWidth.toFloat() / videoWidth.toFloat()
                val scaleY = viewHeight.toFloat() / videoHeight.toFloat()
                val minScale = minOf(scaleX, scaleY)
                val scaledW = videoWidth * minScale
                val scaledH = videoHeight * minScale
                val dx = (viewWidth - scaledW) / 2f
                val dy = (viewHeight - scaledH) / 2f
                matrix.setScale(minScale, minScale)
                matrix.postTranslate(dx, dy)
            }
        }
    }
    view.setTransform(matrix)
}

@Composable
private fun CanvasFallbackAnimation(presetId: String) {
    var frame by remember { mutableStateOf(0) }
    val paint = remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG) }

    LaunchedEffect(presetId) {
        while (isActive) {
            frame = (frame + 1) % 60
            delay(33L)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawIntoCanvas { canvas ->
            VideoSynthesizer.renderFrame(canvas.nativeCanvas, presetId, frame, 60, paint)
        }
    }
}
