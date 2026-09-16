package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grain_inspections")
data class GrainInspectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plotId: Long,
    val plotName: String,
    val cropId: String,
    val cropName: String,
    val grainLengthMm: Double,
    val grainWidthMm: Double,
    val aspectRatio: Double,
    val estimatedTgwGrams: Double,
    val sizeClassification: String,
    val errorMarginMm: Double,
    val confidencePercent: Int,
    val diseasePestName: String,
    val pestSeverityScale: Int, // 0 to 5
    val damagePercent: Double,
    val nutrientDeficiency: String,
    val correctivePrescription: String,
    val gpsLatitude: Double,
    val gpsLongitude: Double,
    val gpsAltitude: Double,
    val gpsAccuracyMeters: Float = 3.5f,
    val weatherTempC: Double,
    val weatherHumidity: Int,
    val weatherWindKmh: Double,
    val weatherCondition: String,
    val sampleCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
