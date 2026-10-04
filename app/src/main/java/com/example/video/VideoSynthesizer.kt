package com.example.video

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object VideoSynthesizer {
    private const val TAG = "VideoSynthesizer"
    private const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
    private const val WIDTH = 480
    private const val HEIGHT = 854
    private const val BIT_RATE = 1_800_000
    private const val FRAME_RATE = 30
    private const val I_FRAME_INTERVAL = 1
    private const val TOTAL_FRAMES = 60 // 2 second perfect loop

    suspend fun generatePresetMp4(outputFile: File, presetId: String): Boolean = withContext(Dispatchers.IO) {
        if (outputFile.exists() && outputFile.length() > 5000) {
            return@withContext true
        }

        outputFile.parentFile?.mkdirs()
        val tempFile = File(outputFile.parent, "${outputFile.name}.tmp")
        if (tempFile.exists()) tempFile.delete()

        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputSurface: Surface? = null

        try {
            val format = MediaFormat.createVideoFormat(MIME_TYPE, WIDTH, HEIGHT).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
                setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
            }

            codec = MediaCodec.createEncoderByType(MIME_TYPE)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = codec.createInputSurface()
            codec.start()

            muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            for (frame in 0 until TOTAL_FRAMES) {
                val canvas: Canvas? = try {
                    inputSurface.lockHardwareCanvas()
                } catch (e: Exception) {
                    inputSurface.lockCanvas(null)
                }

                if (canvas != null) {
                    renderFrame(canvas, presetId, frame, TOTAL_FRAMES, paint)
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                drainEncoder(codec, muxer, bufferInfo, false) { index ->
                    videoTrackIndex = index
                    muxerStarted = true
                }
            }

            codec.signalEndOfInputStream()
            drainEncoder(codec, muxer, bufferInfo, true) { index ->
                videoTrackIndex = index
                muxerStarted = true
            }

            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()
            muxer = null

            codec.stop()
            codec.release()
            codec = null
            inputSurface.release()
            inputSurface = null

            if (tempFile.exists() && tempFile.length() > 5000) {
                tempFile.renameTo(outputFile)
                Log.d(TAG, "Successfully generated MP4 for $presetId (${outputFile.length()} bytes)")
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaCodec generation fallback for $presetId: ${e.message}")
        } finally {
            try { inputSurface?.release() } catch (_: Exception) {}
            try { codec?.stop() } catch (_: Exception) {}
            try { codec?.release() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
            if (tempFile.exists()) tempFile.delete()
        }

        false
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        onMuxerStart: (Int) -> Unit
    ) {
        val timeoutUs = 10_000L
        var muxerStarted = false

        while (true) {
            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = codec.outputFormat
                val trackIndex = muxer.addTrack(newFormat)
                muxer.start()
                muxerStarted = true
                onMuxerStart(trackIndex)
            } else if (outputBufferIndex >= 0) {
                val encodedData = codec.getOutputBuffer(outputBufferIndex)
                if (encodedData != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && bufferInfo.size != 0) {
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(0, encodedData, bufferInfo)
                }
                codec.releaseOutputBuffer(outputBufferIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }

    fun renderFrame(
        canvas: Canvas,
        presetId: String,
        frame: Int,
        totalFrames: Int,
        paint: Paint
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val progress = frame.toFloat() / totalFrames.toFloat()
        val angle = progress * 2f * PI.toFloat()

        when (presetId) {
            "synthwave_horizon" -> {
                // Background dark purple/black gradient
                paint.shader = LinearGradient(0f, 0f, 0f, h, Color.parseColor("#0F051D"), Color.parseColor("#260840"), Shader.TileMode.CLAMP)
                paint.style = Paint.Style.FILL
                canvas.drawRect(0f, 0f, w, h, paint)

                // Neon Sun
                val sunCenterY = h * 0.45f
                val sunRadius = w * 0.28f
                paint.shader = LinearGradient(
                    0f, sunCenterY - sunRadius, 0f, sunCenterY + sunRadius,
                    Color.parseColor("#FFF056"), Color.parseColor("#FF007F"), Shader.TileMode.CLAMP
                )
                canvas.drawCircle(w * 0.5f, sunCenterY, sunRadius, paint)

                // Sun horizontal slices
                paint.shader = null
                paint.color = Color.parseColor("#0F051D")
                for (i in 0..5) {
                    val sliceY = sunCenterY + (i * 12f) + (sin(angle + i).toFloat() * 1.5f)
                    canvas.drawRect(w * 0.2f, sliceY, w * 0.8f, sliceY + (i * 2.2f) + 3f, paint)
                }

                // Grid Horizon Line
                val horizonY = h * 0.58f
                paint.color = Color.parseColor("#FF0099")
                paint.strokeWidth = 3f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(0f, horizonY, w, horizonY, paint)

                // Perspective Grid Lines
                paint.color = Color.parseColor("#80FF007F")
                paint.strokeWidth = 2f
                val numPersp = 12
                for (i in 0..numPersp) {
                    val startX = (w / numPersp) * i
                    canvas.drawLine(w * 0.5f, horizonY, startX * 2.2f - (w * 0.6f), h, paint)
                }

                // Moving horizontal grid lines (creates dynamic forward motion)
                val numHoriz = 10
                for (i in 0 until numHoriz) {
                    val t = ((i + progress) % numHoriz) / numHoriz
                    val gridY = horizonY + (t * t * (h - horizonY))
                    val alpha = (t * 220).toInt().coerceIn(20, 255)
                    paint.color = Color.argb(alpha, 56, 189, 248)
                    paint.strokeWidth = 1.5f + (t * 3f)
                    canvas.drawLine(0f, gridY, w, gridY, paint)
                }
            }

            "cosmic_nebula" -> {
                // Deep space background
                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.45f, w * 0.85f,
                    intArrayOf(Color.parseColor("#4C1D95"), Color.parseColor("#1E1B4B"), Color.parseColor("#030712")),
                    floatArrayOf(0.1f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.style = Paint.Style.FILL
                canvas.drawRect(0f, 0f, w, h, paint)

                // Pulsing glowing stardust clouds
                val pulse = 1f + 0.15f * sin(angle)
                paint.shader = RadialGradient(
                    w * (0.5f + 0.05f * cos(angle)), h * (0.45f + 0.05f * sin(angle)),
                    w * 0.45f * pulse,
                    intArrayOf(Color.parseColor("#B0EC4899"), Color.parseColor("#508B5CF6"), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(w * 0.5f, h * 0.45f, w * 0.6f * pulse, paint)

                // Floating stars & orbs
                paint.shader = null
                paint.style = Paint.Style.FILL
                for (i in 0..40) {
                    val starAngle = (i * 137.5f * PI / 180f).toFloat() + (progress * 0.5f)
                    val dist = (w * 0.08f) + (i * (w * 0.4f) / 40f)
                    val sx = w * 0.5f + dist * cos(starAngle)
                    val sy = h * 0.45f + dist * 1.5f * sin(starAngle)
                    val twinkle = (sin(angle * 2f + i) * 100 + 155).toInt().coerceIn(50, 255)
                    val size = (i % 3 + 2).toFloat()
                    paint.color = Color.argb(twinkle, 255, 255, 255)
                    canvas.drawCircle(sx, sy, size, paint)
                }
            }

            "aurora_dream" -> {
                // Night sky dark teal/navy
                paint.shader = LinearGradient(0f, 0f, 0f, h, Color.parseColor("#022C22"), Color.parseColor("#04101A"), Shader.TileMode.CLAMP)
                paint.style = Paint.Style.FILL
                canvas.drawRect(0f, 0f, w, h, paint)

                // Aurora waving ribbons
                paint.style = Paint.Style.FILL
                for (layer in 0..2) {
                    val path = Path()
                    val baseY = h * (0.3f + layer * 0.12f)
                    path.moveTo(0f, h)
                    path.lineTo(0f, baseY)

                    val steps = 20
                    for (s in 0..steps) {
                        val px = (w / steps) * s
                        val wave = sin((angle) + (s * 0.4f) + (layer * 1.2f)) * (40f + layer * 15f)
                        path.lineTo(px, baseY + wave)
                    }
                    path.lineTo(w, h)
                    path.close()

                    val colorStart = if (layer % 2 == 0) Color.parseColor("#5010B981") else Color.parseColor("#508B5CF6")
                    val colorEnd = Color.TRANSPARENT
                    paint.shader = LinearGradient(0f, baseY - 60f, 0f, baseY + 140f, colorStart, colorEnd, Shader.TileMode.CLAMP)
                    canvas.drawPath(path, paint)
                }

                // Mountain silhouettes in foreground
                paint.shader = null
                paint.color = Color.parseColor("#020617")
                val mountainPath = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h * 0.78f)
                    lineTo(w * 0.25f, h * 0.65f)
                    lineTo(w * 0.5f, h * 0.72f)
                    lineTo(w * 0.75f, h * 0.62f)
                    lineTo(w, h * 0.75f)
                    lineTo(w, h)
                    close()
                }
                canvas.drawPath(mountainPath, paint)
            }

            else -> { // liquid_chroma
                paint.shader = LinearGradient(
                    0f, 0f, w, h,
                    Color.parseColor("#1E1B4B"), Color.parseColor("#312E81"), Shader.TileMode.CLAMP
                )
                paint.style = Paint.Style.FILL
                canvas.drawRect(0f, 0f, w, h, paint)

                // Morphing liquid color blobs
                for (i in 0..3) {
                    val cx = w * (0.5f + 0.25f * cos(angle + i * 1.5f))
                    val cy = h * (0.45f + 0.2f * sin(angle * 1.2f + i * 2f))
                    val r = w * (0.35f + 0.1f * sin(angle + i))

                    val color = when (i) {
                        0 -> Color.parseColor("#6038BDF8")
                        1 -> Color.parseColor("#60A855F7")
                        2 -> Color.parseColor("#60F43F5E")
                        else -> Color.parseColor("#6010B981")
                    }
                    paint.shader = RadialGradient(cx, cy, r, color, Color.TRANSPARENT, Shader.TileMode.CLAMP)
                    canvas.drawCircle(cx, cy, r, paint)
                }
            }
        }
    }
}
