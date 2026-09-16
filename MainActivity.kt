package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.AiDiseaseCornerWidget
import com.example.ui.components.LiveWeatherGlobeDialog
import com.example.ui.screens.CropSolutionsScreen
import com.example.ui.screens.DocumentScreen
import com.example.ui.screens.PlotsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.WeatherHubScreen
import com.example.ui.theme.FieldGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GrainScanViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GrainScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GrainScanApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GrainScanApp(viewModel: GrainScanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request Camera and Location permissions on startup
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.refreshLocationAndWeather()
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AiDiseaseCornerWidget(
                uiState = uiState,
                viewModel = viewModel
            )
        },
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Straighten, contentDescription = "Grain Caliper Scanner") },
                    label = { Text("Scanner") },
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FieldGreenPrimary,
                        selectedTextColor = FieldGreenPrimary,
                        indicatorColor = FieldGreenPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_scanner")
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Eco, contentDescription = "Crop Solutions & IPM") },
                    label = { Text("Solutions") },
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FieldGreenPrimary,
                        selectedTextColor = FieldGreenPrimary,
                        indicatorColor = FieldGreenPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_solutions")
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Landscape, contentDescription = "Multiplot Grouping") },
                    label = { Text("Plots") },
                    selected = uiState.currentTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FieldGreenPrimary,
                        selectedTextColor = FieldGreenPrimary,
                        indicatorColor = FieldGreenPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_plots")
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.WbSunny, contentDescription = "Fast Weather & GPS") },
                    label = { Text("Weather") },
                    selected = uiState.currentTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FieldGreenPrimary,
                        selectedTextColor = FieldGreenPrimary,
                        indicatorColor = FieldGreenPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_weather")
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Description, contentDescription = "Field Documents & Standards") },
                    label = { Text("Document") },
                    selected = uiState.currentTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FieldGreenPrimary,
                        selectedTextColor = FieldGreenPrimary,
                        indicatorColor = FieldGreenPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_document")
                )
            }
        }
    ) { innerPadding ->
        when (uiState.currentTab) {
            0 -> ScannerScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToSolutions = { viewModel.selectTab(1) },
                onNavigateToWeather = { viewModel.selectTab(3) },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> CropSolutionsScreen(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> PlotsScreen(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> WeatherHubScreen(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            4 -> DocumentScreen(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }

        // Live Weather Globe Modal
        LiveWeatherGlobeDialog(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}

