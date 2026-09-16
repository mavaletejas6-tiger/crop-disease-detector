package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SprayingStatus
import com.example.model.WeatherOverlayData
import com.example.ui.theme.SeverityGreen
import com.example.ui.theme.SeverityOrange
import com.example.ui.theme.SeverityRed
import com.example.ui.theme.SkyWeatherBlue

@Composable
fun WeatherOverlayBadge(
    weather: WeatherOverlayData,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onClickDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sprayColor = when (weather.sprayingStatus) {
        SprayingStatus.OPTIMAL -> SeverityGreen
        SprayingStatus.MARGINAL -> SeverityOrange
        SprayingStatus.DO_NOT_SPRAY -> SeverityRed
    }

    Surface(
        modifier = modifier
            .testTag("weather_overlay_badge")
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClickDetail() },
        color = Color(0xCC0E1A12),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Weather icon and main metrics
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = "Temperature",
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${"%.1f".format(weather.temperatureC)}°C",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Humidity",
                    tint = SkyWeatherBlue,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${weather.relativeHumidityPercent}%",
                    color = Color.White,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = "Wind",
                    tint = Color(0xFFB0BEC5),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${"%.0f".format(weather.windSpeedKmh)}kph",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Spraying status dot & refresh
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(sprayColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (weather.sprayingStatus) {
                        SprayingStatus.OPTIMAL -> "Spray OK"
                        SprayingStatus.MARGINAL -> "Spray Caut."
                        SprayingStatus.DO_NOT_SPRAY -> "No Spray"
                    },
                    color = sprayColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(6.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh weather",
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRefresh() }
                    )
                }
            }
        }
    }
}
