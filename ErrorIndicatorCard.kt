package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ErrorIndicatorState
import com.example.model.LightingQuality
import com.example.model.SkewStatus
import com.example.ui.theme.SeverityGreen
import com.example.ui.theme.SeverityOrange
import com.example.ui.theme.SeverityRed
import com.example.ui.theme.SeverityYellow

@Composable
fun ErrorIndicatorCard(
    errorState: ErrorIndicatorState,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val confidenceColor = when {
        errorState.overallConfidenceScore >= 88 -> SeverityGreen
        errorState.overallConfidenceScore >= 70 -> SeverityYellow
        else -> SeverityRed
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("error_indicator_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(confidenceColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Measurement Precision & Error Margin",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${errorState.overallConfidenceScore}%",
                        color = confidenceColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Core Indicators Row (Chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Skew / Tilt
                MetricPill(
                    label = "Tilt Skew",
                    value = "${"%.1f".format(errorState.opticalSkewDegrees)}°",
                    isGood = errorState.skewStatus == SkewStatus.LEVEL,
                    modifier = Modifier.weight(1f)
                )

                // Error Margin mm
                MetricPill(
                    label = "Tolerance",
                    value = "±${"%.2f".format(errorState.calibrationMarginMm)}mm",
                    isGood = errorState.calibrationMarginMm <= 0.15f,
                    modifier = Modifier.weight(1f)
                )

                // Lighting
                MetricPill(
                    label = "Contrast",
                    value = when (errorState.lightingQuality) {
                        LightingQuality.EXCELLENT -> "94% High"
                        LightingQuality.MARGINAL -> "68% Mid"
                        LightingQuality.POOR -> "35% Low"
                    },
                    isGood = errorState.lightingQuality == LightingQuality.EXCELLENT,
                    modifier = Modifier.weight(1f)
                )
            }

            // Expandable details with agronomic tips
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "Optical Quality Checks:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (errorState.qualityAlerts.isNotEmpty()) {
                        errorState.qualityAlerts.forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = SeverityOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = alert,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Passed",
                                tint = SeverityGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Optical alignment, illumination, and scale calibration within certified agronomic tolerances.",
                                fontSize = 11.sp,
                                color = SeverityGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    isGood: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isGood) Color(0x1F2E7D32) else Color(0x1FE65100)
    val textCol = if (isGood) SeverityGreen else SeverityOrange

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textCol
            )
        }
    }
}
