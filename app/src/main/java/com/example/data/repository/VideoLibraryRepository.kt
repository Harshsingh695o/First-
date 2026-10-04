package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.SavedVideoDao
import com.example.data.local.SavedVideoEntity
import com.example.video.VideoPresetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class VideoLibraryRepository(
    private val dao: SavedVideoDao,
    private val context: Context
) {
    val allVideos: Flow<List<SavedVideoEntity>> = dao.getAllVideos()
    val favoriteVideos: Flow<List<SavedVideoEntity>> = dao.getFavoriteVideos()

    suspend fun importVideo(uri: Uri): Result<SavedVideoEntity> = withContext(Dispatchers.IO) {
        try {
            val libraryDir = File(context.filesDir, "video_library").apply { mkdirs() }
            val thumbsDir = File(context.filesDir, "thumbnails").apply { mkdirs() }
            val timestamp = System.currentTimeMillis()

            // Resolve original filename if available
            val originalName = resolveFileName(uri) ?: "Dynamic_Wallpaper_$timestamp.mp4"
            val displayTitle = originalName.substringBeforeLast(".")
                .replace("_", " ")
                .replace("-", " ")
                .trim()
                .ifEmpty { "Dynamic Video" }

            val destinationFile = File(libraryDir, "video_${timestamp}.mp4")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Failed to read video file from storage"))

            if (!destinationFile.exists() || destinationFile.length() == 0L) {
                return@withContext Result.failure(Exception("Saved file is empty"))
            }

            // Extract metadata and thumbnail
            var durationMs = 0L
            var width = 0
            var height = 0
            var thumbFile: File? = null

            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(destinationFile.absolutePath)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                durationMs = durationStr?.toLongOrNull() ?: 0L

                val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                width = widthStr?.toIntOrNull() ?: 0
                height = heightStr?.toIntOrNull() ?: 0

                val frameBitmap = retriever.getFrameAtTime(
                    500_000L, // 0.5 second
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                ) ?: retriever.frameAtTime

                if (frameBitmap != null) {
                    val file = File(thumbsDir, "thumb_${timestamp}.jpg")
                    FileOutputStream(file).use { out ->
                        frameBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    thumbFile = file
                }
            } catch (e: Exception) {
                Log.w("VideoLibraryRepo", "Failed to extract metadata: ${e.message}")
            } finally {
                try { retriever.release() } catch (_: Exception) {}
            }

            val entity = SavedVideoEntity(
                title = displayTitle,
                filePath = destinationFile.absolutePath,
                thumbnailPath = thumbFile?.absolutePath,
                durationMs = durationMs,
                fileSizeBytes = destinationFile.length(),
                width = width,
                height = height,
                dateAdded = timestamp,
                isFavorite = false,
                source = "USER_UPLOAD"
            )

            val newId = dao.insertVideo(entity)
            val savedEntity = entity.copy(id = newId)

            Result.success(savedEntity)
        } catch (e: Exception) {
            Log.e("VideoLibraryRepo", "Error importing video: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteVideo(video: SavedVideoEntity) = withContext(Dispatchers.IO) {
        try {
            val file = File(video.filePath)
            if (file.exists()) file.delete()

            video.thumbnailPath?.let {
                val thumb = File(it)
                if (thumb.exists()) thumb.delete()
            }

            dao.deleteVideoById(video.id)
        } catch (e: Exception) {
            Log.e("VideoLibraryRepo", "Error deleting video: ${e.message}")
        }
    }

    suspend fun renameVideo(id: Long, newTitle: String) = withContext(Dispatchers.IO) {
        dao.renameVideo(id, newTitle)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        dao.setFavorite(id, isFavorite)
    }

    suspend fun preseedPresetsIfEmpty(presetManager: VideoPresetManager) = withContext(Dispatchers.IO) {
        try {
            presetManager.ensurePresetsGenerated()
            val presets = presetManager.presets
            for (preset in presets) {
                val file = presetManager.getPresetFile(preset.id)
                if (file != null && file.exists()) {
                    val existing = dao.getVideoByPath(file.absolutePath)
                    if (existing == null) {
                        dao.insertVideo(
                            SavedVideoEntity(
                                title = preset.title,
                                filePath = file.absolutePath,
                                thumbnailPath = null,
                                durationMs = 2000L,
                                fileSizeBytes = file.length(),
                                width = 480,
                                height = 854,
                                dateAdded = System.currentTimeMillis(),
                                isFavorite = true,
                                source = "PRESET"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VideoLibraryRepo", "Preseed presets error: ${e.message}")
        }
    }

    private fun resolveFileName(uri: Uri): String? {
        var name: String? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        } catch (_: Exception) {}
        return name
    }

    companion object {
        @Volatile
        private var INSTANCE: VideoLibraryRepository? = null

        fun getInstance(context: Context): VideoLibraryRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                INSTANCE ?: VideoLibraryRepository(db.savedVideoDao(), context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
