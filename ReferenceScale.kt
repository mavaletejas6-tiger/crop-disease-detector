package com.example.model

data class ReferencePreset(
    val id: String,
    val label: String,
    val realMm: Float,
    val description: String,
    val isCircular: Boolean = false
)

data class ReferenceScale(
    val presetId: String = "ten_mm_block",
    val realMm: Float = 10.0f,
    val pixelSpan: Float = 120.0f, // current calibrated pixels spanning realMm
    val manualOverrideMm: Float = 10.0f
) {
    val isCircular: Boolean
        get() = PRESETS.find { it.id == presetId }?.isCircular ?: (presetId.startsWith("coin"))

    // Pixels per millimeter
    val pixelsPerMm: Float
        get() = if (realMm > 0f) pixelSpan / realMm else 12.0f

    fun pixelsToMm(pixels: Float): Float {
        return if (pixelsPerMm > 0f) pixels / pixelsPerMm else 0f
    }

    fun mmToPixels(mm: Float): Float {
        return mm * pixelsPerMm
    }

    companion object {
        val PRESETS = listOf(
            ReferencePreset("coin_one_euro", "1 Euro Coin (23.25 mm)", 23.25f, "Standard European 1 Euro coin diameter", isCircular = true),
            ReferencePreset("coin_us_quarter", "US Quarter (24.26 mm)", 24.26f, "United States 25¢ quarter coin diameter", isCircular = true),
            ReferencePreset("coin_us_penny", "US Penny (19.05 mm)", 19.05f, "United States 1¢ penny coin diameter", isCircular = true),
            ReferencePreset("ten_mm_block", "10 mm Calibration Square", 10.0f, "Standard laboratory optical 10mm target square", isCircular = false),
            ReferencePreset("credit_card_width", "Credit Card Width (53.98 mm)", 53.98f, "Standard ISO/IEC 7810 ID-1 card short edge", isCircular = false),
            ReferencePreset("credit_card_length", "Credit Card Length (85.60 mm)", 85.60f, "Standard ISO/IEC 7810 ID-1 card long edge", isCircular = false),
            ReferencePreset("custom_mm", "Custom Calibrated Dimension (mm)", 10.0f, "Enter any physical reference object length in millimeters", isCircular = false)
        )
    }
}
