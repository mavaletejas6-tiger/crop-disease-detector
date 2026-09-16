package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.GrainInspectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrainInspectionDao {
    @Query("SELECT * FROM grain_inspections ORDER BY timestamp DESC")
    fun getAllInspections(): Flow<List<GrainInspectionEntity>>

    @Query("SELECT * FROM grain_inspections WHERE plotId = :plotId ORDER BY timestamp DESC")
    fun getInspectionsForPlot(plotId: Long): Flow<List<GrainInspectionEntity>>

    @Query("SELECT * FROM grain_inspections WHERE id = :id")
    suspend fun getInspectionById(id: Long): GrainInspectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: GrainInspectionEntity): Long

    @Delete
    suspend fun deleteInspection(inspection: GrainInspectionEntity)

    @Query("DELETE FROM grain_inspections WHERE id = :id")
    suspend fun deleteInspectionById(id: Long)

    @Query("SELECT COUNT(*) FROM grain_inspections")
    suspend fun getInspectionCount(): Int

    @Query("SELECT AVG(grainLengthMm) FROM grain_inspections WHERE plotId = :plotId")
    suspend fun getAverageLengthForPlot(plotId: Long): Double?

    @Query("SELECT AVG(grainWidthMm) FROM grain_inspections WHERE plotId = :plotId")
    suspend fun getAverageWidthForPlot(plotId: Long): Double?
}
