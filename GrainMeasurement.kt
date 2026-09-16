package com.example.model

data class DetectedGrainItem(
    val id: Int,
    val centerX: Float,
    val centerY: Float,
    val lengthPx: Float,
    val widthPx: Float,
    val angleDegrees: Float = 0f
) {
    fun lengthMm(ppm: Float): Double = if (ppm > 0f) (lengthPx / ppm).toDouble() else 0.0
    fun widthMm(ppm: Float): Double = if (ppm > 0f) (widthPx / ppm).toDouble() else 0.0
    fun aspectRatio(): Double = if (widthPx > 0f) (lengthPx / widthPx).toDouble() else 0.0
}

data class GrainMeasurement(
    val caliperLengthPx: Float = 88.0f,
    val caliperWidthPx: Float = 28.0f,
    val caliperCenterX: Float = 250.0f,
    val caliperCenterY: Float = 250.0f,
    val detectedGrains: List<DetectedGrainItem> = listOf(
        DetectedGrainItem(1, 200f, 220f, 86f, 27f),
        DetectedGrainItem(2, 290f, 210f, 89f, 29f),
        DetectedGrainItem(3, 240f, 310f, 87f, 28f),
        DetectedGrainItem(4, 340f, 320f, 92f, 29f),
        DetectedGrainItem(5, 170f, 330f, 85f, 26f)
    ),
    val activeSampleIndex: Int = 0,
    val isAutoDetectMode: Boolean = true
) {
    fun getMeasuredLengthMm(scale: ReferenceScale): Double {
        val px = if (isAutoDetectMode && detectedGrains.isNotEmpty()) {
            detectedGrains.map { it.lengthPx }.average().toFloat()
        } else {
            caliperLengthPx
        }
        return scale.pixelsToMm(px).toDouble()
    }

    fun getMeasuredWidthMm(scale: ReferenceScale): Double {
        val px = if (isAutoDetectMode && detectedGrains.isNotEmpty()) {
            detectedGrains.map { it.widthPx }.average().toFloat()
        } else {
            caliperWidthPx
        }
        return scale.pixelsToMm(px).toDouble()
    }

    fun getAspectRatio(scale: ReferenceScale): Double {
        val w = getMeasuredWidthMm(scale)
        return if (w > 0.0) getMeasuredLengthMm(scale) / w else 0.0
    }

    /**
     * Estimated Thousand Grain Weight (TGW) in grams.
     * Approximated using prolate ellipsoid volume: V = (4/3) * pi * (L/2) * (W/2)^2
     * multiplied by cereal specific bulk density coefficient (~0.00062 g/mm3 * 1000).
     */
    fun getEstimatedTgwGrams(crop: CropType, scale: ReferenceScale): Double {
        val l = getMeasuredLengthMm(scale)
        val w = getMeasuredWidthMm(scale)
        val volumeMm3 = (Math.PI / 6.0) * l * w * w
        // Density coefficient tuned per crop
        val densityCoeff = when (crop.id) {
            "corn_maize" -> 0.00072
            "wheat_bread", "wheat_durum" -> 0.00078
            "barley_malting" -> 0.00068
            "soybean", "chickpea_pulse" -> 0.00085
            else -> 0.00062
        }
        val singleWeightGrams = volumeMm3 * densityCoeff
        val rawTgw = singleWeightGrams * 1000.0
        // Bound to realistic standard range
        return rawTgw.coerceIn(crop.standardTgwMinGrams * 0.7, crop.standardTgwMaxGrams * 1.3)
    }
}
