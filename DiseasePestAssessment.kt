package com.example.model

data class DiseasePestItem(
    val id: String,
    val name: String,
    val scientificName: String,
    val targetCrops: List<String>,
    val scaleLevel: Int, // 0 = Clean, 1 = 1-5%, 2 = 6-15%, 3 = 16-25%, 4 = 26-50%, 5 = >50%
    val damagePercentage: Float,
    val economicThresholdPercent: Float,
    val symptoms: String,
    val organicSolution: String,
    val chemicalSolution: String,
    val culturalManagement: String
) {
    val isThresholdExceeded: Boolean
        get() = damagePercentage >= economicThresholdPercent

    val severityLabel: String
        get() = when (scaleLevel) {
            0 -> "Grade 0: None / Clean (0%)"
            1 -> "Grade 1: Trace / Low (1 - 5%)"
            2 -> "Grade 2: Mild Infection (6 - 15%)"
            3 -> "Grade 3: Moderate - Economic Threshold (16 - 25%)"
            4 -> "Grade 4: Severe Infestation (26 - 50%)"
            5 -> "Grade 5: Critical / Catastrophic (>50%)"
            else -> "Scale $scaleLevel"
        }
}

object DiseasePestCatalog {
    val items = listOf(
        DiseasePestItem(
            id = "rice_blast",
            name = "Rice Blast / Neck Blast",
            scientificName = "Magnaporthe oryzae",
            targetCrops = listOf("rice_paddy", "rice_milled"),
            scaleLevel = 2,
            damagePercentage = 12.0f,
            economicThresholdPercent = 5.0f,
            symptoms = "Spindle-shaped lesions with grayish-white centers on leaves and blackened panicle nodes resulting in unfilled chaffy grains.",
            organicSolution = "Foliar spray with Bacillus subtilis or neem oil seed extract (3%). Optimize silicon fertilization to harden cuticle.",
            chemicalSolution = "Tricyclazole 75% WP @ 0.6g/L or Azoxystrobin 18.2% + Difenoconazole 11.4% SC @ 1.0ml/L at boot leaf emergence.",
            culturalManagement = "Avoid excessive nitrogen top-dressing; maintain 5cm water depth; use blast-resistant certified cultivars."
        ),
        DiseasePestItem(
            id = "brown_spot",
            name = "Brown Spot Disease",
            scientificName = "Bipolaris oryzae",
            targetCrops = listOf("rice_paddy", "rice_milled"),
            scaleLevel = 1,
            damagePercentage = 4.5f,
            economicThresholdPercent = 10.0f,
            symptoms = "Oval brown spots with yellow halos on glumes and kernel coats; causes discolored, chalky and deformed grain kernels.",
            organicSolution = "Trichoderma viride seed treatment (10g/kg) and foliar spray of vermiwash combined with zinc sulfate.",
            chemicalSolution = "Mancozeb 75% WP @ 2g/L or Propiconazole 25% EC @ 1ml/L at panicle initiation.",
            culturalManagement = "Correct soil potash and potassium deficiency; ensure balanced basal fertilization to reduce crop susceptibility."
        ),
        DiseasePestItem(
            id = "rice_weevil",
            name = "Rice Weevil / Granary Weevil",
            scientificName = "Sitophilus oryzae",
            targetCrops = listOf("rice_paddy", "rice_milled", "wheat_bread", "corn_maize", "barley_malting"),
            scaleLevel = 3,
            damagePercentage = 18.5f,
            economicThresholdPercent = 2.0f,
            symptoms = "Circular emergence exit holes in kernels; hollowed grains and powdery frass accumulation inside storage grain bulk.",
            organicSolution = "Diatomaceous earth (food-grade) @ 1g/kg grain or dried neem leaf powder (2% w/w) in airtight hermetic bags.",
            chemicalSolution = "Aluminum phosphide fumigation tablets (3g tablet per tonne) in gas-tight grain enclosure under certified supervision.",
            culturalManagement = "Sun-dry grains below 12.0% moisture prior to storage; inspect bin seals and clear old grain residue."
        ),
        DiseasePestItem(
            id = "fusarium_head_blight",
            name = "Fusarium Head Blight (Scab / Tombstone)",
            scientificName = "Fusarium graminearum",
            targetCrops = listOf("wheat_bread", "wheat_durum", "barley_malting"),
            scaleLevel = 2,
            damagePercentage = 8.0f,
            economicThresholdPercent = 3.0f,
            symptoms = "Bleached spikelets on green heads; pink/orange sporodochia at glume bases; shriveled, chalky white 'tombstone' kernels with deoxynivalenol (DON) mycotoxin risk.",
            organicSolution = "Bio-fungicide Clonostachys rosea or Serenade ASO (Bacillus amyloliquefaciens) during anthesis.",
            chemicalSolution = "Prothioconazole + Tebuconazole @ 0.8L/ha applied strictly at flowering (Feekes 10.51).",
            culturalManagement = "Rotate wheat with broadleaf crops (avoid corn-wheat rotation); bury crop residue with deep tillage."
        ),
        DiseasePestItem(
            id = "lesser_grain_borer",
            name = "Lesser Grain Borer",
            scientificName = "Rhyzopertha dominica",
            targetCrops = listOf("wheat_bread", "barley_malting", "sorghum", "corn_maize"),
            scaleLevel = 2,
            damagePercentage = 7.5f,
            economicThresholdPercent = 2.0f,
            symptoms = "Ragged burrow holes in hard cereal kernels; distinctive sweet musty odor from larval boring dust.",
            organicSolution = "Hermetic storage bags (PICS bags) or nitrogen gas purge to create oxygen-depleted atmosphere (<2% O2).",
            chemicalSolution = "Deltamethrin grain protectant spray (0.5ppm) during bin loading, or phosphine fumigation.",
            culturalManagement = "Clean handling augers and grain elevator bins; cool grain bulk using aeration fans to below 15°C."
        ),
        DiseasePestItem(
            id = "rust_disease",
            name = "Stripe & Leaf Rust",
            scientificName = "Puccinia striiformis",
            targetCrops = listOf("wheat_bread", "wheat_durum", "barley_malting"),
            scaleLevel = 1,
            damagePercentage = 3.0f,
            economicThresholdPercent = 5.0f,
            symptoms = "Linear yellow-orange pustules aligned along leaf veins; causes severe kernel shriveling and low test weight.",
            organicSolution = "Sulfur dust (80% WP) @ 2.5kg/ha; early rogueing of volunteer wheat hosts.",
            chemicalSolution = "Pyraclostrobin or Tebuconazole 250 EC @ 1L/ha at flag leaf emergence.",
            culturalManagement = "Eradicate alternate Berberis hosts; sow early-maturing resistant grain lines."
        ),
        DiseasePestItem(
            id = "fall_armyworm",
            name = "Fall Armyworm Grain Feeding",
            scientificName = "Spodoptera frugiperda",
            targetCrops = listOf("corn_maize", "sorghum"),
            scaleLevel = 3,
            damagePercentage = 22.0f,
            economicThresholdPercent = 10.0f,
            symptoms = "Ragged holes through ear husks; feeding damage on developing corn kernels with moist sawdust-like frass.",
            organicSolution = "Bacillus thuringiensis (Bt kurstaki) spray @ 2g/L or Spinosad @ 0.3ml/L; release Trichogramma wasps.",
            chemicalSolution = "Chlorantraniliprole 18.5% SC @ 0.4ml/L targeted directly into the corn leaf whorl and ear silk.",
            culturalManagement = "Install pheromone monitoring traps; intercrop with Desmodium; practice timely field sanitation."
        )
    )

    fun getForCrop(cropId: String): List<DiseasePestItem> {
        return items.filter { it.targetCrops.contains(cropId) }
    }
}
