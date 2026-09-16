package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class DocumentCategory(val displayName: String) {
    ALL("All"),
    CERTIFICATES("Certificates"),
    GRADING_STANDARDS("Grading Standards"),
    AGRONOMY_GUIDES("Agronomy & IPM"),
    STORAGE_PROTOCOLS("Storage & Silos"),
    CUSTOM_NOTES("Field Notes")
}

data class FieldDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: DocumentCategory,
    val dateFormatted: String = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date()),
    val summary: String,
    val fullContent: String,
    val referenceCode: String = "DOC-${(1000..9999).random()}",
    val author: String = "Agri-Inspector AI",
    val isOfficialCertificate: Boolean = false,
    val tags: List<String> = emptyList(),
    // Approval & QR Verification state
    val isApproved: Boolean = false,
    val approvedBy: String? = null,
    val approvedRole: String? = null,
    val approvedTimestamp: String? = null,
    val verificationToken: String? = null,
    val verificationQrPayload: String? = null,
    val approvalNotes: String? = null
) {
    companion object {
        fun generateVerificationToken(docId: String, referenceCode: String, approver: String, timestamp: String): String {
            val raw = "$docId::$referenceCode::$approver::$timestamp::AGRI-CERT-KEY"
            val digest = java.security.MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }.take(32).uppercase()
        }

        fun generateQrPayload(doc: FieldDocument, approver: String, role: String, timestamp: String, token: String): String {
            return """
                VERIFIED_GRAIN_DOCUMENT
                ID: ${doc.id}
                REF: ${doc.referenceCode}
                TITLE: ${doc.title}
                APPROVER: $approver ($role)
                VERIFIED_AT: $timestamp
                SIGNATURE_HASH: $token
                PORTAL: https://agri-verify.registry.org/verify?token=$token
            """.trimIndent()
        }
    }
}

object DocumentCatalog {
    fun getDefaultDocuments(): List<FieldDocument> = listOf(
        FieldDocument(
            id = "doc_usda_grading",
            title = "USDA & FAO Grain Grading & Dimension Standards",
            category = DocumentCategory.GRADING_STANDARDS,
            referenceCode = "STD-USDA-FGIS-2026",
            author = "Federal Grain Inspection & FAO Guidelines",
            summary = "Official dimensional criteria, test weight standards, and maximum allowable foreign material for Grade 1 through 5 grains.",
            tags = listOf("USDA", "FAO", "Grading", "Quality Control"),
            fullContent = """
================================================================================
           USDA & FAO GRAIN GRADING & DIMENSIONAL SPECIFICATIONS
================================================================================
Reference: USDA-FGIS 2026 / FAO Agricultural Handbook 182

1. WHEAT (TRITICUM AESTIVUM)
   - Grade 1: Minimum Test Weight 60.0 lb/bu (77.2 kg/hL). Damaged Kernels max 2.0%. Foreign Material max 0.4%.
   - Grade 2: Minimum Test Weight 58.0 lb/bu. Damaged Kernels max 4.0%. Foreign Material max 0.7%.
   - Dimensional Thresholds: Normal kernel length 6.2 - 7.5 mm; Width 3.0 - 3.8 mm; Length/Width ratio 1.85 - 2.10.
   - Shriveled / Broken Kernels: Must not exceed 3.0% for Grade 1.

2. PADDY & MILLED RICE (ORYZA SATIVA)
   - Extra Long Grain: Length >= 7.0 mm (Length/Width ratio > 3.0).
   - Long Grain: Length 6.0 - 6.99 mm (Length/Width ratio 2.5 - 3.0).
   - Medium Grain: Length 5.0 - 5.99 mm (Length/Width ratio 2.0 - 2.4).
   - Short Grain: Length < 5.0 mm (Length/Width ratio < 2.0).
   - Chalky Kernels tolerance: < 2.0% for Premium Export Grade.

3. CORN / MAIZE (ZEA MAYS)
   - Grade 1: Minimum Test Weight 56.0 lb/bu. Heat damaged max 0.1%. Total damaged max 3.0%.
   - Broken corn and foreign material (BCFM): max 2.0%.
   - Moisture ceiling for commercial receiving: 15.0%.

4. BARLEY (HORDEUM VULGARE)
   - Six-row Malting: Plump kernels (remaining on 6/64" x 3/4" sieve) min 75.0%.
   - Two-row Malting: Plump kernels min 80.0%.
   - Skinned and broken kernels max 4.0%.

5. SOYBEANS (GLYCINE MAX)
   - Grade 1: Splits max 10.0%. Total damaged kernels max 2.0%. Foreign material max 1.0%.
   - Uniform spherical diameter: 6.0 - 8.5 mm.
            """.trimIndent()
        ),
        FieldDocument(
            id = "doc_grain_storage_protocol",
            title = "Grain Moisture & Silo Aeration Protocol",
            category = DocumentCategory.STORAGE_PROTOCOLS,
            referenceCode = "SILO-AIR-402",
            author = "Post-Harvest Grain Preservation Board",
            summary = "Critical moisture equilibrium thresholds, safe storage temperatures, bin aeration cycles, and mycotoxin prevention.",
            tags = listOf("Storage", "Moisture", "Aeration", "Mycotoxin"),
            fullContent = """
================================================================================
           GRAIN MOISTURE & SILO AERATION OPERATIONAL PROTOCOL
================================================================================
Code: SILO-AIR-402 • Post-Harvest Management

1. SAFE STORAGE EQUILIBRIUM MOISTURE CONTENT (EMC):
   - Wheat: 13.0% max for 6 months; 12.0% max for > 12 months.
   - Corn / Maize: 14.5% max for winter; 13.0% max for summer hold.
   - Paddy Rice: 13.5% max.
   - Soybeans: 12.0% max.
   - Canola / Oilseeds: 8.5% max.

2. SILO TEMPERATURE GRADIENT MANAGEMENT:
   - Target bulk grain temperature: Below 15°C (59°F) to prevent weevil and grain borer reproduction.
   - Thermocouple cables must be read every 7 days. Any localized hotspot (> 3°C rise in 48 hours) mandates immediate aeration fan activation.

3. AERATION SCHEDULE & AIRFLOW RATES:
   - Cooling cycles: Maintain 0.1 to 0.2 CFM per bushel airflow.
   - Never run fans when ambient relative humidity exceeds 75% unless grain temperature exceeds ambient air temperature by 5°C.
   - Autumn Coring: Pull the center core of fines out of the bin after filling to ensure uniform airflow through the grain peak.

4. FUNGAL SPORE & MYCOTOXIN SUPPRESSION:
   - Aspergillus flavus & Aflatoxin risk increases dramatically above 14.5% moisture at 25°C - 35°C.
   - Routine moisture checks must be logged before bin discharge.
            """.trimIndent()
        ),
        FieldDocument(
            id = "doc_ipm_spray_guide",
            title = "Integrated Pest Management (IPM) & Chemical Safety Guide",
            category = DocumentCategory.AGRONOMY_GUIDES,
            referenceCode = "IPM-SAFETY-2026",
            author = "Crop Protection & Agronomy Council",
            summary = "Pre-harvest intervals (PHI), withholding periods, tank mix compatibility, and PPE requirements for cereal disease sprays.",
            tags = listOf("IPM", "Pesticides", "Fungicides", "Safety", "PHI"),
            fullContent = """
================================================================================
       INTEGRATED PEST MANAGEMENT (IPM) & CHEMICAL SAFETY DIRECTIVE
================================================================================
Standard: IPM-SAFETY-2026

1. REGISTERED FUNGICIDE GROUPS & USAGE:
   - Triazoles (FRAC 3): Tebuconazole, Propiconazole, Epoxiconazole.
     * Target: Fusarium Head Blight, Rusts, Septoria.
     * Pre-Harvest Interval (PHI): 35 days prior to harvest.
   - Strobilurins (FRAC 11): Azoxystrobin, Pyraclostrobin.
     * Target: Rice Blast, Powdery Mildew, Brown Spot.
     * Pre-Harvest Interval (PHI): 28 days.
   - Biologicals: Trichoderma harzianum, Bacillus subtilis.
     * Target: Soil-borne pathogens, seed damping-off.
     * Pre-Harvest Interval: 0 days (safe until harvest).

2. TANK-MIX COMPATIBILITY RULES:
   - Never mix organophosphates with alkaline copper formulations.
   - Order of Addition (WALES Rule):
     1. W: Wettable powders & dry granules.
     2. A: Agitate thoroughly.
     3. L: Liquid flowables & suspensions.
     4. E: Emulsifiable concentrates.
     5. S: Surfactants and adjuvants.

3. WORKER RE-ENTRY & PERSONAL PROTECTIVE EQUIPMENT (PPE):
   - Restricted Entry Interval (REI): Minimum 24 hours post-application.
   - Mandated PPE: Chemical-resistant nitrile gloves, NIOSH-approved respirator, eye goggles, and Tyvek protective suit.
            """.trimIndent()
        ),
        FieldDocument(
            id = "doc_seed_certification_rules",
            title = "Certified Seed Quality & Germination Protocol",
            category = DocumentCategory.CERTIFICATES,
            referenceCode = "ISTA-SEED-CERT-77",
            author = "International Seed Testing Association (ISTA)",
            summary = "Purity percentages, germination minimums, weed seed tolerances, and certification seal criteria for breeder, foundation, and registered seeds.",
            tags = listOf("Seed", "ISTA", "Germination", "Certification"),
            fullContent = """
================================================================================
          CERTIFIED SEED QUALITY & GERMINATION TESTING PROTOCOL
================================================================================
Standard: ISTA Rules Chapter 5 & 7

1. SEED CLASSES & MULTIPLICATION:
   - Breeder Seed (White Tag): 100% genetic purity produced directly by sponsoring plant breeder.
   - Foundation Seed (White Tag): Direct progeny of breeder seed.
   - Registered Seed (Purple Tag): Progeny of foundation seed, maintained under strict genetic isolation.
   - Certified Seed (Blue Tag): Commercial grade sold to crop producers.

2. MINIMUM STANDARDS FOR CEREAL CROPS:
   - Genetic Purity: Min 99.0% for Certified Class.
   - Physical Purity: Min 98.0% clean grain.
   - Inert Matter: Max 2.0%.
   - Other Crop Seeds: Max 0.2%.
   - Noxious Weed Seeds: ZERO tolerance.
   - Minimum Germination:
     * Wheat: 85%
     * Paddy Rice: 80%
     * Corn: 90%
     * Barley: 85%
     * Soybean: 80%

3. SEED TESTING METHODOLOGY:
   - 400-seed count rolled towel germination test conducted at 20°C for 7 days.
   - Tetrazolium (TZ) staining assay for rapid viability confirmation within 24 hours.
            """.trimIndent()
        ),
        FieldDocument(
            id = "doc_fertilizer_dosage_handbook",
            title = "Plot Soil & Foliar Fertilizer Dosage Calculation Guide",
            category = DocumentCategory.AGRONOMY_GUIDES,
            referenceCode = "AGRI-FERT-CALC",
            author = "Soil Fertility & Plant Nutrition Institute",
            summary = "Macronutrient (N-P-K) and micronutrient deficiency mitigation, soil test correlation tables, and foliar spray schedules during grain fill.",
            tags = listOf("Fertilizer", "NPK", "Soil", "Foliar"),
            fullContent = """
================================================================================
       PLOT SOIL & FOLIAR FERTILIZER DOSAGE CALCULATION HANDBOOK
================================================================================
Code: AGRI-FERT-CALC

1. CEREAL MACRONUTRIENT REQUIREMENT PER TON OF HARVEST:
   - Wheat (per 1 ton grain): 28 kg Nitrogen (N), 11 kg Phosphorus (P2O5), 24 kg Potassium (K2O).
   - Rice (per 1 ton grain): 20 kg Nitrogen, 10 kg Phosphorus, 25 kg Potassium.
   - Corn (per 1 ton grain): 25 kg Nitrogen, 10 kg Phosphorus, 22 kg Potassium.

2. FOLIAR SPRAY CORRECTION AT GRAIN FILL (MILK TO DOUGH STAGE):
   - Nitrogen Deficiency (Pale Grains): Foliar Urea 2% solution (20g/L water) sprayed at dusk.
   - Potassium Deficiency (Poor grain plumpness / shriveling): Potassium Nitrate (KNO3) 1.5% or Potassium Sulfate (SOP) 1.0%.
   - Zinc Deficiency (Stunted / chalky grain): Zinc Sulfate (ZnSO4) 0.5% with 0.25% lime neutralizing buffer.

3. APPLICATION PRECAUTIONS:
   - Do not spray foliar nutrients when leaf temperature exceeds 28°C or relative humidity drops below 40%.
   - Maintain sprayer operating pressure between 2.5 and 3.0 bar for uniform droplet coverage.
            """.trimIndent()
        )
    )
}
