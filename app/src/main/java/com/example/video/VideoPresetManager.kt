package com.example.video

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.VideoPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class VideoPresetManager(private val context: Context) {

    val presets = listOf(
        VideoPreset(
            id = "synthwave_horizon",
            title = "Synthwave Horizon",
            category = "Cyberpunk",
            description = "Neon retro grid with pulsing sun and cyber horizon waves",
            primaryColorHex = 0xFFFF007F,
            secondaryColorHex = 0xFF38BDF8,
            tag = "Dynamic MP4"
        ),
        VideoPreset(
            id = "cosmic_nebula",
            title = "Cosmic Nebula",
            category = "Sci-Fi Space",
            description = "Ethereal violet galaxy vortex with twinkling star clusters",
            primaryColorHex = 0xFFA855F7,
            secondaryColorHex = 0xFFEC4899,
            tag = "Dynamic MP4"
        ),
        VideoPreset(
            id = "aurora_dream",
            title = "Aurora Dream",
            category = "Atmospheric",
            description = "Hypnotic emerald ribbons dancing over night horizon",
            primaryColorHex = 0xFF10B981,
            secondaryColorHex = 0xFF06B6D4,
            tag = "Dynamic MP4"
        ),
        VideoPreset(
            id = "liquid_chroma",
            title = "Liquid Chroma",
            category = "Abstract Flow",
            description = "Organic fluid gradients morphing in mesmerizing motion",
            primaryColorHex = 0xFF38BDF8,
            secondaryColorHex = 0xFFF43F5E,
            tag = "Dynamic MP4"
        )
    )

    init {
        // Kick off synthesis of preset MP4 files in background
        CoroutineScope(Dispatchers.IO).launch {
            ensurePresetsGenerated()
        }
    }

    suspend fun ensurePresetsGenerated() {
        val presetsDir = File(context.filesDir, "presets").apply { mkdirs() }
        for (preset in presets) {
            val file = File(presetsDir, "${preset.id}.mp4")
            if (!file.exists() || file.length() < 1000) {
                try {
                    VideoSynthesizer.generatePresetMp4(file, preset.id)
                } catch (e: Exception) {
                    Log.e("VideoPresetManager", "Error generating ${preset.id}: ${e.message}")
                }
            }
        }
    }

    fun getPresetFile(presetId: String): File? {
        val file = File(File(context.filesDir, "presets"), "$presetId.mp4")
        return if (file.exists() && file.length() > 0) file else null
    }

    fun importCustomVideo(uri: Uri): Pair<File?, String?> {
        return try {
            val customDir = File(context.filesDir, "custom_wallpapers").apply { mkdirs() }
            val timestamp = System.currentTimeMillis()
            val destFile = File(customDir, "custom_wallpaper_$timestamp.mp4")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile.exists() && destFile.length() > 0) {
                Pair(destFile, "My Video #${(100..999).random()}")
            } else {
                Pair(null, null)
            }
        } catch (e: Exception) {
            Log.e("VideoPresetManager", "Error importing video: ${e.message}", e)
            Pair(null, null)
        }
    }

    fun getCustomVideos(): List<File> {
        val customDir = File(context.filesDir, "custom_wallpapers")
        return customDir.listFiles { file -> file.extension.lowercase() == "mp4" }?.toList() ?: emptyList()
    }

    companion object {
        @Volatile
        private var INSTANCE: VideoPresetManager? = null

        fun getInstance(context: Context): VideoPresetManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VideoPresetManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
