package com.example.model

enum class SkewStatus {
    LEVEL,
    TILTED_MILD,
    TILTED_SEVERE
}

enum class LightingQuality {
    EXCELLENT,
    MARGINAL,
    POOR
}

data class ErrorIndicatorState(
    val opticalSkewDegrees: Float = 1.8f,
    val skewStatus: SkewStatus = SkewStatus.LEVEL,
    val calibrationMarginMm: Float = 0.12f,
    val calibrationMarginPercent: Float = 1.8f,
    val lightingQuality: LightingQuality = LightingQuality.EXCELLENT,
    val lightingScore: Int = 92,
    val grainOverlapDetected: Boolean = false,
    val brokenGrainDetected: Boolean = false,
    val overallConfidenceScore: Int = 94,
    val qualityAlerts: List<String> = emptyList()
) {
    val isReadyForScientificMeasurement: Boolean
        get() = overallConfidenceScore >= 80 && skewStatus != SkewStatus.TILTED_SEVERE

    companion object {
        fun computeState(
            skewAngle: Float,
            pixelsPerMm: Float,
            hasOverlap: Boolean,
            hasBroken: Boolean,
            ambientLightLevel: Float = 800f
        ): ErrorIndicatorState {
            val status = when {
                skewAngle <= 4.0f -> SkewStatus.LEVEL
                skewAngle <= 10.0f -> SkewStatus.TILTED_MILD
                else -> SkewStatus.TILTED_SEVERE
            }

            val lighting = when {
                ambientLightLevel >= 600f -> LightingQuality.EXCELLENT
                ambientLightLevel >= 250f -> LightingQuality.MARGINAL
                else -> LightingQuality.POOR
            }

            // Estimate margin based on resolution and skew
            val baseMarginMm = (1.2f / pixelsPerMm.coerceAtLeast(1.0f)) + (skewAngle * 0.015f)
            val marginPercent = (baseMarginMm / 6.5f) * 100f // benchmark grain length 6.5mm

            val alerts = mutableListOf<String>()
            var confidence = 98

            if (status == SkewStatus.TILTED_MILD) {
                confidence -= 12
                alerts.add("Slight tilt (${"%.1f".format(skewAngle)}°). Hold device parallel to surface for optimal caliper precision.")
            } else if (status == SkewStatus.TILTED_SEVERE) {
                confidence -= 35
                alerts.add("Critical tilt warning (${"%.1f".format(skewAngle)}°)! Perspective distortion will skew mm measurements.")
            }

            if (lighting == LightingQuality.MARGINAL) {
                confidence -= 10
                alerts.add("Diffused shadow detected. Increase overhead illumination.")
            } else if (lighting == LightingQuality.POOR) {
                confidence -= 25
                alerts.add("Low contrast background. Place grains on contrasting dark or white surface.")
            }

            if (hasOverlap) {
                confidence -= 15
                alerts.add("Cluster overlap detected. Separate touching grains for individual contour isolation.")
            }

            if (hasBroken) {
                alerts.add("Broken kernel flagged. Classified separately to prevent bias in average size.")
            }

            return ErrorIndicatorState(
                opticalSkewDegrees = skewAngle,
                skewStatus = status,
                calibrationMarginMm = baseMarginMm,
                calibrationMarginPercent = marginPercent,
                lightingQuality = lighting,
                lightingScore = when (lighting) {
                    LightingQuality.EXCELLENT -> 95
                    LightingQuality.MARGINAL -> 68
                    LightingQuality.POOR -> 35
                },
                grainOverlapDetected = hasOverlap,
                brokenGrainDetected = hasBroken,
                overallConfidenceScore = confidence.coerceIn(10, 100),
                qualityAlerts = alerts
            )
        }
    }
}
