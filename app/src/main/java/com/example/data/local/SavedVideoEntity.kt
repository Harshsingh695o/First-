package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_videos")
data class SavedVideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val thumbnailPath: String? = null,
    val durationMs: Long = 0,
    val fileSizeBytes: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val source: String = "USER_UPLOAD" // "USER_UPLOAD" or "PRESET"
)
