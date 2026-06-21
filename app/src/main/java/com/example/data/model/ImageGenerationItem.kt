package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "generated_images_history")
data class ImageGenerationItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalPrompt: String,
    val selectedStyle: String,
    val finalPrompt: String,
    val negativePrompt: String = "",
    val imagePath: String, // Path to local storage folder on device
    val aspectRatio: String = "1:1",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isVideo: Boolean = false
) : Serializable
