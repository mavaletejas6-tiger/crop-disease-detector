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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CropTypeRepository
import com.example.model.DeficiencySeverity
import com.example.model.DiseasePestCatalog
import com.example.ui.theme.FieldGreenPrimary
import com.example.ui.theme.SeverityGreen
import com.example.ui.theme.SeverityOrange
import com.example.ui.theme.SeverityRed
import com.example.ui.theme.SeverityYellow
import com.example.ui.theme.WheatGold
import com.example.viewmodel.GrainScanUiState
import com.example.viewmodel.GrainScanViewModel

@Composable
fun CropSolutionsScreen(
    uiState: GrainScanUiState,
    viewModel: GrainScanViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val crop = uiState.selectedCrop
    val scale = uiState.referenceScale
    val measurement = uiState.measurement
    val estimatedTgw = measurement.getEstimatedTgwGrams(crop, scale)
    val sowingRate = crop.calculateSowingRateKgHa(estimatedTgw)
    val pest = uiState.selectedDiseasePest

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 90.dp)
    ) {
        Text(
            text = "Crop Solutions & Diagnostics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Tailored grading standards, disease/pest scaling, and nutrient deficiency estimator.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Crop Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CropTypeRepository.crops.forEach { item ->
                FilterChip(
                    selected = item.id == crop.id,
                    onClick = { viewModel.selectCrop(item) },
                    label = { Text(item.commonName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FieldGreenPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("crop_solution_chip_${item.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Crop Type Solution Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("crop_solution_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = crop.commonName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = crop.scientificName,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${crop.moistureTargetPercent}% Target Moisture",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Standard Dimension Benchmarks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BenchmarkItem("Std Length", "${crop.standardLengthMinMm}-${crop.standardLengthMaxMm} mm")
                    BenchmarkItem("Std Width", "${crop.standardWidthMinMm}-${crop.standardWidthMaxMm} mm")
                    BenchmarkItem("Optimal Ratio", "${crop.optimalAspectRatio}")
                    BenchmarkItem("Std TGW", "${crop.standardTgwMinGrams.toInt()}-${crop.standardTgwMaxGrams.toInt()} g")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Milling & Processing Solutions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = FieldGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Milling & Post-Harvest Solution",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = crop.millingRecoveryAdvice,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calculated Sowing Rate
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1F1E5E3A))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Calibrated Sowing Seed Rate",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${"%.1f".format(sowingRate)} kg/ha (${"%.1f".format(sowingRate * 0.892)} lbs/acre)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FieldGreenPrimary
                        )
                    }
                    Text(
                        text = "Based on ${"%.1f".format(estimatedTgw)}g TGW",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Disease and Pest Scaling Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("disease_pest_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = if (pest.isThresholdExceeded) SeverityRed else SeverityOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Disease & Pest Scaling (0-5)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (pest.isThresholdExceeded) {
                        Surface(
                            color = Color(0x33C62828),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = SeverityRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Threshold Exceeded",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeverityRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pest selector for current crop
                val availablePests = DiseasePestCatalog.getForCrop(crop.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availablePests.forEach { item ->
                        FilterChip(
                            selected = item.id == pest.id,
                            onClick = { viewModel.selectDiseasePest(item) },
                            label = { Text(item.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD32F2F),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scale Selector 0 to 5
                Text(
                    text = "Standard Severity Scale: ${pest.severityLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (0..5).forEach { scaleNum ->
                        val isSelected = uiState.diseaseScaleLevel == scaleNum
                        val scaleColor = when (scaleNum) {
                            0 -> SeverityGreen
                            1 -> Color(0xFF66BB6A)
                            2 -> SeverityYellow
                            3 -> SeverityOrange
                            4 -> SeverityRed
                            else -> Color(0xFF880E4F)
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) scaleColor else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.updateDiseasePestScale(scaleNum) }
                                .testTag("pest_scale_button_$scaleNum"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$scaleNum",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Damage Progress Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Estimated Crop Damage",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${"%.1f".format(uiState.damagePercentage)}% (Eco. Threshold: ${pest.economicThresholdPercent}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pest.isThresholdExceeded) SeverityRed else SeverityGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (uiState.damagePercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (pest.isThresholdExceeded) SeverityRed else SeverityGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Symptoms & Treatments
                Text(
                    text = "Identified Symptoms: ${pest.symptoms}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Prescription Boxes
                TreatmentRow("Biological / Organic", pest.organicSolution, SeverityGreen)
                Spacer(modifier = Modifier.height(6.dp))
                TreatmentRow("Chemical IPM Solution", pest.chemicalSolution, SeverityOrange)
                Spacer(modifier = Modifier.height(6.dp))
                TreatmentRow("Cultural Sanitation", pest.culturalManagement, MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.setDiseaseChatOpen(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("talk_about_disease_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3821)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF80E27E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Talk to AI About ${pest.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Nutrient Deficiency Estimator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("nutrient_deficiency_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = FieldGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nutrient Deficiency Estimator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select observable grain & foliar symptom indicators:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Symptom toggle chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SymptomChip("Pale Kernels (N)", uiState.isPale) { viewModel.toggleSymptom("pale", it) }
                    SymptomChip("Shriveled Grain (N)", uiState.isShriveled) { viewModel.toggleSymptom("shriveled", it) }
                    SymptomChip("Chalky Spots (Zn)", uiState.hasChalkiness) { viewModel.toggleSymptom("chalkiness", it) }
                    SymptomChip("Chaffy Lodging (K)", uiState.isChaffy) { viewModel.toggleSymptom("chaffy", it) }
                    SymptomChip("Purpling Veins (P)", uiState.hasPurpling) { viewModel.toggleSymptom("purpling", it) }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Estimated Deficiencies List
                uiState.estimatedNutrients.forEach { profile ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = profile.elementName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FieldGreenPrimary
                                )
                                if (profile.estimatedDeficitKgHa > 0) {
                                    Text(
                                        text = "Deficit: ~${profile.estimatedDeficitKgHa.toInt()} kg/ha",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeverityOrange
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kernel Impact: ${profile.kernelSymptom}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Corrective Dose: ${profile.fertilizerRecommendation}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FieldGreenPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchmarkItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SymptomChip(label: String, selected: Boolean, onToggle: (Boolean) -> Unit) {
    FilterChip(
        selected = selected,
        onClick = { onToggle(!selected) },
        label = { Text(label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = FieldGreenPrimary,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun TreatmentRow(type: String, advice: String, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.1f))
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor)
                .padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(type, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(advice, fontSize = 11.sp, lineHeight = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
