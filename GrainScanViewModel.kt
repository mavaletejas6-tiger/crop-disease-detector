package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GrainRepository
import com.example.data.entity.GrainInspectionEntity
import com.example.data.entity.PlotEntity
import com.example.model.CropType
import com.example.model.CropTypeRepository
import com.example.model.DeficiencySeverity
import com.example.model.DetectedGrainItem
import com.example.model.DiseasePestCatalog
import com.example.model.DiseasePestItem
import com.example.model.DocumentCatalog
import com.example.model.DocumentCategory
import com.example.model.ErrorIndicatorState
import com.example.model.FieldDocument
import com.example.model.GrainMeasurement
import com.example.model.NutrientDeficiencyEstimator
import com.example.model.NutrientDeficiencyProfile
import com.example.model.ReferencePreset
import com.example.model.ReferenceScale
import com.example.model.WeatherOverlayData
import com.example.service.DataExporter
import com.example.service.GeminiAgronomistService
import com.example.service.DiseaseChatMessage
import com.example.service.ChatSender
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.service.GpsCoordinate
import com.example.service.ImageProcessingHelper
import com.example.service.LocationHelper
import com.example.service.WeatherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ImageSourceType {
    SAMPLE_PHOTO,
    CAMERA_PHOTO,
    GALLERY_PHOTO
}

enum class CanvasMode {
    MEASURE_GRAINS,
    CALIBRATE_SCALE
}

data class GrainScanUiState(
    val selectedCrop: CropType = CropTypeRepository.crops.first(),
    val referenceScale: ReferenceScale = ReferenceScale(presetId = "coin_one_euro", realMm = 23.25f, pixelSpan = 135.0f),
    val measurement: GrainMeasurement = GrainMeasurement(),
    val errorIndicator: ErrorIndicatorState = ErrorIndicatorState(),
    // Image & Camera States
    val imageSourceType: ImageSourceType = ImageSourceType.SAMPLE_PHOTO,
    val capturedBitmap: Bitmap? = null,
    val activeCanvasMode: CanvasMode = CanvasMode.MEASURE_GRAINS,
    val scaleReticleX: Float = 140f,
    val scaleReticleY: Float = 145f,
    val scaleReticleSpanPx: Float = 135f,
    val isScaleCalibrated: Boolean = true,
    val scaleCalibrationStatusText: String = "Calibrated: 1 Euro (5.81 px/mm)",
    val selectedDiseasePest: DiseasePestItem = DiseasePestCatalog.items.first(),
    val diseaseScaleLevel: Int = 1,
    val damagePercentage: Float = 4.5f,
    // Nutrient symptoms toggles
    val isPale: Boolean = true,
    val isShriveled: Boolean = false,
    val hasChalkiness: Boolean = true,
    val isChaffy: Boolean = false,
    val hasPurpling: Boolean = false,
    val estimatedNutrients: List<NutrientDeficiencyProfile> = emptyList(),
    // Multiplot grouping
    val selectedPlot: PlotEntity? = null,
    val showCreatePlotDialog: Boolean = false,
    // GPS Geo-tagging
    val gpsCoordinate: GpsCoordinate = GpsCoordinate(),
    val isLocating: Boolean = false,
    // Weather overlay & fast checker
    val weatherData: WeatherOverlayData = WeatherOverlayData(),
    val isWeatherLoading: Boolean = false,
    // AI Agronomist & Status
    val aiConsultationText: String = "",
    val isAiAnalyzing: Boolean = false,
    val isDiseaseChatOpen: Boolean = false,
    val diseaseChatMessages: List<DiseaseChatMessage> = listOf(
        DiseaseChatMessage(
            sender = ChatSender.AI,
            text = "Hello! I am your AI Crop Pathologist & Disease Specialist. Ask me anything about fungal/bacterial symptoms, chemical sprays, organic bio-treatments, or grain consumption safety for your crop."
        )
    ),
    val isDiseaseAiThinking: Boolean = false,
    val currentDiseaseQueryInput: String = "",
    // Live Weather Globe State
    val isGlobeWeatherOpen: Boolean = false,
    val globeSelectedLatitude: Double = 41.5868,
    val globeSelectedLongitude: Double = -93.6250,
    val globeSelectedCityName: String = "Des Moines, USA (Corn Belt)",
    val globeWeatherData: WeatherOverlayData = WeatherOverlayData(
        cityName = "Des Moines, USA (Corn Belt)",
        latitude = 41.5868,
        longitude = -93.6250,
        temperatureC = 22.4,
        relativeHumidityPercent = 58,
        rainfallMm = 1.2
    ),
    val isGlobeWeatherLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val showExportDialog: Boolean = false,
    val currentTab: Int = 0, // 0: Scanner, 1: Crop Solutions, 2: Plots & Records, 3: Weather Hub, 4: Document
    // Document Section State
    val documents: List<FieldDocument> = DocumentCatalog.getDefaultDocuments(),
    val selectedDocument: FieldDocument? = null,
    val selectedDocumentCategory: DocumentCategory = DocumentCategory.ALL,
    val documentSearchQuery: String = "",
    val isViewingDocument: Boolean = false,
    val showCreateDocumentDialog: Boolean = false,
    val showApprovalDialog: Boolean = false,
    val documentToApprove: FieldDocument? = null,
    val showQrVerificationDialog: Boolean = false,
    val verifiedDocumentForQr: FieldDocument? = null
)

class GrainScanViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = GrainRepository(database.plotDao(), database.grainInspectionDao())
    private val weatherService = WeatherService()
    private val locationHelper = LocationHelper(application)
    private val geminiService = GeminiAgronomistService()

    val plots: StateFlow<List<PlotEntity>> = repository.allPlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inspections: StateFlow<List<GrainInspectionEntity>> = repository.allInspections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(GrainScanUiState())
    val uiState: StateFlow<GrainScanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            refreshLocationAndWeather()
            recalculateNutrientDeficiencies()
            recalculateErrorState()
        }

        // Observe plots to assign default selected plot if null
        viewModelScope.launch {
            plots.collect { plotList ->
                if (_uiState.value.selectedPlot == null && plotList.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(selectedPlot = plotList.first())
                }
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(currentTab = tabIndex)
    }

    fun selectCrop(crop: CropType) {
        val cropPests = DiseasePestCatalog.getForCrop(crop.id)
        val defaultPest = cropPests.firstOrNull() ?: DiseasePestCatalog.items.first()
        _uiState.value = _uiState.value.copy(
            selectedCrop = crop,
            selectedDiseasePest = defaultPest,
            diseaseScaleLevel = defaultPest.scaleLevel,
            damagePercentage = defaultPest.damagePercentage
        )
        recalculateNutrientDeficiencies()
        recalculateErrorState()
    }

    fun selectPlot(plot: PlotEntity) {
        _uiState.value = _uiState.value.copy(selectedPlot = plot)
        // If plot has specific crop, switch to it
        val matchingCrop = CropTypeRepository.crops.find { it.id == plot.cropId }
        if (matchingCrop != null) {
            selectCrop(matchingCrop)
        }
    }

    fun createNewPlot(
        name: String,
        cropId: String,
        variety: String,
        areaHectares: Double,
        sowingDate: String,
        soilType: String,
        notes: String
    ) {
        viewModelScope.launch {
            val newPlot = PlotEntity(
                name = name.ifBlank { "Field Plot #${Random.nextInt(10, 99)}" },
                cropId = cropId,
                variety = variety.ifBlank { "Standard Variety" },
                areaHectares = if (areaHectares > 0) areaHectares else 2.0,
                targetSowingDate = sowingDate,
                soilType = soilType.ifBlank { "Clay Loam" },
                notes = notes
            )
            val newId = repository.insertPlot(newPlot)
            _uiState.value = _uiState.value.copy(
                selectedPlot = newPlot.copy(id = newId),
                showCreatePlotDialog = false,
                snackbarMessage = "Plot '${newPlot.name}' created successfully"
            )
        }
    }

    fun setCreatePlotDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showCreatePlotDialog = visible)
    }

    // --- Image, Camera & Scale Detection Controls ---
    fun setCanvasMode(mode: CanvasMode) {
        _uiState.value = _uiState.value.copy(activeCanvasMode = mode)
    }

    fun setCapturedCameraBitmap(bitmap: Bitmap) {
        val brightness = ImageProcessingHelper.calculateImageBrightness(bitmap)
        _uiState.value = _uiState.value.copy(
            capturedBitmap = bitmap,
            imageSourceType = ImageSourceType.CAMERA_PHOTO,
            activeCanvasMode = CanvasMode.CALIBRATE_SCALE, // Prompt user to calibrate scale on their photo
            isScaleCalibrated = false,
            snackbarMessage = "Photo captured! Position or auto-detect reference scale on the image."
        )
        recalculateErrorState(ambientLight = brightness)
    }

    fun setSelectedGalleryUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bitmap = ImageProcessingHelper.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                val brightness = ImageProcessingHelper.calculateImageBrightness(bitmap)
                _uiState.value = _uiState.value.copy(
                    capturedBitmap = bitmap,
                    imageSourceType = ImageSourceType.GALLERY_PHOTO,
                    activeCanvasMode = CanvasMode.CALIBRATE_SCALE,
                    isScaleCalibrated = false,
                    snackbarMessage = "Image loaded! Position or auto-detect reference scale on the image."
                )
                recalculateErrorState(ambientLight = brightness)
            } else {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Could not load selected image."
                )
            }
        }
    }

    fun loadSamplePhotoWithScale() {
        _uiState.value = _uiState.value.copy(
            capturedBitmap = null,
            imageSourceType = ImageSourceType.SAMPLE_PHOTO,
            scaleReticleX = 140f,
            scaleReticleY = 145f,
            scaleReticleSpanPx = 135f,
            isScaleCalibrated = true,
            scaleCalibrationStatusText = "Calibrated: 1 Euro (5.81 px/mm)",
            snackbarMessage = "Loaded laboratory reference photo with coin & calibration markers."
        )
        recalculateErrorState()
    }

    fun updateScaleReticle(centerX: Float, centerY: Float, spanPx: Float) {
        val boundedSpan = spanPx.coerceIn(30f, 350f)
        val currentScale = _uiState.value.referenceScale
        val newScale = currentScale.copy(pixelSpan = boundedSpan)
        _uiState.value = _uiState.value.copy(
            scaleReticleX = centerX,
            scaleReticleY = centerY,
            scaleReticleSpanPx = boundedSpan,
            referenceScale = newScale,
            isScaleCalibrated = false
        )
        recalculateErrorState()
    }

    fun autoDetectScaleFromActiveImage(canvasWidth: Float = 480f, canvasHeight: Float = 360f) {
        val state = _uiState.value
        val scale = state.referenceScale
        val result = ImageProcessingHelper.autoDetectScaleObjectOnImage(
            bitmap = state.capturedBitmap,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            isCircular = scale.isCircular,
            referenceMm = scale.realMm
        )

        val newScale = scale.copy(pixelSpan = result.spanPx)
        val ppm = newScale.pixelsPerMm
        _uiState.value = _uiState.value.copy(
            scaleReticleX = result.centerX,
            scaleReticleY = result.centerY,
            scaleReticleSpanPx = result.spanPx,
            referenceScale = newScale,
            isScaleCalibrated = true,
            scaleCalibrationStatusText = "Auto-detected: ${"%.2f".format(ppm)} px/mm (${result.description})",
            snackbarMessage = "Scale auto-detected on image! (${"%.1f".format(result.spanPx)}px = ${scale.realMm}mm -> ${"%.2f".format(ppm)} px/mm)"
        )
        recalculateErrorState()
    }

    fun confirmScaleCalibration(canvasWidth: Float = 480f, canvasHeight: Float = 360f) {
        val state = _uiState.value
        val scale = state.referenceScale
        val ppm = scale.pixelsPerMm
        _uiState.value = _uiState.value.copy(
            isScaleCalibrated = true,
            activeCanvasMode = CanvasMode.MEASURE_GRAINS,
            scaleCalibrationStatusText = "Locked scale: ${"%.2f".format(ppm)} px/mm",
            snackbarMessage = "Scale locked at ${"%.2f".format(ppm)} px/mm. Measuring grains on the photo!"
        )
        autoDetectGrainsFromActiveImage(canvasWidth, canvasHeight)
    }

    fun autoDetectGrainsFromActiveImage(canvasWidth: Float = 480f, canvasHeight: Float = 360f) {
        val state = _uiState.value
        val crop = state.selectedCrop
        val scale = state.referenceScale
        val grains = ImageProcessingHelper.segmentGrainsOnImage(
            crop = crop,
            pixelsPerMm = scale.pixelsPerMm,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            scaleCenterX = state.scaleReticleX,
            scaleCenterY = state.scaleReticleY
        )

        val avgLen = if (grains.isNotEmpty()) grains.map { it.lengthPx }.average().toFloat() else state.measurement.caliperLengthPx
        val avgWid = if (grains.isNotEmpty()) grains.map { it.widthPx }.average().toFloat() else state.measurement.caliperWidthPx

        _uiState.value = _uiState.value.copy(
            measurement = state.measurement.copy(
                detectedGrains = grains,
                caliperLengthPx = avgLen,
                caliperWidthPx = avgWid,
                isAutoDetectMode = true
            ),
            snackbarMessage = "Detected ${grains.size} grain kernels on the image."
        )
        recalculateErrorState()
    }

    fun selectGrainKernel(grain: DetectedGrainItem) {
        _uiState.value = _uiState.value.copy(
            measurement = _uiState.value.measurement.copy(
                caliperCenterX = grain.centerX,
                caliperCenterY = grain.centerY,
                caliperLengthPx = grain.lengthPx,
                caliperWidthPx = grain.widthPx,
                isAutoDetectMode = false
            ),
            snackbarMessage = "Focused on grain #${grain.id}: ${"%.2f".format(grain.lengthMm(_uiState.value.referenceScale.pixelsPerMm))} mm"
        )
    }

    fun moveCaliperToPosition(x: Float, y: Float) {
        _uiState.value = _uiState.value.copy(
            measurement = _uiState.value.measurement.copy(
                caliperCenterX = x,
                caliperCenterY = y
            )
        )
    }

    // --- Reference Scale & Caliper Controls ---
    fun selectScalePreset(preset: ReferencePreset) {
        val newScale = _uiState.value.referenceScale.copy(
            presetId = preset.id,
            realMm = preset.realMm,
            manualOverrideMm = preset.realMm
        )
        _uiState.value = _uiState.value.copy(referenceScale = newScale)
        recalculateErrorState()
    }

    fun updateCustomScaleMm(customMm: Float) {
        if (customMm > 0f) {
            val newScale = _uiState.value.referenceScale.copy(
                presetId = "custom_mm",
                realMm = customMm,
                manualOverrideMm = customMm
            )
            _uiState.value = _uiState.value.copy(referenceScale = newScale)
            recalculateErrorState()
        }
    }

    fun updateScalePixelSpan(newPixelSpan: Float) {
        if (newPixelSpan > 20f) {
            val newScale = _uiState.value.referenceScale.copy(pixelSpan = newPixelSpan)
            _uiState.value = _uiState.value.copy(
                referenceScale = newScale,
                scaleReticleSpanPx = newPixelSpan
            )
            recalculateErrorState()
        }
    }

    fun updateCaliperDimensions(lengthPx: Float, widthPx: Float) {
        val newMeasurement = _uiState.value.measurement.copy(
            caliperLengthPx = lengthPx.coerceAtLeast(10f),
            caliperWidthPx = widthPx.coerceAtLeast(5f)
        )
        _uiState.value = _uiState.value.copy(measurement = newMeasurement)
    }

    fun toggleAutoDetect(enabled: Boolean) {
        val newMeasurement = _uiState.value.measurement.copy(isAutoDetectMode = enabled)
        _uiState.value = _uiState.value.copy(measurement = newMeasurement)
        recalculateErrorState()
    }

    fun simulateNewGrainSampleScan() {
        autoDetectGrainsFromActiveImage()
    }

    // --- Error Indicator Computations ---
    fun updateTiltAngle(angleDegrees: Float) {
        recalculateErrorState(tiltAngle = angleDegrees)
    }

    private fun recalculateErrorState(
        tiltAngle: Float = _uiState.value.errorIndicator.opticalSkewDegrees,
        ambientLight: Float = (_uiState.value.errorIndicator.lightingScore * 8.5f).coerceIn(200f, 1000f)
    ) {
        val scale = _uiState.value.referenceScale
        val newState = ErrorIndicatorState.computeState(
            skewAngle = tiltAngle,
            pixelsPerMm = scale.pixelsPerMm,
            hasOverlap = false,
            hasBroken = false,
            ambientLightLevel = ambientLight
        )
        _uiState.value = _uiState.value.copy(errorIndicator = newState)
    }

    // --- Disease & Pest Scaling ---
    fun selectDiseasePest(item: DiseasePestItem) {
        _uiState.value = _uiState.value.copy(
            selectedDiseasePest = item,
            diseaseScaleLevel = item.scaleLevel,
            damagePercentage = item.damagePercentage
        )
    }

    fun updateDiseasePestScale(level: Int) {
        val pct = when (level) {
            0 -> 0.0f
            1 -> 3.0f
            2 -> 11.0f
            3 -> 20.5f
            4 -> 38.0f
            5 -> 65.0f
            else -> 10.0f
        }
        val currentItem = _uiState.value.selectedDiseasePest
        _uiState.value = _uiState.value.copy(
            diseaseScaleLevel = level,
            damagePercentage = pct,
            selectedDiseasePest = currentItem.copy(scaleLevel = level, damagePercentage = pct)
        )
    }

    // --- Nutrient Deficiency Symptom Toggles ---
    fun toggleSymptom(symptom: String, value: Boolean) {
        when (symptom) {
            "pale" -> _uiState.value = _uiState.value.copy(isPale = value)
            "shriveled" -> _uiState.value = _uiState.value.copy(isShriveled = value)
            "chalkiness" -> _uiState.value = _uiState.value.copy(hasChalkiness = value)
            "chaffy" -> _uiState.value = _uiState.value.copy(isChaffy = value)
            "purpling" -> _uiState.value = _uiState.value.copy(hasPurpling = value)
        }
        recalculateNutrientDeficiencies()
    }

    private fun recalculateNutrientDeficiencies() {
        val state = _uiState.value
        val deficiencies = NutrientDeficiencyEstimator.estimateDeficiency(
            cropId = state.selectedCrop.id,
            isPale = state.isPale,
            isShriveled = state.isShriveled,
            hasChalkiness = state.hasChalkiness,
            isChaffy = state.isChaffy,
            hasPurpling = state.hasPurpling
        )
        _uiState.value = _uiState.value.copy(estimatedNutrients = deficiencies)
    }

    // --- GPS & Fast Weather ---
    fun refreshLocationAndWeather() {
        _uiState.value = _uiState.value.copy(isLocating = true, isWeatherLoading = true)

        locationHelper.requestLocationUpdate { coord ->
            _uiState.value = _uiState.value.copy(
                gpsCoordinate = coord,
                isLocating = false
            )

            // Fast location weather checker
            viewModelScope.launch {
                val weather = weatherService.fetchCurrentWeather(coord.latitude, coord.longitude)
                _uiState.value = _uiState.value.copy(
                    weatherData = weather,
                    isWeatherLoading = false
                )
            }
        }
    }

    // --- Save Inspection to Room Database ---
    fun saveCurrentInspection(notes: String = "") {
        viewModelScope.launch {
            val state = _uiState.value
            val plot = state.selectedPlot
            val crop = state.selectedCrop
            val scale = state.referenceScale
            val measurement = state.measurement
            val error = state.errorIndicator
            val pest = state.selectedDiseasePest
            val nutrient = state.estimatedNutrients.firstOrNull()?.elementName ?: "Balanced Nutrition"
            val prescription = state.estimatedNutrients.firstOrNull()?.fertilizerRecommendation ?: "Standard maintenance."
            val gps = state.gpsCoordinate
            val weather = state.weatherData

            val lengthMm = measurement.getMeasuredLengthMm(scale)
            val widthMm = measurement.getMeasuredWidthMm(scale)
            val aspect = measurement.getAspectRatio(scale)
            val tgw = measurement.getEstimatedTgwGrams(crop, scale)
            val classification = crop.classifyGrain(lengthMm, widthMm)

            val newInspection = GrainInspectionEntity(
                plotId = plot?.id ?: 1L,
                plotName = plot?.name ?: "General Field Plot",
                cropId = crop.id,
                cropName = crop.commonName,
                grainLengthMm = lengthMm,
                grainWidthMm = widthMm,
                aspectRatio = aspect,
                estimatedTgwGrams = tgw,
                sizeClassification = classification,
                errorMarginMm = error.calibrationMarginMm.toDouble(),
                confidencePercent = error.overallConfidenceScore,
                diseasePestName = pest.name,
                pestSeverityScale = state.diseaseScaleLevel,
                damagePercent = state.damagePercentage.toDouble(),
                nutrientDeficiency = nutrient,
                correctivePrescription = prescription,
                gpsLatitude = gps.latitude,
                gpsLongitude = gps.longitude,
                gpsAltitude = gps.altitude,
                gpsAccuracyMeters = gps.accuracyMeters,
                weatherTempC = weather.temperatureC,
                weatherHumidity = weather.relativeHumidityPercent,
                weatherWindKmh = weather.windSpeedKmh,
                weatherCondition = weather.conditionDescription,
                sampleCount = measurement.detectedGrains.size.coerceAtLeast(1),
                notes = notes
            )

            val id = repository.insertInspection(newInspection)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Saved Inspection #$id to ${newInspection.plotName}!"
            )
        }
    }

    fun deleteInspection(inspection: GrainInspectionEntity) {
        viewModelScope.launch {
            repository.deleteInspection(inspection)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Removed inspection sample #${inspection.id}"
            )
        }
    }

    // --- AI Agronomist Consultation ---
    fun consultAiAgronomist() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiAnalyzing = true)
            val state = _uiState.value
            val scale = state.referenceScale
            val length = state.measurement.getMeasuredLengthMm(scale)
            val width = state.measurement.getMeasuredWidthMm(scale)
            val pest = state.selectedDiseasePest
            val defName = state.estimatedNutrients.firstOrNull()?.elementName ?: "Normal"

            val response = geminiService.analyzeGrainAgronomy(
                cropName = state.selectedCrop.commonName,
                grainLengthMm = length,
                grainWidthMm = width,
                pestSeverityScale = state.diseaseScaleLevel,
                deficiencySuspect = defName,
                weatherTempC = state.weatherData.temperatureC,
                weatherHumidity = state.weatherData.relativeHumidityPercent
            )

            _uiState.value = _uiState.value.copy(
                isAiAnalyzing = false,
                aiConsultationText = response.diagnosticSummary
            )
        }
    }

    // --- AI Disease & Pest Chat Section ---
    fun setDiseaseChatOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isDiseaseChatOpen = open)
    }

    fun updateDiseaseQueryInput(query: String) {
        _uiState.value = _uiState.value.copy(currentDiseaseQueryInput = query)
    }

    fun sendDiseaseChatMessage(customQuery: String? = null) {
        val query = (customQuery ?: _uiState.value.currentDiseaseQueryInput).trim()
        if (query.isBlank()) return

        val userMsg = DiseaseChatMessage(
            sender = ChatSender.USER,
            text = query
        )

        val updatedList = _uiState.value.diseaseChatMessages + userMsg
        _uiState.value = _uiState.value.copy(
            diseaseChatMessages = updatedList,
            currentDiseaseQueryInput = "",
            isDiseaseAiThinking = true
        )

        viewModelScope.launch {
            val state = _uiState.value
            val aiResponse = geminiService.consultDiseaseSpecialist(
                userQuestion = query,
                cropName = state.selectedCrop.commonName,
                diseaseItem = state.selectedDiseasePest,
                diseaseScaleLevel = state.diseaseScaleLevel,
                damagePercentage = state.damagePercentage,
                weatherTempC = state.weatherData.temperatureC,
                weatherHumidity = state.weatherData.relativeHumidityPercent
            )

            val aiMsg = DiseaseChatMessage(
                sender = ChatSender.AI,
                text = aiResponse
            )

            _uiState.value = _uiState.value.copy(
                diseaseChatMessages = _uiState.value.diseaseChatMessages + aiMsg,
                isDiseaseAiThinking = false
            )
        }
    }

    fun setExportDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showExportDialog = visible)
    }

    // --- Live Weather Globe Methods ---
    fun setGlobeWeatherOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isGlobeWeatherOpen = open)
        if (open && !_uiState.value.globeWeatherData.isLiveFromNetwork) {
            val state = _uiState.value
            fetchGlobeWeather(state.globeSelectedLatitude, state.globeSelectedLongitude, state.globeSelectedCityName)
        }
    }

    fun selectGlobeCity(name: String, latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            globeSelectedCityName = name,
            globeSelectedLatitude = latitude,
            globeSelectedLongitude = longitude
        )
        fetchGlobeWeather(latitude, longitude, name)
    }

    fun updateGlobeCoordinates(latitude: Double, longitude: Double) {
        val city = weatherService.lookupNearestCity(latitude, longitude)
        _uiState.value = _uiState.value.copy(
            globeSelectedCityName = city,
            globeSelectedLatitude = latitude,
            globeSelectedLongitude = longitude
        )
        fetchGlobeWeather(latitude, longitude, city)
    }

    fun fetchGlobeWeather(latitude: Double, longitude: Double, cityName: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGlobeWeatherLoading = true)
            val data = weatherService.fetchCurrentWeatherWithCity(latitude, longitude, cityName)
            _uiState.value = _uiState.value.copy(
                isGlobeWeatherLoading = false,
                globeWeatherData = data,
                globeSelectedCityName = data.cityName
            )
        }
    }

    fun applyGlobeLocationToFieldScanner() {
        val globeData = _uiState.value.globeWeatherData
        _uiState.value = _uiState.value.copy(
            weatherData = globeData,
            gpsCoordinate = GpsCoordinate(
                latitude = globeData.latitude,
                longitude = globeData.longitude,
                isGpsLocked = true
            ),
            isGlobeWeatherOpen = false,
            snackbarMessage = "Synced weather & GPS to ${globeData.cityName}!"
        )
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    // Document Management
    fun selectDocument(doc: FieldDocument?) {
        _uiState.value = _uiState.value.copy(
            selectedDocument = doc,
            isViewingDocument = doc != null
        )
    }

    fun setDocumentCategory(category: DocumentCategory) {
        _uiState.value = _uiState.value.copy(selectedDocumentCategory = category)
    }

    fun setDocumentSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(documentSearchQuery = query)
    }

    fun setShowCreateDocumentDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateDocumentDialog = show)
    }

    fun addCustomDocument(title: String, category: DocumentCategory, content: String, author: String = "Field Inspector") {
        val newDoc = FieldDocument(
            title = title,
            category = category,
            summary = if (content.length > 120) content.take(120) + "..." else content,
            fullContent = content,
            author = author,
            tags = listOf(category.displayName, "Field Note")
        )
        val updatedList = listOf(newDoc) + _uiState.value.documents
        _uiState.value = _uiState.value.copy(
            documents = updatedList,
            showCreateDocumentDialog = false,
            selectedDocument = newDoc,
            isViewingDocument = true,
            snackbarMessage = "Document created: $title"
        )
    }

    fun generateInspectionCertificateFromScan() {
        val state = _uiState.value
        val crop = state.selectedCrop
        val scale = state.referenceScale
        val meas = state.measurement
        val lengthMm = meas.getMeasuredLengthMm(scale)
        val widthMm = meas.getMeasuredWidthMm(scale)
        val aspectRatio = meas.getAspectRatio(scale)
        val tgwGrams = meas.getEstimatedTgwGrams(crop, scale)
        val gradeClass = crop.classifyGrain(lengthMm, widthMm)
        val plotName = state.selectedPlot?.name ?: "Field Plot Alpha"
        val plotVariety = state.selectedPlot?.variety ?: "Certified Foundation Stock"
        val weather = state.weatherData
        val disease = state.selectedDiseasePest
        val certId = "CERT-GRAIN-${(10000..99999).random()}"
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

        val content = """
================================================================================
           OFFICIAL GRAIN QUALITY INSPECTION CERTIFICATE
================================================================================
Certificate ID: $certId
Issue Date: $timestamp
Issuing Station: AI Mobile Caliper Laboratory
Inspection Protocol: USDA-FGIS / ISO 9001 Agri-Assay Standards

1. FIELD & PLOT IDENTIFICATION
   - Target Crop: ${crop.commonName} (${crop.scientificName})
   - Field / Plot Name: $plotName
   - Variety / Cultivar: $plotVariety
   - Soil Classification: ${state.selectedPlot?.soilType ?: "Alluvial Loam"}
   - GPS Location: ${"%.4f".format(state.gpsCoordinate.latitude)}° N, ${"%.4f".format(state.gpsCoordinate.longitude)}° E
   - Weather Station Sync: ${weather.cityName} (${"%.1f".format(weather.temperatureC)}°C, ${weather.relativeHumidityPercent}% RH, ${"%.1f".format(weather.rainfallMm)} mm rain)

2. OPTICAL CALIPER MEASUREMENTS (SAMPLE AVERAGE)
   - Kernel Length: ${"%.2f".format(lengthMm)} mm
   - Kernel Width: ${"%.2f".format(widthMm)} mm
   - Length / Width Aspect Ratio: ${"%.2f".format(aspectRatio)}
   - Estimated Thousand Grain Weight (TGW): ${"%.1f".format(tgwGrams)} grams
   - Optical Caliper Scale: ${state.scaleCalibrationStatusText}
   - Sample Size: ${if (meas.isAutoDetectMode) "${meas.detectedGrains.size} kernels" else "Single Caliper"}

3. PATHOLOGY & DISEASE SEVERITY
   - Primary Pathogen / Pest: ${disease.name} (${disease.scientificName})
   - Symptom Scale: Grade ${state.diseaseScaleLevel} / 5 (${disease.severityLabel})
   - Surface Damage Coverage: ${"%.1f".format(state.damagePercentage)}%
   - Recommended IPM Action: ${disease.chemicalSolution}

4. AGRONOMIC CONFORMITY VERDICT
   - Overall Quality Class: $gradeClass
   - Target Safe Moisture: ${"%.1f".format(crop.moistureTargetPercent)}%
   - Milling Recovery Guidance: ${crop.millingRecoveryAdvice}
   - Certification Status: VERIFIED & COMPLIANT FOR COMMERCIAL TRADE
   - Inspector Digital Signature: Certified AI Digital Seal [#$certId]
================================================================================
        """.trimIndent()

        val certDoc = FieldDocument(
            id = certId,
            title = "Quality Certificate: ${crop.commonName} ($plotName)",
            category = DocumentCategory.CERTIFICATES,
            referenceCode = certId,
            author = "AI Mobile Caliper Laboratory",
            summary = "Official Quality Certificate for ${crop.commonName}. Length: ${"%.2f".format(lengthMm)} mm, Grade: $gradeClass, Plot: $plotName.",
            fullContent = content,
            isOfficialCertificate = true,
            tags = listOf(crop.commonName, "Certified", "Inspection", plotName)
        )

        val updated = listOf(certDoc) + _uiState.value.documents
        _uiState.value = _uiState.value.copy(
            documents = updated,
            selectedDocument = certDoc,
            isViewingDocument = true,
            snackbarMessage = "Generated official Certificate of Inspection!"
        )
    }

    fun deleteDocument(id: String) {
        val updated = _uiState.value.documents.filter { it.id != id }
        _uiState.value = _uiState.value.copy(
            documents = updated,
            selectedDocument = if (_uiState.value.selectedDocument?.id == id) null else _uiState.value.selectedDocument,
            isViewingDocument = if (_uiState.value.selectedDocument?.id == id) false else _uiState.value.isViewingDocument,
            snackbarMessage = "Document removed"
        )
    }

    // Document Viewer Approval & QR Verification
    fun openApprovalDialog(doc: FieldDocument) {
        _uiState.value = _uiState.value.copy(
            showApprovalDialog = true,
            documentToApprove = doc
        )
    }

    fun closeApprovalDialog() {
        _uiState.value = _uiState.value.copy(
            showApprovalDialog = false,
            documentToApprove = null
        )
    }

    fun approveDocument(
        docId: String,
        approverName: String,
        approverRole: String,
        notes: String = "All dimensions and statutory standards inspected and confirmed."
    ) {
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val doc = _uiState.value.documents.find { it.id == docId } ?: return
        val token = FieldDocument.generateVerificationToken(doc.id, doc.referenceCode, approverName, timestamp)
        val qrPayload = FieldDocument.generateQrPayload(doc, approverName, approverRole, timestamp, token)

        val approvedDoc = doc.copy(
            isApproved = true,
            approvedBy = approverName,
            approvedRole = approverRole,
            approvedTimestamp = timestamp,
            verificationToken = token,
            verificationQrPayload = qrPayload,
            approvalNotes = notes
        )

        val updatedDocs = _uiState.value.documents.map {
            if (it.id == docId) approvedDoc else it
        }

        _uiState.value = _uiState.value.copy(
            documents = updatedDocs,
            selectedDocument = if (_uiState.value.selectedDocument?.id == docId) approvedDoc else _uiState.value.selectedDocument,
            showApprovalDialog = false,
            documentToApprove = null,
            showQrVerificationDialog = true,
            verifiedDocumentForQr = approvedDoc,
            snackbarMessage = "Document approved by $approverName! Verification QR generated."
        )
    }

    fun openQrVerificationDialog(doc: FieldDocument) {
        _uiState.value = _uiState.value.copy(
            showQrVerificationDialog = true,
            verifiedDocumentForQr = doc
        )
    }

    fun closeQrVerificationDialog() {
        _uiState.value = _uiState.value.copy(
            showQrVerificationDialog = false,
            verifiedDocumentForQr = null
        )
    }
}
