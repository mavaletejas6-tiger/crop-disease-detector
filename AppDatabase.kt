package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.GrainInspectionDao
import com.example.data.dao.PlotDao
import com.example.data.entity.GrainInspectionEntity
import com.example.data.entity.PlotEntity

@Database(
    entities = [PlotEntity::class, GrainInspectionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plotDao(): PlotDao
    abstract fun grainInspectionDao(): GrainInspectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grain_scan_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
