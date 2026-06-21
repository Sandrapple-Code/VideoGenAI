package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ImageGenerationItem

@Database(entities = [ImageGenerationItem::class], version = 2, exportSchema = false)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile
        private var INSTANCE: ImageDatabase? = null

        fun getDatabase(context: Context): ImageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImageDatabase::class.java,
                    "image_studio_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
