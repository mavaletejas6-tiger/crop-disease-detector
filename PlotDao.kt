package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlotDao {
    @Query("SELECT * FROM plots ORDER BY createdAt DESC")
    fun getAllPlots(): Flow<List<PlotEntity>>

    @Query("SELECT * FROM plots WHERE id = :id")
    fun getPlotById(id: Long): Flow<PlotEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: PlotEntity): Long

    @Update
    suspend fun updatePlot(plot: PlotEntity)

    @Delete
    suspend fun deletePlot(plot: PlotEntity)

    @Query("SELECT COUNT(*) FROM plots")
    suspend fun getPlotCount(): Int
}
