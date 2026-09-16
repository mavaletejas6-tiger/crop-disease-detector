package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WeatherOverlayData
import com.example.service.CitySearchResult
import com.example.service.WeatherService
import com.example.ui.theme.FieldGreenPrimary
import com.example.ui.theme.SkyWeatherBlue
import com.example.viewmodel.GrainScanUiState
import com.example.viewmodel.GrainScanViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Extensive Worldwide City Location Database
data class GlobalCity(
    val name: String,
    val country: String,
    val region: String,
    val continent: String,
    val latitude: Double,
    val longitude: Double
) {
    val fullDisplayName: String get() = "$name, $country"
}

// 85+ Major Worldwide Cities and Key Agricultural Belts
val ALL_WORLD_CITIES = listOf(
    // North America
    GlobalCity("Des Moines", "USA", "Iowa Corn Belt", "North America", 41.5868, -93.6250),
    GlobalCity("Chicago", "USA", "Midwest Grain Exchange", "North America", 41.8781, -87.6298),
    GlobalCity("Kansas City", "USA", "Winter Wheat Belt", "North America", 39.0997, -94.5786),
    GlobalCity("Fargo", "USA", "Red River Valley Grain", "North America", 46.8772, -96.7898),
    GlobalCity("San Francisco", "USA", "Central Valley Agriculture", "North America", 37.7749, -122.4194),
    GlobalCity("Fresno", "USA", "San Joaquin Valley", "North America", 36.7468, -119.7726),
    GlobalCity("Dallas", "USA", "Texas Cotton & Sorghum", "North America", 32.7767, -96.7970),
    GlobalCity("New York", "USA", "East Coast Metropolis", "North America", 40.7128, -74.0060),
    GlobalCity("Winnipeg", "Canada", "Prairie Grain Capital", "North America", 49.8951, -97.1384),
    GlobalCity("Saskatoon", "Canada", "Saskatchewan Wheat Hub", "North America", 52.1332, -106.6700),
    GlobalCity("Calgary", "Canada", "Alberta Cattle & Barley", "North America", 51.0447, -114.0719),
    GlobalCity("Toronto", "Canada", "Ontario Farmland Basin", "North America", 43.6532, -79.3832),
    GlobalCity("Guadalajara", "Mexico", "Jalisco Corn & Agave", "North America", 20.6597, -103.3496),
    GlobalCity("Culiacán", "Mexico", "Sinaloa Tomato & Grain", "North America", 24.8091, -107.3940),
    GlobalCity("Mexico City", "Mexico", "Valley of Mexico", "North America", 19.4326, -99.1332),

    // Europe
    GlobalCity("Paris", "France", "Bassin Parisien Wheat Plains", "Europe", 48.8566, 2.3522),
    GlobalCity("Toulouse", "France", "Occitanie Cereals & Sunflower", "Europe", 43.6047, 1.4442),
    GlobalCity("London", "UK", "East Anglia Arable Lands", "Europe", 51.5074, -0.1278),
    GlobalCity("Cambridge", "UK", "Fenland Grain & Ag-Tech", "Europe", 52.2053, 0.1218),
    GlobalCity("Berlin", "Germany", "Brandenburg Rye & Canola", "Europe", 52.5200, 13.4050),
    GlobalCity("Munich", "Germany", "Bavarian Barley & Hops", "Europe", 48.1351, 11.5820),
    GlobalCity("Kyiv", "Ukraine", "Central Black Earth Steppe", "Europe", 50.4501, 30.5234),
    GlobalCity("Odesa", "Ukraine", "Black Sea Grain Port", "Europe", 46.4825, 30.7233),
    GlobalCity("Warsaw", "Poland", "Mazovian Rye & Wheat", "Europe", 52.2297, 21.0122),
    GlobalCity("Poznań", "Poland", "Greater Poland Cereals", "Europe", 52.4064, 16.9252),
    GlobalCity("Rome", "Italy", "Lazio Olive & Durum Wheat", "Europe", 41.9028, 12.4964),
    GlobalCity("Bologna", "Italy", "Po Valley Agro-Industrial", "Europe", 44.4949, 11.3426),
    GlobalCity("Madrid", "Spain", "Castile Grain Meseta", "Europe", 40.4168, -3.7038),
    GlobalCity("Seville", "Spain", "Andalusia Sunflower & Olive", "Europe", 37.3891, -5.9845),
    GlobalCity("Bucharest", "Romania", "Danubian Corn & Sunflower", "Europe", 44.4268, 26.1025),
    GlobalCity("Amsterdam", "Netherlands", "Greenhouse Horticulture Hub", "Europe", 52.3676, 4.9041),

    // Asia
    GlobalCity("Ludhiana", "India", "Punjab Wheat & Rice Bowl", "Asia", 30.9010, 75.8573),
    GlobalCity("New Delhi", "India", "Gangetic Plain Agriculture", "Asia", 28.6139, 77.2090),
    GlobalCity("Indore", "India", "Malwa Soybean Belt", "Asia", 22.7196, 75.8577),
    GlobalCity("Nagpur", "India", "Vidarbha Cotton & Oranges", "Asia", 21.1458, 79.0882),
    GlobalCity("Hyderabad", "India", "Deccan Seed & Millet Hub", "Asia", 17.3850, 78.4867),
    GlobalCity("Kolkata", "India", "Bengal Paddy Delta", "Asia", 22.5726, 88.3639),
    GlobalCity("Beijing", "China", "North China Wheat Plain", "Asia", 39.9042, 116.4074),
    GlobalCity("Harbin", "China", "Heilongjiang Black Soil Corn", "Asia", 45.8038, 126.5349),
    GlobalCity("Zhengzhou", "China", "Henan Wheat Center", "Asia", 34.7466, 113.6253),
    GlobalCity("Guangzhou", "China", "Pearl River Rice Basin", "Asia", 23.1291, 113.2644),
    GlobalCity("Tokyo", "Japan", "Kanto Rice Plain", "Asia", 35.6762, 139.6503),
    GlobalCity("Sapporo", "Japan", "Hokkaido Dairy & Wheat", "Asia", 43.0618, 141.3545),
    GlobalCity("Niigata", "Japan", "Koshihikari Rice Basin", "Asia", 37.9022, 139.0232),
    GlobalCity("Seoul", "South Korea", "Gyeonggi Grain Basin", "Asia", 37.5665, 126.9780),
    GlobalCity("Bangkok", "Thailand", "Chao Phraya Rice Delta", "Asia", 13.7563, 100.5018),
    GlobalCity("Hanoi", "Vietnam", "Red River Paddy Delta", "Asia", 21.0285, 105.8542),
    GlobalCity("Ho Chi Minh City", "Vietnam", "Mekong Rice Hub", "Asia", 10.8231, 106.6297),
    GlobalCity("Jakarta", "Indonesia", "Java Paddy & Palm Basin", "Asia", -6.2088, 106.8456),
    GlobalCity("Manila", "Philippines", "Central Luzon Rice Granary", "Asia", 14.5995, 120.9842),
    GlobalCity("Almaty", "Kazakhstan", "Eurasian Steppe Wheat", "Asia", 43.2220, 76.8512),
    GlobalCity("Tashkent", "Uzbekistan", "Fergana Cotton & Wheat", "Asia", 41.2995, 69.2401),
    GlobalCity("Lahore", "Pakistan", "Punjab Indus Basmati Belt", "Asia", 31.5204, 74.3587),

    // South America
    GlobalCity("São Paulo", "Brazil", "Paulista Agribusiness Hub", "South America", -23.5505, -46.6333),
    GlobalCity("Cuiabá", "Brazil", "Mato Grosso Soy Capital", "South America", -15.6014, -56.0979),
    GlobalCity("Rondonópolis", "Brazil", "Cerrado Grain Terminal", "South America", -16.4674, -54.6360),
    GlobalCity("Porto Alegre", "Brazil", "Rio Grande do Sul Rice & Soy", "South America", -30.0346, -51.2177),
    GlobalCity("Buenos Aires", "Argentina", "Pampas Grain Hub", "South America", -34.6037, -58.3816),
    GlobalCity("Rosario", "Argentina", "Parana River Grain Port", "South America", -32.9468, -60.6393),
    GlobalCity("Córdoba", "Argentina", "Central Pampas Corn & Soy", "South America", -31.4201, -64.1888),
    GlobalCity("Santiago", "Chile", "Central Valley Vineyards & Fruit", "South America", -33.4489, -70.6693),
    GlobalCity("Lima", "Peru", "Coastal Oasis Crops", "South America", -12.0464, -77.0428),
    GlobalCity("Bogotá", "Colombia", "Highland Cereals & Coffee", "South America", 4.7110, -74.0721),
    GlobalCity("Montevideo", "Uruguay", "Plata Basin Cattle & Soy", "South America", -34.9011, -56.1645),

    // Africa
    GlobalCity("Nairobi", "Kenya", "Highland Cereals & Tea", "Africa", -1.2921, 36.8219),
    GlobalCity("Eldoret", "Kenya", "Rift Valley Grain Granary", "Africa", 0.5143, 35.2698),
    GlobalCity("Cairo", "Egypt", "Nile Delta Wheat Basin", "Africa", 30.0444, 31.2357),
    GlobalCity("Alexandria", "Egypt", "Mediterranean Agro Port", "Africa", 31.2001, 29.9187),
    GlobalCity("Johannesburg", "South Africa", "Highveld Corn Belt", "Africa", -26.2041, 28.0473),
    GlobalCity("Bloemfontein", "South Africa", "Free State Maize Granary", "Africa", -29.0852, 26.1596),
    GlobalCity("Lagos", "Nigeria", "Coastal Agribusiness Terminal", "Africa", 6.5244, 3.3792),
    GlobalCity("Kano", "Nigeria", "Savannah Sorghum & Millet", "Africa", 12.0022, 8.5920),
    GlobalCity("Casablanca", "Morocco", "Chaouia Wheat Plains", "Africa", 33.5731, -7.5898),
    GlobalCity("Addis Ababa", "Ethiopia", "Highland Teff & Barley", "Africa", 9.0300, 38.7400),
    GlobalCity("Accra", "Ghana", "Cocoa & Cereal Belt", "Africa", 5.6037, -0.1870),

    // Oceania
    GlobalCity("Melbourne", "Australia", "Victoria Wheat & Barley", "Oceania", -37.8136, 144.9631),
    GlobalCity("Perth", "Australia", "Western Australia Wheatbelt", "Oceania", -31.9505, 115.8605),
    GlobalCity("Sydney", "Australia", "New South Wales Grain Hub", "Oceania", -33.8688, 151.2093),
    GlobalCity("Toowoomba", "Australia", "Darling Downs Grain Granary", "Oceania", -27.5598, 151.9507),
    GlobalCity("Adelaide", "Australia", "South Australia Barley & Wheat", "Oceania", -34.9285, 138.6007),
    GlobalCity("Christchurch", "New Zealand", "Canterbury Plains Grain", "Oceania", -43.5321, 172.6362),
    GlobalCity("Auckland", "New Zealand", "Waikato Dairy & Maize", "Oceania", -36.8485, 174.7633),

    // Middle East
    GlobalCity("Riyadh", "Saudi Arabia", "Al-Kharj Wheat & Date Oasis", "Middle East", 24.7136, 46.6753),
    GlobalCity("Ankara", "Turkey", "Anatolian Wheat Plateau", "Middle East", 39.9334, 32.8597),
    GlobalCity("Konya", "Turkey", "Konya Basin Turkey Granary", "Middle East", 37.8746, 32.4932),
    GlobalCity("Tehran", "Iran", "Alborz Foothills Cereals", "Middle East", 35.6892, 51.3890),
    GlobalCity("Baghdad", "Iraq", "Mesopotamia Tigris Wheat Valley", "Middle East", 33.3152, 44.3661),
    GlobalCity("Dubai", "UAE", "Desert Agro-Technology Hub", "Middle East", 25.2048, 55.2708)
)

val CONTINENT_FILTERS = listOf("All", "North America", "Europe", "Asia", "South America", "Africa", "Oceania", "Middle East")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveWeatherGlobeDialog(
    uiState: GrainScanUiState,
    viewModel: GrainScanViewModel
) {
    if (!uiState.isGlobeWeatherOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val weather = uiState.globeWeatherData
    val coroutineScope = rememberCoroutineScope()
    val weatherService = remember { WeatherService() }

    // Globe Rotation state (pitch and yaw in degrees)
    var globeRotX by remember { mutableFloatStateOf(uiState.globeSelectedLatitude.toFloat().coerceIn(-80f, 80f)) }
    var globeRotY by remember { mutableFloatStateOf(uiState.globeSelectedLongitude.toFloat().coerceIn(-180f, 180f)) }

    // Search and Filter State
    var searchQuery by remember { mutableStateOf("") }
    var selectedContinent by remember { mutableStateOf("All") }
    var showSearchResultsList by remember { mutableStateOf(false) }
    val onlineSearchResults = remember { mutableStateListOf<CitySearchResult>() }
    var isSearchingOnline by remember { mutableStateOf(false) }

    // Selected Pinpoint Callout Visibility
    var isPinpointCalloutVisible by remember { mutableStateOf(true) }

    // Debounced Online Search Job
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            searchJob?.cancel()
            searchJob = coroutineScope.launch {
                delay(350)
                isSearchingOnline = true
                val results = weatherService.searchCitiesOnline(searchQuery.trim())
                onlineSearchResults.clear()
                onlineSearchResults.addAll(results)
                isSearchingOnline = false
            }
        } else {
            onlineSearchResults.clear()
            isSearchingOnline = false
        }
    }

    // Filtered Local Cities
    val filteredCities = remember(searchQuery, selectedContinent) {
        ALL_WORLD_CITIES.filter { city ->
            val matchesContinent = selectedContinent == "All" || city.continent.equals(selectedContinent, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    city.name.contains(searchQuery, ignoreCase = true) ||
                    city.country.contains(searchQuery, ignoreCase = true) ||
                    city.region.contains(searchQuery, ignoreCase = true)
            matchesContinent && matchesQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.setGlobeWeatherOpen(false) },
        sheetState = sheetState,
        containerColor = Color(0xFF060D12),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("live_weather_globe_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
        ) {
            // Satellite Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFF0D47A1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Satellite Globe",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Satellite Live Weather Globe",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF00E5FF).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "SATELLITE VIEW",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF80D8FF),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tap any pinpoint to reveal city name, location & weather",
                            fontSize = 11.sp,
                            color = Color(0xFF90A4AE)
                        )
                    }
                }

                IconButton(onClick = { viewModel.setGlobeWeatherOpen(false) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar for All Cities Worldwide
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    showSearchResultsList = it.isNotBlank()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("globe_city_search_input"),
                placeholder = {
                    Text("Search any city worldwide (e.g. Paris, Tokyo, Des Moines)...", fontSize = 12.sp, color = Color(0xFF78909C))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            showSearchResultsList = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFFB0BEC5), modifier = Modifier.size(18.dp))
                        }
                    } else if (isSearchingOnline) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00E5FF), strokeWidth = 2.dp)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0C1920),
                    unfocusedContainerColor = Color(0xFF0C1920),
                    focusedIndicatorColor = Color(0xFF00E5FF),
                    unfocusedIndicatorColor = Color(0xFF263238),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Continent Quick Filter Chips
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CONTINENT_FILTERS.forEach { continent ->
                    val isSelected = selectedContinent == continent
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedContinent = continent
                            showSearchResultsList = false
                        },
                        label = {
                            Text(
                                text = continent,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFFB0BEC5)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00695C),
                            containerColor = Color(0xFF102029)
                        )
                    )
                }
            }

            // Dropdown / Search Results Panel if active
            if (showSearchResultsList && searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1E28))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        // Local Matches
                        if (filteredCities.isNotEmpty()) {
                            Text("WORLDWIDE DIRECTORY MATCHES:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF80CBC4), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                            filteredCities.take(8).forEach { city ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            globeRotX = city.latitude.toFloat().coerceIn(-80f, 80f)
                                            globeRotY = city.longitude.toFloat().coerceIn(-180f, 180f)
                                            isPinpointCalloutVisible = true
                                            searchQuery = ""
                                            showSearchResultsList = false
                                            viewModel.selectGlobeCity(city.fullDisplayName, city.latitude, city.longitude)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(city.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("${city.region} • ${city.country}", fontSize = 11.sp, color = Color(0xFF90A4AE))
                                        }
                                    }
                                    Text("${"%.1f".format(city.latitude)}°, ${"%.1f".format(city.longitude)}°", fontSize = 10.sp, color = Color(0xFF80D8FF))
                                }
                            }
                        }

                        // Online Geocoded Search Matches
                        if (onlineSearchResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("GLOBAL SATELLITE SEARCH MATCHES:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4DD0E1), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                            onlineSearchResults.forEach { res ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            globeRotX = res.latitude.toFloat().coerceIn(-80f, 80f)
                                            globeRotY = res.longitude.toFloat().coerceIn(-180f, 180f)
                                            isPinpointCalloutVisible = true
                                            searchQuery = ""
                                            showSearchResultsList = false
                                            viewModel.selectGlobeCity(res.displayName, res.latitude, res.longitude)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(res.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("${res.adminRegion}, ${res.country}", fontSize = 11.sp, color = Color(0xFF90A4AE))
                                        }
                                    }
                                    Text("${"%.2f".format(res.latitude)}°, ${"%.2f".format(res.longitude)}°", fontSize = 10.sp, color = Color(0xFFFFD54F))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Satellite View 3D Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF03080D))
                    .border(1.dp, Color(0xFF1E3A4A), RoundedCornerShape(18.dp))
                    .testTag("interactive_weather_globe_canvas"),
                contentAlignment = Alignment.Center
            ) {
                // Interactive 3D Satellite Earth Canvas with Pinpoints
                SatelliteGlobeCanvas(
                    selectedLat = uiState.globeSelectedLatitude.toFloat(),
                    selectedLon = uiState.globeSelectedLongitude.toFloat(),
                    rotX = globeRotX,
                    rotY = globeRotY,
                    rainfallMm = weather.rainfallMm,
                    cities = ALL_WORLD_CITIES,
                    onRotate = { dx, dy ->
                        globeRotY = (globeRotY + dx * 0.45f).coerceIn(-180f, 180f)
                        globeRotX = (globeRotX - dy * 0.45f).coerceIn(-80f, 80f)
                    },
                    onPinpointClicked = { city ->
                        globeRotX = city.latitude.toFloat().coerceIn(-80f, 80f)
                        globeRotY = city.longitude.toFloat().coerceIn(-180f, 180f)
                        isPinpointCalloutVisible = true
                        viewModel.selectGlobeCity(city.fullDisplayName, city.latitude, city.longitude)
                    },
                    onCoordinateTapped = { lat, lon ->
                        globeRotX = lat.toFloat().coerceIn(-80f, 80f)
                        globeRotY = lon.toFloat().coerceIn(-180f, 180f)
                        isPinpointCalloutVisible = true
                        viewModel.updateGlobeCoordinates(lat, lon)
                    }
                )

                // Top Satellite HUD Telemetry Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xCC001824)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LEO SAT 780KM • SENSOR ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80D8FF)
                            )
                        }
                    }
                }

                // Loading Spinner
                if (uiState.isGlobeWeatherLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF00E5FF),
                        strokeWidth = 3.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(24.dp)
                    )
                }

                // Drag and Tap Hint at Bottom
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC051017),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "Drag to orbit satellite • Tap any glowing pin point for weather",
                        fontSize = 10.sp,
                        color = Color(0xFF80D8FF),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PINPOINT POPUP / HUD CARD (Shows Location, Weather and City Name on Pinpoint Click)
            AnimatedVisibility(
                visible = isPinpointCalloutVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1F29)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .testTag("pinpoint_weather_callout_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        // Title & Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E5FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Pin Point",
                                        tint = Color(0xFF041017),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "PINPOINT TELEMETRY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            color = Color(0xFF004D40),
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(
                                                text = "LOCKED",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF80CBC4),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = weather.cityName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.fetchGlobeWeather(
                                        uiState.globeSelectedLatitude,
                                        uiState.globeSelectedLongitude,
                                        uiState.globeSelectedCityName
                                    )
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF162D3B))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Location Coordinates & Condition Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF07141C))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Explore, contentDescription = null, tint = Color(0xFF80D8FF), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Location: ${weather.formattedLatitude}, ${weather.formattedLongitude}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFCFD8DC)
                                )
                            }

                            Text(
                                text = weather.conditionDescription,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFD54F)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pinpoint Key Metrics Grid (Temp, Rain, Humidity, Coldness)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PinpointStatBadge(
                                label = "Temperature",
                                value = "${"%.1f".format(weather.temperatureC)}°C",
                                extra = "Feels ${"%.1f".format(weather.apparentTemperatureC)}°C",
                                icon = Icons.Default.Thermostat,
                                color = Color(0xFFFFB74D),
                                modifier = Modifier.weight(1f)
                            )

                            PinpointStatBadge(
                                label = "Rainfall",
                                value = "${"%.1f".format(weather.rainfallMm)} mm",
                                extra = if (weather.rainfallMm > 0.1) "Active Rain" else "Dry Surface",
                                icon = Icons.Default.WaterDrop,
                                color = Color(0xFF29B6F6),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PinpointStatBadge(
                                label = "Relative Humidity",
                                value = "${weather.relativeHumidityPercent}% RH",
                                extra = "Vapor Saturation",
                                icon = Icons.Default.Cloud,
                                color = Color(0xFF81C784),
                                modifier = Modifier.weight(1f)
                            )

                            PinpointStatBadge(
                                label = "Coldness Level",
                                value = weather.coldnessLevel,
                                extra = "Chill: ${"%.1f".format(weather.windChillC)}°C",
                                icon = Icons.Default.AcUnit,
                                color = if (weather.temperatureC <= 5.0) Color(0xFF00E5FF) else Color(0xFFA7FFEB),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Button to Sync Pinpoint to Field Inspection
                        Button(
                            onClick = { viewModel.applyGlobeLocationToFieldScanner() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("apply_globe_weather_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Apply ${weather.cityName} to Field Inspection",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick City Selector Horizontal Carousel
            Text(
                text = "Key Worldwide Agricultural Hubs (Tap to Pinpoint):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF80CBC4)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredCities.take(20).forEach { city ->
                    val isSelected = uiState.globeSelectedCityName.contains(city.name, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            globeRotX = city.latitude.toFloat().coerceIn(-80f, 80f)
                            globeRotY = city.longitude.toFloat().coerceIn(-180f, 180f)
                            isPinpointCalloutVisible = true
                            viewModel.selectGlobeCity(city.fullDisplayName, city.latitude, city.longitude)
                        },
                        label = {
                            Text(
                                text = "${city.name} (${city.country})",
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFFCFD8DC)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF90A4AE)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF004D40),
                            containerColor = Color(0xFF13222B)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PinpointStatBadge(
    label: String,
    value: String,
    extra: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF09161F),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(extra, fontSize = 9.sp, color = Color(0xFF90A4AE), maxLines = 1)
        }
    }
}

// 3D Spherical Coordinate Helper
private data class Vec3(val x: Float, val y: Float, val z: Float)

// Satellite View Earth Canvas with 3D Orthographic Projection & Pinpoint Beacons
@Composable
private fun SatelliteGlobeCanvas(
    selectedLat: Float,
    selectedLon: Float,
    rotX: Float,
    rotY: Float,
    rainfallMm: Double,
    cities: List<GlobalCity>,
    onRotate: (Float, Float) -> Unit,
    onPinpointClicked: (GlobalCity) -> Unit,
    onCoordinateTapped: (Double, Double) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "satellite_motion")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloudDrift"
    )

    // Precomputed stars for space background
    val stars = remember {
        val list = mutableListOf<Triple<Float, Float, Float>>() // xFrac, yFrac, alpha
        val rnd = java.util.Random(42)
        for (i in 0 until 55) {
            list.add(Triple(rnd.nextFloat(), rnd.nextFloat(), 0.3f + rnd.nextFloat() * 0.7f))
        }
        list
    }

    // 3D projection function from lat/lon to 2D screen coordinate on rotated globe
    fun projectLatLon(latDeg: Float, lonDeg: Float, cx: Float, cy: Float, radius: Float): Pair<Offset, Boolean> {
        val latRad = (latDeg * PI / 180.0).toFloat()
        val lonRad = (lonDeg * PI / 180.0).toFloat()
        val rotXRad = (rotX * PI / 180.0).toFloat()
        val rotYRad = (rotY * PI / 180.0).toFloat()

        // 3D sphere point (Z pointing forward toward viewer)
        val deltaLon = lonRad - rotYRad
        val x0 = cos(latRad) * sin(deltaLon)
        val y0 = sin(latRad)
        val z0 = cos(latRad) * cos(deltaLon)

        // Rotate by rotX (tilt up/down)
        val y1 = y0 * cos(rotXRad) - z0 * sin(rotXRad)
        val z1 = y0 * sin(rotXRad) + z0 * cos(rotXRad)
        val x1 = x0

        val screenX = cx + x1 * radius
        val screenY = cy - y1 * radius
        val isVisible = z1 > 0f // Visible if on front hemisphere

        return Pair(Offset(screenX, screenY), isVisible)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onRotate(dragAmount.x, dragAmount.y)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radius = Math.min(size.width, size.height) * 0.44f

                    // Check if tap hit any visible city pinpoint
                    var clickedCity: GlobalCity? = null
                    var closestDist = Float.MAX_VALUE

                    for (city in cities) {
                        val (pos, isVisible) = projectLatLon(city.latitude.toFloat(), city.longitude.toFloat(), cx, cy, radius)
                        if (isVisible) {
                            val dx = tapOffset.x - pos.x
                            val dy = tapOffset.y - pos.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist < 32f && dist < closestDist) {
                                closestDist = dist
                                clickedCity = city
                            }
                        }
                    }

                    if (clickedCity != null) {
                        onPinpointClicked(clickedCity)
                    } else {
                        // Check if tap was on the globe sphere surface
                        val dx = (tapOffset.x - cx) / radius
                        val dy = (tapOffset.y - cy) / radius
                        val distSq = dx * dx + dy * dy
                        if (distSq <= 1.0f) {
                            val z = sqrt(1.0f - distSq)
                            // Invert 3D projection to latitude and longitude
                            val rotXRad = (rotX * PI / 180.0)
                            val y0 = -dy * cos(rotXRad) + z * sin(rotXRad)
                            val z0 = -dy * -sin(rotXRad) + z * cos(rotXRad)
                            val latRad = kotlin.math.asin(y0.coerceIn(-1.0, 1.0))
                            val lonRad = kotlin.math.atan2(dx.toDouble(), z0)
                            val targetLat = (latRad * 180.0 / PI).coerceIn(-85.0, 85.0)
                            val targetLon = ((lonRad * 180.0 / PI) + rotY).let {
                                var l = it % 360.0
                                if (l > 180.0) l -= 360.0
                                if (l < -180.0) l += 360.0
                                l
                            }
                            onCoordinateTapped(targetLat, targetLon)
                        }
                    }
                }
            }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = Math.min(size.width, size.height) * 0.44f

        // 1. Deep Cosmic Starfield Background
        stars.forEach { (xFrac, yFrac, alpha) ->
            drawCircle(
                color = Color.White.copy(alpha = alpha * (0.6f + 0.4f * sin(radarPulse * 2 * PI.toFloat()))),
                radius = 1.2f,
                center = Offset(xFrac * size.width, yFrac * size.height)
            )
        }

        // 2. Satellite Outer Orbital Ring & Telemetry HUD
        drawCircle(
            color = Color(0x1F00E5FF),
            radius = radius * 1.30f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f))
        )

        // 3. Atmosphere Limb Glow (Signature Blue Curvature Dispersion)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x4400E5FF), Color(0x220288D1), Color(0x00000000)),
                center = Offset(cx, cy),
                radius = radius * 1.22f
            ),
            radius = radius * 1.22f,
            center = Offset(cx, cy)
        )

        // 4. Satellite Ocean Base Sphere (Deep Oceanic Waters + Shading)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0F3E66), // Deep ocean blue
                    Color(0xFF0A2A47),
                    Color(0xFF041221)  // Shaded limb
                ),
                center = Offset(cx - radius * 0.35f, cy - radius * 0.35f),
                radius = radius * 1.2f
            ),
            radius = radius,
            center = Offset(cx, cy)
        )

        // 5. Shallow Continental Shelves & Coastal Azure Waters
        drawSatelliteContinents(cx, cy, radius, rotX, rotY, cloudDrift)

        // 6. Latitude Parallels (Equator, Tropics)
        val parallels = listOf(-60f, -30f, 0f, 30f, 60f)
        parallels.forEach { lat ->
            val latRad = (lat * PI / 180f).toFloat()
            val rotXRad = (rotX * PI / 180f).toFloat()
            val yCenter = cy - (sin(latRad) * cos(rotXRad) * radius)
            val rWidth = cos(latRad) * radius
            val rHeight = radius * 0.22f * cos(latRad) * cos(rotXRad)

            if (rWidth > 2f) {
                drawOval(
                    color = if (lat == 0f) Color(0x4400E5FF) else Color(0x1800E5FF),
                    topLeft = Offset(cx - rWidth, yCenter - rHeight),
                    size = Size(rWidth * 2f, rHeight * 2f),
                    style = Stroke(
                        width = if (lat == 0f) 1.5f else 0.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
                    )
                )
            }
        }

        // 7. Satellite Cloud Swirl Bands (Animated Atmospheric Weather Systems)
        val cloudAlphas = listOf(0.18f, 0.25f, 0.15f)
        listOf(20f, -15f, 45f).forEachIndexed { idx, cLat ->
            val driftOffset = (cloudDrift * (idx + 1) * 0.5f) % 360f
            val (cloudPos, isVis) = projectLatLon(cLat, driftOffset, cx, cy, radius)
            if (isVis) {
                drawCircle(
                    color = Color.White.copy(alpha = cloudAlphas[idx % cloudAlphas.size]),
                    radius = radius * 0.35f,
                    center = cloudPos
                )
            }
        }

        // 8. Day/Night Terminator Shading & City Night Lights
        drawTerminatorAndNightLights(cx, cy, radius, rotX, rotY)

        // 9. VISIBLE CITY PIN POINTS ON THE SATELLITE GLOBE
        // Render visible city pinpoints across the front hemisphere
        cities.forEach { city ->
            val (pinPos, isVisible) = projectLatLon(city.latitude.toFloat(), city.longitude.toFloat(), cx, cy, radius)
            if (isVisible) {
                val isSelectedCity = Math.abs(city.latitude - selectedLat) < 0.2 && Math.abs(city.longitude - selectedLon) < 0.2

                if (!isSelectedCity) {
                    // Micro glowing pinpoint for worldwide cities
                    drawCircle(
                        color = Color(0x5500E5FF),
                        radius = 4.5f,
                        center = pinPos
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 2.5f,
                        center = pinPos
                    )
                }
            }
        }

        // 10. SELECTED ACTIVE PINPOINT RETICLE & PULSING BEACON
        val (targetPos, isTargetVisible) = projectLatLon(selectedLat, selectedLon, cx, cy, radius)
        if (isTargetVisible) {
            // Sonar radar pulse expanding ring
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = (1f - radarPulse) * 0.85f),
                radius = 8f + radarPulse * 26f,
                center = targetPos,
                style = Stroke(width = 2.5f)
            )

            // Outer Reticle Ring
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 10f,
                center = targetPos,
                style = Stroke(width = 2f)
            )

            // Solid Beacon Pin Center
            drawCircle(
                color = Color(0xFFFFD54F), // Amber gold pin point
                radius = 5.5f,
                center = targetPos
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = targetPos
            )

            // Pin leader line rising up from point
            drawLine(
                color = Color(0xFF00E5FF),
                start = targetPos,
                end = Offset(targetPos.x + 18f, targetPos.y - 20f),
                strokeWidth = 2f
            )

            // Leader indicator dot
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 4f,
                center = Offset(targetPos.x + 18f, targetPos.y - 20f)
            )
        }

        // 11. Precipitation Radar Rings (if raining at selected pinpoint)
        if (rainfallMm > 0.05 && isTargetVisible) {
            drawCircle(
                color = Color(0x6629B6F6),
                radius = 18f + radarPulse * 32f,
                center = targetPos,
                style = Stroke(
                    width = 3.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), radarPulse * 16f)
                )
            )
        }

        // 12. Globe Specular Rim Shading
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x00000000), Color(0x4400E5FF)),
                center = Offset(cx, cy),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 3.5f)
        )
    }
}

// Satellite Continental Landmasses with Realistic Terrain Coloring
private fun DrawScope.drawSatelliteContinents(
    cx: Float,
    cy: Float,
    radius: Float,
    rotX: Float,
    rotY: Float,
    cloudDrift: Float
) {
    // 3D projection function inside DrawScope
    fun project(latDeg: Float, lonDeg: Float): Pair<Offset, Boolean> {
        val latRad = (latDeg * PI / 180.0).toFloat()
        val lonRad = (lonDeg * PI / 180.0).toFloat()
        val rotXRad = (rotX * PI / 180.0).toFloat()
        val rotYRad = (rotY * PI / 180.0).toFloat()

        val deltaLon = lonRad - rotYRad
        val x0 = cos(latRad) * sin(deltaLon)
        val y0 = sin(latRad)
        val z0 = cos(latRad) * cos(deltaLon)

        val y1 = y0 * cos(rotXRad) - z0 * sin(rotXRad)
        val z1 = y0 * sin(rotXRad) + z0 * cos(rotXRad)
        val x1 = x0

        val screenX = cx + x1 * radius
        val screenY = cy - y1 * radius
        return Pair(Offset(screenX, screenY), z1 > -0.1f)
    }

    // Realistic Continent Landmass Clusters (Anchor Lat/Lon, Size Factor, Land Color, Shelf Color)
    data class LandRegion(val lat: Float, val lon: Float, val scale: Float, val color: Color, val shelfColor: Color)

    val satelliteRegions = listOf(
        // North America
        LandRegion(50f, -100f, 0.38f, Color(0xFF2E6B34), Color(0xFF00838F)), // Canada/US Plains
        LandRegion(38f, -95f, 0.32f, Color(0xFF43793B), Color(0xFF00ACC1)),  // US Corn Belt
        LandRegion(32f, -110f, 0.22f, Color(0xFF9E783B), Color(0xFF0097A7)), // US Southwest / Deserts
        LandRegion(24f, -102f, 0.20f, Color(0xFF5A7836), Color(0xFF00ACC1)), // Mexico

        // South America
        LandRegion(-4f, -62f, 0.35f, Color(0xFF1B5E20), Color(0xFF00838F)),  // Amazon Rainforest
        LandRegion(-16f, -52f, 0.30f, Color(0xFF386B28), Color(0xFF00ACC1)), // Cerrado Soy Belt
        LandRegion(-34f, -64f, 0.28f, Color(0xFF557A32), Color(0xFF0097A7)), // Argentina Pampas
        LandRegion(-22f, -68f, 0.15f, Color(0xFF8D6E63), Color(0xFF00838F)), // Andes Mountain Ridge

        // Europe
        LandRegion(48f, 12f, 0.26f, Color(0xFF33691E), Color(0xFF00ACC1)),   // Central Europe Plains
        LandRegion(55f, -3f, 0.18f, Color(0xFF2E7D32), Color(0xFF00838F)),   // British Isles
        LandRegion(40f, -4f, 0.20f, Color(0xFF795548), Color(0xFF0097A7)),   // Iberian Peninsula
        LandRegion(52f, 32f, 0.32f, Color(0xFF3B6E2C), Color(0xFF00ACC1)),   // Eastern European Steppes
        LandRegion(62f, 15f, 0.24f, Color(0xFF1B5E20), Color(0xFF00838F)),   // Scandinavia

        // Africa
        LandRegion(25f, 18f, 0.42f, Color(0xFFB8860B), Color(0xFF00838F)),   // Sahara Desert Sand
        LandRegion(12f, 20f, 0.36f, Color(0xFF827717), Color(0xFF00ACC1)),   // Sahel Savannah
        LandRegion(0f, 22f, 0.35f, Color(0xFF1B5E20), Color(0xFF00838F)),    // Congo Rainforest Basin
        LandRegion(-26f, 26f, 0.28f, Color(0xFF558B2F), Color(0xFF0097A7)),  // South Africa Highveld

        // Asia
        LandRegion(35f, 105f, 0.42f, Color(0xFF33691E), Color(0xFF00838F)),  // China River Valleys
        LandRegion(42f, 90f, 0.28f, Color(0xFFB8860B), Color(0xFF00ACC1)),   // Gobi Desert
        LandRegion(22f, 78f, 0.36f, Color(0xFF2E7D32), Color(0xFF00ACC1)),   // Indian Subcontinent
        LandRegion(60f, 95f, 0.48f, Color(0xFF1B5E20), Color(0xFF00838F)),   // Siberian Taiga
        LandRegion(15f, 102f, 0.24f, Color(0xFF1B5E20), Color(0xFF00ACC1)),  // Indochina / Thailand
        LandRegion(36f, 138f, 0.16f, Color(0xFF2E7D32), Color(0xFF00838F)),  // Japan Archipelago

        // Australia & Oceania
        LandRegion(-24f, 134f, 0.38f, Color(0xFFC06A3B), Color(0xFF00838F)), // Australian Red Outback
        LandRegion(-34f, 145f, 0.24f, Color(0xFF558B2F), Color(0xFF00ACC1)), // Murray-Darling Grain
        LandRegion(-42f, 172f, 0.15f, Color(0xFF2E7D32), Color(0xFF00838F)), // New Zealand

        // Polar Ice Sheets
        LandRegion(76f, -40f, 0.24f, Color(0xFFECEFF1), Color(0xFF80DEEA)),  // Greenland Ice Sheet
        LandRegion(-80f, 0f, 0.45f, Color(0xFFF5F5F5), Color(0xFF80DEEA))    // Antarctica Ice Cap
    )

    satelliteRegions.forEach { region ->
        val (pos, isVis) = project(region.lat, region.lon)
        if (isVis) {
            val patchRadius = radius * region.scale

            // Shallow turquoise coastal continental shelf water
            drawCircle(
                color = region.shelfColor.copy(alpha = 0.4f),
                radius = patchRadius * 1.15f,
                center = pos
            )

            // Main Landmass Terrain
            drawCircle(
                color = region.color.copy(alpha = 0.85f),
                radius = patchRadius,
                center = pos
            )

            // Inner elevation highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = patchRadius * 0.5f,
                center = Offset(pos.x - patchRadius * 0.2f, pos.y - patchRadius * 0.2f)
            )
        }
    }
}

// Satellite Day/Night Terminator & Sparkling Amber City Lights on the Night Side
private fun DrawScope.drawTerminatorAndNightLights(
    cx: Float,
    cy: Float,
    radius: Float,
    rotX: Float,
    rotY: Float
) {
    // Night Shadow Gradient across the dark side (sun coming from top-left)
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
                Color(0x3300050A),
                Color(0xAA010408)
            ),
            start = Offset(cx - radius * 0.8f, cy - radius * 0.8f),
            end = Offset(cx + radius * 0.8f, cy + radius * 0.8f)
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    // Precomputed night city light clusters on populated continents
    val nightLights = listOf(
        Pair(40f, -74f),  // US East Coast
        Pair(34f, -118f), // US West Coast
        Pair(48f, 2f),    // Western Europe
        Pair(28f, 77f),   // India
        Pair(31f, 121f),  // East China
        Pair(35f, 139f)   // Japan
    )

    nightLights.forEach { (lat, lon) ->
        val latRad = (lat * PI / 180.0).toFloat()
        val lonRad = (lon * PI / 180.0).toFloat()
        val rotXRad = (rotX * PI / 180.0).toFloat()
        val rotYRad = (rotY * PI / 180.0).toFloat()

        val deltaLon = lonRad - rotYRad
        val x0 = cos(latRad) * sin(deltaLon)
        val y0 = sin(latRad)
        val z0 = cos(latRad) * cos(deltaLon)

        val y1 = y0 * cos(rotXRad) - z0 * sin(rotXRad)
        val z1 = y0 * sin(rotXRad) + z0 * cos(rotXRad)
        val x1 = x0

        // If on the front hemisphere and in the darker quadrant
        if (z1 > 0f && (x1 > 0.1f || y1 < -0.1f)) {
            val px = cx + x1 * radius
            val py = cy - y1 * radius

            drawCircle(
                color = Color(0x88FFB300),
                radius = 3.5f,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 1.8f,
                center = Offset(px, py)
            )
        }
    }
}
