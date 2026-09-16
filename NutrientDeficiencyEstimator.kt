package com.example.model

enum class DeficiencySeverity {
    NONE,
    MILD,
    MODERATE,
    SEVERE
}

data class NutrientDeficiencyProfile(
    val elementCode: String,
    val elementName: String,
    val kernelSymptom: String,
    val canopySymptom: String,
    val severity: DeficiencySeverity,
    val estimatedDeficitKgHa: Double,
    val fertilizerRecommendation: String,
    val applicationMethod: String,
    val timingRecommendation: String
)

object NutrientDeficiencyEstimator {
    val profiles = listOf(
        NutrientDeficiencyProfile(
            elementCode = "N",
            elementName = "Nitrogen Deficiency",
            kernelSymptom = "Pinched, small grains with low protein content; dull yellowish-pale appearance and reduced thousand grain weight (TGW).",
            canopySymptom = "General chlorosis of older bottom leaves progressing upwards; stunted tillers and spindly stems.",
            severity = DeficiencySeverity.MODERATE,
            estimatedDeficitKgHa = 45.0,
            fertilizerRecommendation = "Urea (46% N) @ 60-80 kg/ha or Ammonium Nitrate split application (50% basal, 50% panicle initiation).",
            applicationMethod = "Soil top-dressing followed by light irrigation, or 2% foliar Urea spray during boot stage.",
            timingRecommendation = "Immediate application at active tillering / panicle initiation; avoid late heading application."
        ),
        NutrientDeficiencyProfile(
            elementCode = "P",
            elementName = "Phosphorus Deficiency",
            kernelSymptom = "Severely delayed grain filling and poor maturity synchronization; small, hard kernels with reduced starch endosperm.",
            canopySymptom = "Dark green foliage with distinct reddish-purple anthocyanin pigmentation along leaf sheaths and main veins.",
            severity = DeficiencySeverity.MILD,
            estimatedDeficitKgHa = 30.0,
            fertilizerRecommendation = "DAP (Diammonium Phosphate 18-46-0) @ 65 kg/ha or Single Super Phosphate (SSP) @ 150 kg/ha.",
            applicationMethod = "Sub-surface band placement near root zone (2-3 inches deep).",
            timingRecommendation = "Best applied as basal dressing at sowing; top-dress with water-soluble 0-52-34 if mid-season."
        ),
        NutrientDeficiencyProfile(
            elementCode = "K",
            elementName = "Potassium Deficiency",
            kernelSymptom = "Chaffy, lightweight and unevenly filled kernels; weak husk clasping and high susceptibility to grain lodging.",
            canopySymptom = "Marginal leaf scorch (firing) starting from leaf tips and margins on older leaves, curling downwards.",
            severity = DeficiencySeverity.MODERATE,
            estimatedDeficitKgHa = 40.0,
            fertilizerRecommendation = "MOP (Muriate of Potash / KCl 0-0-60) @ 50 kg/ha or Potassium Sulfate (SOP) @ 60 kg/ha.",
            applicationMethod = "Broadcast and incorporate into soil, or 1.5% Potassium Nitrate (13-0-45) foliar spray.",
            timingRecommendation = "Apply 50% basal + 50% at panicle emergence to enhance carbohydrate translocation to grain."
        ),
        NutrientDeficiencyProfile(
            elementCode = "Zn",
            elementName = "Zinc Deficiency (Khaira Disease)",
            kernelSymptom = "Chalky white opaque spots in grain belly; distorted kernel tips, sterility, and brittle pericarp during milling.",
            canopySymptom = "Rusty brown blotches on middle leaves; bleached leaf bases, stunted internodes (rosetting).",
            severity = DeficiencySeverity.SEVERE,
            estimatedDeficitKgHa = 15.0,
            fertilizerRecommendation = "Zinc Sulfate Heptahydrate (ZnSO4 21%) @ 25 kg/ha soil application or Cheated Zn (12% EDTA) foliar.",
            applicationMethod = "Basal broadcast on soil or foliar spray (0.5% ZnSO4 + 0.25% lime) 2-3 times at 10-day intervals.",
            timingRecommendation = "Apply immediately upon visual symptom detection at 20-30 days after transplanting/emergence."
        ),
        NutrientDeficiencyProfile(
            elementCode = "S",
            elementName = "Sulfur Deficiency",
            kernelSymptom = "Low amino acid / gluten quality; flour vitreousness drops and kernels appear slightly translucent but undersized.",
            canopySymptom = "Uniform chlorosis of youngest leaves first (unlike N deficiency which shows on bottom leaves first).",
            severity = DeficiencySeverity.MILD,
            estimatedDeficitKgHa = 20.0,
            fertilizerRecommendation = "Ammonium Sulfate (21-0-0-24S) @ 60 kg/ha or Agricultural Gypsum @ 150 kg/ha.",
            applicationMethod = "Broadcasting prior to rain or irrigation; foliar elemental sulfur wettable powder.",
            timingRecommendation = "Early vegetative stage to stimulate nitrogen uptake and protein synthesis."
        ),
        NutrientDeficiencyProfile(
            elementCode = "B",
            elementName = "Boron Deficiency (Floret Sterility)",
            kernelSymptom = "Empty spikelets, cracked grain coats, and poor pollination filling leading to blighted panicles.",
            canopySymptom = "Thickened, brittle leaves with irregular chlorotic margins; aborted terminal growth points.",
            severity = DeficiencySeverity.MODERATE,
            estimatedDeficitKgHa = 5.0,
            fertilizerRecommendation = "Solubor (20% B) @ 1.2 kg/ha or Borax (11% B) @ 10 kg/ha soil application.",
            applicationMethod = "Foliar spray of Solubor @ 1g/L directly during pre-flowering and boot leaf stage.",
            timingRecommendation = "Strictly before flower anthesis; late application cannot rescue pollinated blank kernels."
        )
    )

    fun estimateDeficiency(
        cropId: String,
        isPale: Boolean,
        isShriveled: Boolean,
        hasChalkiness: Boolean,
        isChaffy: Boolean,
        hasPurpling: Boolean
    ): List<NutrientDeficiencyProfile> {
        val detected = mutableListOf<NutrientDeficiencyProfile>()

        if (isPale || isShriveled) {
            profiles.find { it.elementCode == "N" }?.let { detected.add(it) }
        }
        if (hasPurpling) {
            profiles.find { it.elementCode == "P" }?.let { detected.add(it) }
        }
        if (isChaffy) {
            profiles.find { it.elementCode == "K" }?.let { detected.add(it) }
        }
        if (hasChalkiness) {
            profiles.find { it.elementCode == "Zn" }?.let { detected.add(it) }
        }

        if (detected.isEmpty()) {
            // Default healthy or mild maintenance profile
            detected.add(
                NutrientDeficiencyProfile(
                    elementCode = "Optimal",
                    elementName = "Balanced Nutrition",
                    kernelSymptom = "Normal plump kernel with uniform translucent endosperm and optimal TGW.",
                    canopySymptom = "Vibrant deep green foliage without interveinal chlorosis or marginal necrosis.",
                    severity = DeficiencySeverity.NONE,
                    estimatedDeficitKgHa = 0.0,
                    fertilizerRecommendation = "Maintain balanced NPK maintenance dosage aligned with harvest removal rate.",
                    applicationMethod = "Standard split application according to crop schedule.",
                    timingRecommendation = "Follow standard agro-advisory for current phenological stage."
                )
            )
        }

        return detected
    }
}
