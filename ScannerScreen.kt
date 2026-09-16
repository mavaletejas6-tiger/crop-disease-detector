package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.CropTypeRepository
import com.example.model.ReferenceScale
import com.example.ui.components.CaliperOverlayCanvas
import com.example.ui.components.ErrorIndicatorCard
import com.example.ui.components.WeatherOverlayBadge
import com.example.ui.theme.FieldGreenPrimary
import com.example.viewmodel.CanvasMode
import com.example.viewmodel.GrainScanUiState
import com.example.viewmodel.GrainScanViewModel

@Composable
fun ScannerScreen(
    uiState: GrainScanUiState,
    viewModel: GrainScanViewModel,
    onNavigateToSolutions: () -> Unit,
    onNavigateToWeather: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val crop = uiState.selectedCrop
    val scale = uiState.referenceScale
    val measurement = uiState.measurement

    val measuredLengthMm = measurement.getMeasuredLengthMm(scale)
    val measuredWidthMm = measurement.getMeasuredWidthMm(scale)
    val aspectRatio = measurement.getAspectRatio(scale)
    val estimatedTgw = measurement.getEstimatedTgwGrams(crop, scale)
    val sizeClassification = crop.classifyGrain(measuredLengthMm, measuredWidthMm)

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.setCapturedCameraBitmap(bitmap)
        }
    }

    // Photo Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setSelectedGalleryUri(context, uri)
        }
    }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        // Hero Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.hero_grain_banner),
                contentDescription = "Grain Inspection Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC0C1A11), Color(0xFF0C1A11))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GrainScan Optical Caliper",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xCC004D40),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { viewModel.setGlobeWeatherOpen(true) }
                                .testTag("open_weather_globe_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = "Map / Globe",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Globe",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = Color(0x99000000),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "GPS Tag",
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${"%.2f".format(uiState.gpsCoordinate.latitude)}, ${"%.2f".format(uiState.gpsCoordinate.longitude)}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                WeatherOverlayBadge(
                    weather = uiState.weatherData,
                    isLoading = uiState.isWeatherLoading,
                    onRefresh = { viewModel.refreshLocationAndWeather() },
                    onClickDetail = onNavigateToWeather
                )
            }
        }

        Column(modifier = Modifier.padding(14.dp)) {
            // Plot Selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Target Plot Group",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.selectedPlot?.name ?: "No Plot Assigned",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.setCreatePlotDialogVisible(true) },
                    modifier = Modifier.testTag("create_plot_button")
                ) {
                    Text("+ New Plot", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Crop Selector Chips
            Text(
                text = "Crop Type Solution",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CropTypeRepository.crops.forEach { cropOption ->
                    FilterChip(
                        selected = cropOption.id == crop.id,
                        onClick = { viewModel.selectCrop(cropOption) },
                        label = { Text(cropOption.commonName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FieldGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("crop_chip_${cropOption.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Camera & Image Acquisition Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("camera_capture_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary)
                ) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Take Photo", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_photo_button")
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Import Photo", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.loadSamplePhotoWithScale() },
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("sample_photo_button")
                ) {
                    Icon(imageVector = Icons.Default.Science, contentDescription = "Lab Sample", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lab Sample", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Mode Switcher Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.activeCanvasMode == CanvasMode.MEASURE_GRAINS,
                    onClick = { viewModel.setCanvasMode(CanvasMode.MEASURE_GRAINS) },
                    label = { Text("📐 Measure Grains", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FieldGreenPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_measure_grains")
                )

                FilterChip(
                    selected = uiState.activeCanvasMode == CanvasMode.CALIBRATE_SCALE,
                    onClick = { viewModel.setCanvasMode(CanvasMode.CALIBRATE_SCALE) },
                    label = { Text("🪙 Detect Scale on Image", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00897B),
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_calibrate_scale")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Measurement & Calibration Canvas
            CaliperOverlayCanvas(
                measurement = measurement,
                referenceScale = scale,
                canvasMode = uiState.activeCanvasMode,
                capturedBitmap = uiState.capturedBitmap,
                scaleReticleX = uiState.scaleReticleX,
                scaleReticleY = uiState.scaleReticleY,
                scaleReticleSpanPx = uiState.scaleReticleSpanPx,
                onCaliperChange = { len, wid ->
                    viewModel.updateCaliperDimensions(len, wid)
                },
                onReticleChange = { cx, cy, span ->
                    viewModel.updateScaleReticle(cx, cy, span)
                },
                onGrainSelected = { grain ->
                    viewModel.selectGrainKernel(grain)
                },
                onCanvasTapped = { x, y ->
                    viewModel.moveCaliperToPosition(x, y)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Controls depending on Canvas Mode
            if (uiState.activeCanvasMode == CanvasMode.CALIBRATE_SCALE) {
                // ==========================================
                // SCALE CALIBRATION ON IMAGE CONTROL CARD
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scale_calibration_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Straighten,
                                    contentDescription = null,
                                    tint = Color(0xFF00897B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Detect Grain Scale on Image",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${"%.2f".format(scale.pixelsPerMm)} px/mm",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00897B)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select your reference object placed in the photo, then auto-detect or drag the reticle to match its outer diameter/edge:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reference object preset chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReferenceScale.PRESETS.forEach { preset ->
                                FilterChip(
                                    selected = scale.presetId == preset.id,
                                    onClick = { viewModel.selectScalePreset(preset) },
                                    label = { Text(preset.label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00897B),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Slider to adjust reticle span on image
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Pixel Span: ${scale.pixelSpan.toInt()} px spanning ${scale.realMm} mm",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Slider(
                                value = scale.pixelSpan,
                                onValueChange = { viewModel.updateScalePixelSpan(it) },
                                valueRange = 40f..260f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00897B),
                                    activeTrackColor = Color(0xFF00897B)
                                ),
                                modifier = Modifier.testTag("scale_slider")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Actions: Auto-detect scale on image & Confirm scale
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.autoDetectScaleFromActiveImage() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auto_detect_scale_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                            ) {
                                Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto Detect Scale", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.confirmScaleCalibration() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("confirm_scale_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lock & Measure", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // GRAIN CALIPER ACTIONS & MEASUREMENT CARD
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.autoDetectGrainsFromActiveImage() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detect_grains_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Detect Grains", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.saveCurrentInspection() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_sample_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Sample", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time Grain Measurement Readout Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grain_metrics_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detected Grain Dimensions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = sizeClassification,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricBlock(
                                title = "Length",
                                value = "${"%.2f".format(measuredLengthMm)} mm",
                                benchmark = "${crop.standardLengthMinMm}-${crop.standardLengthMaxMm} mm",
                                modifier = Modifier.weight(1f)
                            )
                            MetricBlock(
                                title = "Width",
                                value = "${"%.2f".format(measuredWidthMm)} mm",
                                benchmark = "${crop.standardWidthMinMm}-${crop.standardWidthMaxMm} mm",
                                modifier = Modifier.weight(1f)
                            )
                            MetricBlock(
                                title = "L/W Ratio",
                                value = "%.2f".format(aspectRatio),
                                benchmark = "Opt: ${crop.optimalAspectRatio}",
                                modifier = Modifier.weight(1f)
                            )
                            MetricBlock(
                                title = "Est. TGW",
                                value = "${"%.1f".format(estimatedTgw)} g",
                                benchmark = "${crop.standardTgwMinGrams.toInt()}-${crop.standardTgwMaxGrams.toInt()} g",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Error Indicator Card
            ErrorIndicatorCard(errorState = uiState.errorIndicator)

            Spacer(modifier = Modifier.height(14.dp))

            // AI Agronomist Action Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_audit_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "AI Agronomist",
                                tint = FieldGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Agronomist Analysis",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.consultAiAgronomist() },
                            enabled = !uiState.isAiAnalyzing,
                            colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary),
                            modifier = Modifier.testTag("run_ai_audit_button")
                        ) {
                            if (uiState.isAiAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Run Audit", fontSize = 12.sp)
                            }
                        }
                    }

                    if (uiState.aiConsultationText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.aiConsultationText,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBlock(
    title: String,
    value: String,
    benchmark: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = benchmark,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
