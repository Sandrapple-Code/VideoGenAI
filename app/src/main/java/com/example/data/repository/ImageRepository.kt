package com.example.data.repository

import com.example.data.local.ImageDao
import com.example.data.model.ImageGenerationItem
import kotlinx.coroutines.flow.Flow

class ImageRepository(private val imageDao: ImageDao) {
    val allHistory: Flow<List<ImageGenerationItem>> = imageDao.getAllHistory()

    suspend fun insertItem(item: ImageGenerationItem): Long {
        return imageDao.insertItem(item)
    }

    suspend fun deleteItem(item: ImageGenerationItem) {
        imageDao.deleteItem(item)
    }

    suspend fun deleteItemById(id: Int) {
        imageDao.deleteItemById(id)
    }

    suspend fun updateFavorite(id: Int, isFavorite: Boolean) {
        imageDao.updateFavorite(id, isFavorite)
    }

    suspend fun clearAll() {
        imageDao.clearAll()
    }
}
