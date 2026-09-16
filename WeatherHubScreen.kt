package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HarvestDryingCondition
import com.example.model.SprayingStatus
import com.example.ui.theme.FieldGreenPrimary
import com.example.ui.theme.SeverityGreen
import com.example.ui.theme.SeverityOrange
import com.example.ui.theme.SeverityRed
import com.example.ui.theme.SkyWeatherBlue
import com.example.viewmodel.GrainScanUiState
import com.example.viewmodel.GrainScanViewModel

@Composable
fun WeatherHubScreen(
    uiState: GrainScanUiState,
    viewModel: GrainScanViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val weather = uiState.weatherData
    val gps = uiState.gpsCoordinate

    val sprayColor = when (weather.sprayingStatus) {
        SprayingStatus.OPTIMAL -> SeverityGreen
        SprayingStatus.MARGINAL -> SeverityOrange
        SprayingStatus.DO_NOT_SPRAY -> SeverityRed
    }

    val dryingColor = when (weather.dryingCondition) {
        HarvestDryingCondition.EXCELLENT -> SeverityGreen
        HarvestDryingCondition.FAVORABLE -> SeverityOrange
        HarvestDryingCondition.POOR_HIGH_HUMIDITY -> SeverityRed
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 90.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Fast Location Weather",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Hyperlocal agro-meteorology & GPS geotagging engine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { viewModel.refreshLocationAndWeather() },
                enabled = !uiState.isWeatherLoading && !uiState.isLocating,
                colors = ButtonDefaults.buttonColors(containerColor = FieldGreenPrimary),
                modifier = Modifier.testTag("refresh_weather_button")
            ) {
                if (uiState.isWeatherLoading || uiState.isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Weather Globe Button
        Button(
            onClick = { viewModel.setGlobeWeatherOpen(true) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_weather_globe_hub_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "Map / Globe",
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Open Live Weather Globe (3D Orbit & Coordinates)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Weather Station Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("weather_station_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E15))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = weather.locationName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = weather.conditionDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA5D6A7)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Temperature Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${"%.1f".format(weather.temperatureC)}",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "°C",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB74D),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Feels like ${"%.1f".format(weather.apparentTemperatureC)}°C (${"%.1f".format(weather.temperatureF)}°F)",
                            fontSize = 12.sp,
                            color = Color(0xFFCFD8DC)
                        )
                        Text(
                            text = if (weather.isLiveFromNetwork) "● Live Satellite/Radar Feed" else "● Field Offline Sensor",
                            fontSize = 10.sp,
                            color = if (weather.isLiveFromNetwork) SeverityGreen else SeverityOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meteorological Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherMetricTile(
                        icon = Icons.Default.WaterDrop,
                        label = "Relative Humidity",
                        value = "${weather.relativeHumidityPercent}%",
                        accentColor = SkyWeatherBlue
                    )
                    WeatherMetricTile(
                        icon = Icons.Default.Air,
                        label = "Wind Velocity",
                        value = "${"%.1f".format(weather.windSpeedKmh)} km/h",
                        accentColor = Color(0xFFB0BEC5)
                    )
                    WeatherMetricTile(
                        icon = Icons.Default.Navigation,
                        label = "Wind Heading",
                        value = "${weather.windDirectionDegrees}°",
                        accentColor = Color(0xFF81C784),
                        iconRotation = weather.windDirectionDegrees.toFloat()
                    )
                    WeatherMetricTile(
                        icon = Icons.Default.Compress,
                        label = "Baro Pressure",
                        value = "${weather.surfacePressureHpa.toInt()} hPa",
                        accentColor = Color(0xFFFFD54F)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rainfall & Coldness Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF132B20),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = SkyWeatherBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Rainfall", fontSize = 9.sp, color = Color(0xFF80CBC4), fontWeight = FontWeight.Bold)
                                Text("${"%.1f".format(weather.rainfallMm)} mm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFF132B20),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = null,
                                tint = Color(0xFF80D8FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Coldness / Chill", fontSize = 9.sp, color = Color(0xFF80CBC4), fontWeight = FontWeight.Bold)
                                Text("${weather.coldnessLevel} (${"%.1f".format(weather.windChillC)}°C)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Agro Spraying Advisory Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("spraying_advisory_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agricultural Spraying Window",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = sprayColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (weather.sprayingStatus) {
                                SprayingStatus.OPTIMAL -> "OPTIMAL"
                                SprayingStatus.MARGINAL -> "MARGINAL"
                                SprayingStatus.DO_NOT_SPRAY -> "DO NOT SPRAY"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = sprayColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = weather.sprayingStatusAdvice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Post-Harvest Grain Drying Advisor Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drying_advisory_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Harvest & Field Drying Advisor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = dryingColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (weather.dryingCondition) {
                                HarvestDryingCondition.EXCELLENT -> "RAPID DRYING"
                                HarvestDryingCondition.FAVORABLE -> "FAVORABLE"
                                HarvestDryingCondition.POOR_HIGH_HUMIDITY -> "HIGH HUMIDITY"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = dryingColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = weather.dryingAdvice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // GPS Geotagging Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gps_geotag_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = FieldGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GPS Geotagging Precision",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = if (gps.isGpsLocked) "Locked (±${"%.1f".format(gps.accuracyMeters)}m)" else "Acquiring Fix",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gps.isGpsLocked) SeverityGreen else SeverityOrange
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Latitude", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.5f".format(gps.latitude)}° N", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Longitude", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.5f".format(gps.longitude)}° W", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Altitude", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.1f".format(gps.altitude)} m ASL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    iconRotation: Float = 0f
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier
                .size(18.dp)
                .rotate(iconRotation)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 8.sp, color = Color(0xFFB0BEC5))
    }
}
