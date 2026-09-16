package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.GrainInspectionEntity
import com.example.data.entity.PlotEntity
import com.example.model.CropTypeRepository
import com.example.service.DataExporter
import com.example.ui.theme.FieldGreenPrimary
import com.example.ui.theme.SeverityGreen
import com.example.ui.theme.WheatGold
import com.example.viewmodel.GrainScanUiState
import com.example.viewmodel.GrainScanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlotsScreen(
    uiState: GrainScanUiState,
    viewModel: GrainScanViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val plots by viewModel.plots.collectAsState()
    val inspections by viewModel.inspections.collectAsState()

    var showAllPlots by remember { mutableStateOf(false) }

    val filteredInspections = if (showAllPlots || uiState.selectedPlot == null) {
        inspections
    } else {
        inspections.filter { it.plotId == uiState.selectedPlot.id }
    }

    // New Plot Dialog State
    if (uiState.showCreatePlotDialog) {
        CreatePlotDialog(
            onDismiss = { viewModel.setCreatePlotDialogVisible(false) },
            onCreate = { name, cropId, variety, area, sowing, soil, notes ->
                viewModel.createNewPlot(name, cropId, variety, area, sowing, soil, notes)
            }
        )
    }

    // Export Data Dialog
    if (uiState.showExportDialog) {
        ExportPreviewDialog(
            inspections = inspections,
            onDismiss = { viewModel.setExportDialogVisible(false) },
            onConfirmShare = {
                val csv = DataExporter.generateCsv(inspections)
                val summary = DataExporter.generateAgronomySummary(inspections)
                DataExporter.shareExport(context, csv, summary)
                viewModel.setExportDialogVisible(false)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Multiplot Grouping & Audit",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Manage research plots, sample logs, and field export data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Export Data Top Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_data_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Field Agronomy Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${inspections.size} inspections logged across ${plots.size} plots with GPS & weather stamps.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = { viewModel.setExportDialogVisible(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary),
                        modifier = Modifier.testTag("export_data_button")
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontSize = 12.sp)
                    }
                }
            }
        }

        // Plot Selection & New Plot Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Agricultural Field Plots",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = { viewModel.setCreatePlotDialogVisible(true) },
                    modifier = Modifier.testTag("add_plot_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Plot", fontSize = 12.sp)
                }
            }
        }

        // Multiplot horizontal selector chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showAllPlots,
                    onClick = { showAllPlots = true },
                    label = { Text("All Plots (${inspections.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FieldGreenPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                plots.forEach { plot ->
                    val isSelected = !showAllPlots && uiState.selectedPlot?.id == plot.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            showAllPlots = false
                            viewModel.selectPlot(plot)
                        },
                        label = { Text(plot.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FieldGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("plot_chip_${plot.id}")
                    )
                }
            }
        }

        // Current Plot Details Card
        if (!showAllPlots && uiState.selectedPlot != null) {
            val p = uiState.selectedPlot
            val plotSamples = inspections.filter { it.plotId == p.id }
            val avgLen = if (plotSamples.isNotEmpty()) plotSamples.map { it.grainLengthMm }.average() else 0.0
            val avgWid = if (plotSamples.isNotEmpty()) plotSamples.map { it.grainWidthMm }.average() else 0.0

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_plot_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Variety: ${p.variety} • Soil: ${p.soilType}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${p.areaHectares} ha (${(p.areaHectares * 2.471).toInt()} ac)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Samples: ${plotSamples.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (avgLen > 0) "Avg Length: ${"%.2f".format(avgLen)} mm" else "No scans yet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FieldGreenPrimary
                            )
                            Text(
                                text = if (avgWid > 0) "Avg Width: ${"%.2f".format(avgWid)} mm" else "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Inspections Count Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inspection Records (${filteredInspections.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Inspections List
        if (filteredInspections.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No inspection records logged for this plot yet.\nUse the Scanner screen to measure and save samples.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            items(filteredInspections, key = { it.id }) { item ->
                InspectionItemCard(
                    inspection = item,
                    onDelete = { viewModel.deleteInspection(item) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun InspectionItemCard(
    inspection: GrainInspectionEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inspection_card_${inspection.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${inspection.cropName} • ${inspection.sizeClassification}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${inspection.plotName} • ${dateFormat.format(Date(inspection.timestamp))}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete sample",
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dimensions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "L: ${"%.2f".format(inspection.grainLengthMm)}mm",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "W: ${"%.2f".format(inspection.grainWidthMm)}mm",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ratio: ${"%.2f".format(inspection.aspectRatio)}",
                    fontSize = 12.sp
                )
                Text(
                    text = "TGW: ${"%.1f".format(inspection.estimatedTgwGrams)}g",
                    fontSize = 12.sp,
                    color = FieldGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // GPS & Weather Geotags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${"%.4f".format(inspection.gpsLatitude)}, ${"%.4f".format(inspection.gpsLongitude)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${"%.1f".format(inspection.weatherTempC)}°C, ${inspection.weatherHumidity}% RH",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatePlotDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, cropId: String, variety: String, area: Double, sowing: String, soil: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cropId by remember { mutableStateOf(CropTypeRepository.crops.first().id) }
    var variety by remember { mutableStateOf("") }
    var areaText by remember { mutableStateOf("3.0") }
    var sowingDate by remember { mutableStateOf("2026-06-15") }
    var soilType by remember { mutableStateOf("Clay Loam") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Multiplot Group") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plot Name (e.g. West Basin 4)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Seed Variety (e.g. Basmati-386)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = areaText,
                    onValueChange = { areaText = it },
                    label = { Text("Area in Hectares") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text("Soil Type") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val area = areaText.toDoubleOrNull() ?: 2.0
                    onCreate(name, cropId, variety, area, sowingDate, soilType, "")
                }
            ) {
                Text("Create Plot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ExportPreviewDialog(
    inspections: List<GrainInspectionEntity>,
    onDismiss: () -> Unit,
    onConfirmShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Agronomy Dataset") },
        text = {
            Column {
                Text(
                    text = "Ready to export ${inspections.size} inspection records in standard RFC 4180 CSV format and structured agronomy field audit report.\n\nIncludes:\n• High-precision grain dimensions (mm) & TGW (g)\n• Pest/disease severity scale (0-5)\n• Nutrient deficiency estimation\n• GPS geotags (latitude, longitude, altitude)\n• Weather overlay parameters (temperature, humidity, wind).",
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmShare,
                colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share / Save File")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
