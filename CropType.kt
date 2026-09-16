package com.example.model

enum class CropCategory {
    CEREAL,
    PULSE,
    OILSEED
}

data class CropType(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val category: CropCategory,
    val standardLengthMinMm: Double,
    val standardLengthMaxMm: Double,
    val standardWidthMinMm: Double,
    val standardWidthMaxMm: Double,
    val optimalAspectRatio: Double,
    val standardTgwMinGrams: Double,
    val standardTgwMaxGrams: Double,
    val sizeClasses: List<String>,
    val moistureTargetPercent: Double,
    val millingRecoveryAdvice: String,
    val sowingTargetPlantsM2: Int,
    val fieldEmergenceRate: Double = 0.85
) {
    fun classifyGrain(lengthMm: Double, widthMm: Double): String {
        val ratio = if (widthMm > 0) lengthMm / widthMm else 0.0
        return when (id) {
            "rice_paddy", "rice_milled" -> when {
                lengthMm >= 7.50 -> "Extra Long Grain (L: ${"%.2f".format(lengthMm)}mm)"
                lengthMm in 6.61..7.49 -> "Long Grain (L: ${"%.2f".format(lengthMm)}mm)"
                lengthMm in 5.51..6.60 -> "Medium Grain (L: ${"%.2f".format(lengthMm)}mm)"
                else -> "Short / Round Grain (L: ${"%.2f".format(lengthMm)}mm)"
            }
            "wheat_bread", "wheat_durum" -> when {
                lengthMm >= 6.8 -> "Plump / Bold Kernel (${"%.2f".format(lengthMm)}mm)"
                lengthMm in 5.8..6.79 -> "Standard Grade Kernel (${"%.2f".format(lengthMm)}mm)"
                else -> "Pinched / Small Kernel (${"%.2f".format(lengthMm)}mm)"
            }
            "barley_malting" -> when {
                widthMm >= 2.50 -> "Grade 1 Plump Malting (>2.5mm)"
                widthMm in 2.20..2.49 -> "Grade 2 Standard (2.2-2.5mm)"
                else -> "Screenings / Feed Grain (<2.2mm)"
            }
            "corn_maize" -> when {
                lengthMm >= 11.0 -> "Large Flat Kernel (${"%.2f".format(lengthMm)}mm)"
                lengthMm in 8.5..10.9 -> "Medium Flat Kernel (${"%.2f".format(lengthMm)}mm)"
                else -> "Small Round Kernel (${"%.2f".format(lengthMm)}mm)"
            }
            "soybean" -> when {
                lengthMm >= 7.0 -> "Large Seed (${"%.2f".format(lengthMm)}mm)"
                lengthMm in 5.5..6.99 -> "Medium Standard Seed (${"%.2f".format(lengthMm)}mm)"
                else -> "Small Seed (${"%.2f".format(lengthMm)}mm)"
            }
            else -> when {
                ratio >= 3.0 -> "Slender Kernel (Ratio ${"%.2f".format(ratio)})"
                ratio in 2.1..2.99 -> "Medium Kernel (Ratio ${"%.2f".format(ratio)})"
                else -> "Bold / Round Kernel (Ratio ${"%.2f".format(ratio)})"
            }
        }
    }

    /**
     * Calculates recommended sowing rate in kg/ha based on measured Thousand Grain Weight (TGW).
     * Formula: Seed rate (kg/ha) = (Target plants/m² * TGW in g) / (Germination% * Purity% * Field Emergence%) / 10
     */
    fun calculateSowingRateKgHa(measuredTgwGrams: Double, germinationRate: Double = 0.90, purityRate: Double = 0.98): Double {
        val denom = germinationRate * purityRate * fieldEmergenceRate * 10.0
        return if (denom > 0) (sowingTargetPlantsM2 * measuredTgwGrams) / denom else 0.0
    }
}

object CropTypeRepository {
    val crops: List<CropType> = listOf(
        CropType(
            id = "rice_paddy",
            commonName = "Rice (Paddy)",
            scientificName = "Oryza sativa",
            category = CropCategory.CEREAL,
            standardLengthMinMm = 6.2,
            standardLengthMaxMm = 8.2,
            standardWidthMinMm = 2.0,
            standardWidthMaxMm = 2.7,
            optimalAspectRatio = 3.1,
            standardTgwMinGrams = 22.0,
            standardTgwMaxGrams = 29.0,
            sizeClasses = listOf("Extra Long (>7.5mm)", "Long (6.6-7.5mm)", "Medium (5.5-6.6mm)", "Short (<5.5mm)"),
            moistureTargetPercent = 14.0,
            millingRecoveryAdvice = "Ensure paddy moisture is tempered to 13.5-14.0% before dehulling to maximize Head Rice Recovery (HRR) and avoid broken grain stress cracks.",
            sowingTargetPlantsM2 = 250
        ),
        CropType(
            id = "rice_milled",
            commonName = "Rice (Milled / White)",
            scientificName = "Oryza sativa",
            category = CropCategory.CEREAL,
            standardLengthMinMm = 5.5,
            standardLengthMaxMm = 7.4,
            standardWidthMinMm = 1.8,
            standardWidthMaxMm = 2.3,
            optimalAspectRatio = 3.2,
            standardTgwMinGrams = 18.0,
            standardTgwMaxGrams = 24.0,
            sizeClasses = listOf("Extra Long", "Long", "Medium", "Short"),
            moistureTargetPercent = 12.5,
            millingRecoveryAdvice = "Grade with standard sizing cylinder. Whiteness index optimal at 38-42 with minimum chalky belly cores.",
            sowingTargetPlantsM2 = 250
        ),
        CropType(
            id = "wheat_bread",
            commonName = "Bread Wheat",
            scientificName = "Triticum aestivum",
            category = CropCategory.CEREAL,
            standardLengthMinMm = 5.8,
            standardLengthMaxMm = 7.2,
            standardWidthMinMm = 2.8,
            standardWidthMaxMm = 3.6,
            optimalAspectRatio = 2.0,
            standardTgwMinGrams = 34.0,
            standardTgwMaxGrams = 46.0,
            sizeClasses = listOf("Plump (>6.8mm)", "Standard (5.8-6.8mm)", "Pinched (<5.8mm)"),
            moistureTargetPercent = 13.0,
            millingRecoveryAdvice = "Condition wheat to 15.5% moisture 16 hours prior to break rolls. Screen out shriveled kernels to retain test weight > 78 kg/hL.",
            sowingTargetPlantsM2 = 320
        ),
        CropType(
            id = "wheat_durum",
            commonName = "Durum Wheat (Semolina)",
            scientificName = "Triticum durum",
            category = CropCategory.CEREAL,
            standardLengthMinMm = 6.5,
            standardLengthMaxMm = 8.5,
            standardWidthMinMm = 3.0,
            standardWidthMaxMm = 3.8,
            optimalAspectRatio = 2.2,
            standardTgwMinGrams = 40.0,
            standardTgwMaxGrams = 55.0,
            sizeClasses = listOf("Large Vitreous", "Standard Vitreous", "Starchy/Piebald"),
            moistureTargetPercent = 12.5,
            millingRecoveryAdvice = "High vitreo-kernel ratio (>80%) required for premium semolina yield. Minimize mechanical damage during harvest handling.",
            sowingTargetPlantsM2 = 300
        ),
        CropType(
            id = "barley_malting",
            commonName = "Malting Barley (2-Row / 6-Row)",
            scientificName = "Hordeum vulgare",
            category = CropCategory.CEREAL,
            standardLengthMinMm = 7.0,
            standardLengthMaxMm = 9.5,
            standardWidthMinMm = 2.4,
            standardWidthMaxMm = 3.4,
            optimalAspectRatio = 2.8,
            standardTgwMinGrams = 38.0,
            standardTgwMaxGrams = 50.0,
            sizeClasses = listOf("Plump Screen >2.5mm", "Mid Screen 2.2-2.5mm", "Thin Screenings <2.2mm"),
            moistureTargetPercent = 13.0,
            millingRecoveryAdvice = "Target >90% plumpness on 2.5mm slotted screen for top tier brewery acceptance. High test weight correlates to uniform malt modification.",
            sowingTargetPlantsM2 = 280
        ),
        CropType(
            id = "corn_maize",
            commonName = "Corn / Maize (Dent & Flint)",
            scientificName = "Zea mays",
            category = CropCategory.CEREAL,
            standardLengthMinMm = 8.5,
            standardLengthMaxMm = 12.5,
            standardWidthMinMm = 7.0,
            standardWidthMaxMm = 9.5,
            optimalAspectRatio = 1.3,
            standardTgwMinGrams = 260.0,
            standardTgwMaxGrams = 360.0,
            sizeClasses = listOf("Large Flat", "Medium Flat", "Small Flat", "Large Round", "Small Round"),
            moistureTargetPercent = 15.0,
            millingRecoveryAdvice = "Slow-dry with gentle aeration below 55°C to avoid stress cracks in endosperm. Uniform seed caliber ensures singulation during precision planting.",
            sowingTargetPlantsM2 = 8
        ),
        CropType(
            id = "soybean",
            commonName = "Soybean",
            scientificName = "Glycine max",
            category = CropCategory.OILSEED,
            standardLengthMinMm = 5.5,
            standardLengthMaxMm = 8.5,
            standardWidthMinMm = 5.0,
            standardWidthMaxMm = 7.8,
            optimalAspectRatio = 1.1,
            standardTgwMinGrams = 140.0,
            standardTgwMaxGrams = 210.0,
            sizeClasses = listOf("Large (>7mm)", "Medium (5.5-7mm)", "Small (<5.5mm)"),
            moistureTargetPercent = 13.0,
            millingRecoveryAdvice = "Avoid mechanical impact below 11% moisture to prevent seed coat splits and cotyledon shattering.",
            sowingTargetPlantsM2 = 45
        ),
        CropType(
            id = "chickpea_pulse",
            commonName = "Chickpea (Kabuli / Desi)",
            scientificName = "Cicer arietinum",
            category = CropCategory.PULSE,
            standardLengthMinMm = 6.0,
            standardLengthMaxMm = 11.0,
            standardWidthMinMm = 5.5,
            standardWidthMaxMm = 9.5,
            optimalAspectRatio = 1.15,
            standardTgwMinGrams = 220.0,
            standardTgwMaxGrams = 450.0,
            sizeClasses = listOf("Jumbo (9-10mm)", "Medium (7-8mm)", "Small (<7mm)"),
            moistureTargetPercent = 12.0,
            millingRecoveryAdvice = "Dehulling efficiency requires 10-12% moisture. Caliber sizing directly determines market price premiums.",
            sowingTargetPlantsM2 = 35
        ),
        CropType(
            id = "sorghum",
            commonName = "Grain Sorghum",
            scientificName = "Sorghum bicolor",
            category = CropCategory.CEREAL,
            standardLengthMinMm = 3.5,
            standardLengthMaxMm = 5.0,
            standardWidthMinMm = 3.0,
            standardWidthMaxMm = 4.5,
            optimalAspectRatio = 1.15,
            standardTgwMinGrams = 24.0,
            standardTgwMaxGrams = 36.0,
            sizeClasses = listOf("Large Seed", "Medium Seed", "Small Seed"),
            moistureTargetPercent = 13.5,
            millingRecoveryAdvice = "Decortication removes pericarp tannins. Plump grains provide higher starch extraction in ethanol and feed milling.",
            sowingTargetPlantsM2 = 18
        ),
        CropType(
            id = "oats",
            commonName = "Oats (Groats / Grain)",
            scientificName = "Avena sativa",
            category = CropCategory.CEREAL,
            standardLengthMinMm = 8.0,
            standardLengthMaxMm = 13.0,
            standardWidthMinMm = 2.2,
            standardWidthMaxMm = 3.2,
            optimalAspectRatio = 3.8,
            standardTgwMinGrams = 30.0,
            standardTgwMaxGrams = 42.0,
            sizeClasses = listOf("Heavy Plump Groat", "Standard Groat", "Double/Slim Oat"),
            moistureTargetPercent = 12.5,
            millingRecoveryAdvice = "High groat percentage (>72%) desired. Dehulling impactor speed must be calibrated to grain length to prevent groat breakage.",
            sowingTargetPlantsM2 = 300
        )
    )

    fun getById(id: String): CropType {
        return crops.find { it.id == id } ?: crops.first()
    }
}
