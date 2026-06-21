package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ImageGenerationItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Query("SELECT * FROM generated_images_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ImageGenerationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ImageGenerationItem): Long

    @Delete
    suspend fun deleteItem(item: ImageGenerationItem)

    @Query("DELETE FROM generated_images_history WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("UPDATE generated_images_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)

    @Query("DELETE FROM generated_images_history")
    suspend fun clearAll()
}
