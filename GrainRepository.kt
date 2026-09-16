package com.example.data

import com.example.data.dao.GrainInspectionDao
import com.example.data.dao.PlotDao
import com.example.data.entity.GrainInspectionEntity
import com.example.data.entity.PlotEntity
import kotlinx.coroutines.flow.Flow

class GrainRepository(
    private val plotDao: PlotDao,
    private val inspectionDao: GrainInspectionDao
) {
    val allPlots: Flow<List<PlotEntity>> = plotDao.getAllPlots()
    val allInspections: Flow<List<GrainInspectionEntity>> = inspectionDao.getAllInspections()

    fun getInspectionsForPlot(plotId: Long): Flow<List<GrainInspectionEntity>> {
        return inspectionDao.getInspectionsForPlot(plotId)
    }

    suspend fun insertPlot(plot: PlotEntity): Long {
        return plotDao.insertPlot(plot)
    }

    suspend fun updatePlot(plot: PlotEntity) {
        plotDao.updatePlot(plot)
    }

    suspend fun deletePlot(plot: PlotEntity) {
        plotDao.deletePlot(plot)
    }

    suspend fun insertInspection(inspection: GrainInspectionEntity): Long {
        return inspectionDao.insertInspection(inspection)
    }

    suspend fun deleteInspection(inspection: GrainInspectionEntity) {
        inspectionDao.deleteInspection(inspection)
    }

    suspend fun deleteInspectionById(id: Long) {
        inspectionDao.deleteInspectionById(id)
    }

    suspend fun seedInitialDataIfEmpty() {
        if (plotDao.getPlotCount() == 0) {
            val plot1Id = plotDao.insertPlot(
                PlotEntity(
                    name = "Plot 1 - North Valley Rice Basin",
                    cropId = "rice_paddy",
                    variety = "Basmati Taraori-386",
                    areaHectares = 4.2,
                    targetSowingDate = "2026-06-10",
                    soilType = "Clay Loam",
                    notes = "Certified organic paddy block. High export quality target."
                )
            )

            val plot2Id = plotDao.insertPlot(
                PlotEntity(
                    name = "Plot 2 - East Terrace Wheat Field",
                    cropId = "wheat_bread",
                    variety = "HD-2967 High Yield",
                    areaHectares = 6.8,
                    targetSowingDate = "2026-11-05",
                    soilType = "Alluvial Silt Loam",
                    notes = "Rotational field after legumes. Standard test weight evaluation."
                )
            )

            val plot3Id = plotDao.insertPlot(
                PlotEntity(
                    name = "Plot 3 - Highland Barley Malt Nursery",
                    cropId = "barley_malting",
                    variety = "Conlon 2-Row Malting",
                    areaHectares = 3.5,
                    targetSowingDate = "2026-04-20",
                    soilType = "Sandy Loam",
                    notes = "Plumpness monitoring for brewery grain delivery contract."
                )
            )

            // Seed a few representative inspections
            inspectionDao.insertInspection(
                GrainInspectionEntity(
                    plotId = plot1Id,
                    plotName = "Plot 1 - North Valley Rice Basin",
                    cropId = "rice_paddy",
                    cropName = "Rice (Paddy)",
                    grainLengthMm = 7.34,
                    grainWidthMm = 2.21,
                    aspectRatio = 3.32,
                    estimatedTgwGrams = 25.4,
                    sizeClassification = "Long Grain (L: 7.34mm)",
                    errorMarginMm = 0.08,
                    confidencePercent = 95,
                    diseasePestName = "Rice Blast / Neck Blast",
                    pestSeverityScale = 1,
                    damagePercent = 3.2,
                    nutrientDeficiency = "Mild Nitrogen Deficit",
                    correctivePrescription = "Top-dress 45 kg/ha Urea at active tillering.",
                    gpsLatitude = 38.5449,
                    gpsLongitude = -121.7405,
                    gpsAltitude = 16.5,
                    weatherTempC = 27.2,
                    weatherHumidity = 54,
                    weatherWindKmh = 8.4,
                    weatherCondition = "Clear Sky",
                    sampleCount = 18,
                    notes = "Excellent kernel length elongation. Plump endosperm with minimal chalkiness."
                )
            )

            inspectionDao.insertInspection(
                GrainInspectionEntity(
                    plotId = plot2Id,
                    plotName = "Plot 2 - East Terrace Wheat Field",
                    cropId = "wheat_bread",
                    cropName = "Bread Wheat",
                    grainLengthMm = 6.65,
                    grainWidthMm = 3.15,
                    aspectRatio = 2.11,
                    estimatedTgwGrams = 41.8,
                    sizeClassification = "Standard Grade Kernel (6.65mm)",
                    errorMarginMm = 0.09,
                    confidencePercent = 92,
                    diseasePestName = "Fusarium Head Blight",
                    pestSeverityScale = 1,
                    damagePercent = 2.1,
                    nutrientDeficiency = "Zinc Deficiency",
                    correctivePrescription = "Foliar 0.5% ZnSO4 spray.",
                    gpsLatitude = 38.5480,
                    gpsLongitude = -121.7380,
                    gpsAltitude = 18.0,
                    weatherTempC = 25.8,
                    weatherHumidity = 61,
                    weatherWindKmh = 11.0,
                    weatherCondition = "Partly Cloudy",
                    sampleCount = 24,
                    notes = "Plump wheat kernels. Uniform vitreousness."
                )
            )
        }
    }
}
