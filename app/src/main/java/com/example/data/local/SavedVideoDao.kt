package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedVideoDao {

    @Query("SELECT * FROM saved_videos ORDER BY isFavorite DESC, dateAdded DESC")
    fun getAllVideos(): Flow<List<SavedVideoEntity>>

    @Query("SELECT * FROM saved_videos WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteVideos(): Flow<List<SavedVideoEntity>>

    @Query("SELECT * FROM saved_videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): SavedVideoEntity?

    @Query("SELECT * FROM saved_videos WHERE filePath = :filePath LIMIT 1")
    suspend fun getVideoByPath(filePath: String): SavedVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: SavedVideoEntity): Long

    @Update
    suspend fun updateVideo(video: SavedVideoEntity)

    @Query("UPDATE saved_videos SET title = :newTitle WHERE id = :id")
    suspend fun renameVideo(id: Long, newTitle: String)

    @Query("UPDATE saved_videos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM saved_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Delete
    suspend fun deleteVideo(video: SavedVideoEntity)
}
