package com.example.service

import android.content.Context
import android.content.Intent
import com.example.data.entity.GrainInspectionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExporter {

    fun generateCsv(inspections: List<GrainInspectionEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sb = StringBuilder()

        // CSV Header
        sb.append("Inspection_ID,Timestamp,Plot_Name,Crop,Length_mm,Width_mm,Aspect_Ratio,Size_Class,Est_TGW_g,Error_Margin_mm,Confidence_pct,Disease_Pest,Pest_Severity_0_5,Damage_pct,Nutrient_Deficiency,Corrective_Prescription,GPS_Latitude,GPS_Longitude,GPS_Altitude_m,Weather_Temp_C,Weather_Humidity_pct,Weather_Wind_kmh,Weather_Condition,Notes\n")

        for (item in inspections) {
            val dateStr = dateFormat.format(Date(item.timestamp))
            val safePlot = escapeCsv(item.plotName)
            val safeCrop = escapeCsv(item.cropName)
            val safeClass = escapeCsv(item.sizeClassification)
            val safePest = escapeCsv(item.diseasePestName)
            val safeNutrient = escapeCsv(item.nutrientDeficiency)
            val safePrescription = escapeCsv(item.correctivePrescription)
            val safeWeather = escapeCsv(item.weatherCondition)
            val safeNotes = escapeCsv(item.notes)

            sb.append("${item.id},")
            sb.append("$dateStr,")
            sb.append("$safePlot,")
            sb.append("$safeCrop,")
            sb.append("${"%.2f".format(item.grainLengthMm)},")
            sb.append("${"%.2f".format(item.grainWidthMm)},")
            sb.append("${"%.2f".format(item.aspectRatio)},")
            sb.append("$safeClass,")
            sb.append("${"%.1f".format(item.estimatedTgwGrams)},")
            sb.append("${"%.2f".format(item.errorMarginMm)},")
            sb.append("${item.confidencePercent},")
            sb.append("$safePest,")
            sb.append("${item.pestSeverityScale},")
            sb.append("${"%.1f".format(item.damagePercent)},")
            sb.append("$safeNutrient,")
            sb.append("$safePrescription,")
            sb.append("${item.gpsLatitude},")
            sb.append("${item.gpsLongitude},")
            sb.append("${item.gpsAltitude},")
            sb.append("${"%.1f".format(item.weatherTempC)},")
            sb.append("${item.weatherHumidity},")
            sb.append("${"%.1f".format(item.weatherWindKmh)},")
            sb.append("$safeWeather,")
            sb.append("$safeNotes\n")
        }

        return sb.toString()
    }

    fun generateAgronomySummary(inspections: List<GrainInspectionEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("      GRAINSCAN AGRONOMY FIELD AUDIT    \n")
        sb.append("========================================\n")
        sb.append("Generated: ${dateFormat.format(Date())}\n")
        sb.append("Total Inspection Samples: ${inspections.size}\n\n")

        if (inspections.isEmpty()) {
            sb.append("No inspection records logged yet.\n")
            return sb.toString()
        }

        val avgLength = inspections.map { it.grainLengthMm }.average()
        val avgWidth = inspections.map { it.grainWidthMm }.average()
        val avgTgw = inspections.map { it.estimatedTgwGrams }.average()

        sb.append("--- BATCH SUMMARY METRICS ---\n")
        sb.append("• Mean Grain Length: ${"%.2f".format(avgLength)} mm\n")
        sb.append("• Mean Grain Width: ${"%.2f".format(avgWidth)} mm\n")
        sb.append("• Mean L/W Aspect Ratio: ${"%.2f".format(avgLength / avgWidth)}\n")
        sb.append("• Mean Thousand Grain Wt (TGW): ${"%.1f".format(avgTgw)} g\n\n")

        sb.append("--- PLOT INSPECTIONS ---\n")
        inspections.take(10).forEachIndexed { index, item ->
            sb.append("[#${index + 1}] ${item.plotName} (${item.cropName})\n")
            sb.append("    Dimensions: ${"%.2f".format(item.grainLengthMm)} x ${"%.2f".format(item.grainWidthMm)} mm (${item.sizeClassification})\n")
            sb.append("    GPS Tag: ${"%.5f".format(item.gpsLatitude)}, ${"%.5f".format(item.gpsLongitude)} (Alt: ${"%.1f".format(item.gpsAltitude)}m)\n")
            sb.append("    Health: ${item.diseasePestName} (Scale ${item.pestSeverityScale}/5) | ${item.nutrientDeficiency}\n")
            sb.append("    Weather Overlay: ${"%.1f".format(item.weatherTempC)}°C, ${item.weatherHumidity}% RH, ${"%.1f".format(item.weatherWindKmh)} km/h\n")
            sb.append("    Prescription: ${item.correctivePrescription}\n\n")
        }

        if (inspections.size > 10) {
            sb.append("... and ${inspections.size - 10} additional samples included in CSV export.\n")
        }

        return sb.toString()
    }

    fun shareExport(context: Context, csvData: String, summaryText: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, "GrainScan Field Agronomy Export.csv")
            putExtra(Intent.EXTRA_SUBJECT, "GrainScan Field Agronomy Export Data")
            putExtra(Intent.EXTRA_TEXT, "$summaryText\n\n--- RAW CSV DATA ---\n$csvData")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Grain Inspection Data")
        context.startActivity(shareIntent)
    }

    private fun escapeCsv(value: String): String {
        val containsSpecial = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (containsSpecial) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
