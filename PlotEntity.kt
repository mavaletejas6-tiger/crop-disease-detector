package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plots")
data class PlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cropId: String,
    val variety: String,
    val areaHectares: Double = 2.5,
    val targetSowingDate: String = "2026-06-15",
    val soilType: String = "Alluvial Loam",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
