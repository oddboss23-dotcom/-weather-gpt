package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.AlertSeverity
import com.example.data.model.CityLocation
import com.example.data.model.DisasterAlert
import com.example.service.HapticFeedbackManager
import com.example.ui.components.LocationPermissionRationaleDialog
import com.example.ui.components.TopWeatherBar
import com.example.ui.components.WeatherVoiceBottomDock
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavScreens
import com.example.ui.screens.RadarForecastScreen
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.ChatVoiceScreen
import com.example.ui.screens.ForecastScreen
import com.example.ui.screens.KrishiScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WeatherHomeScreen
import com.example.data.climate.ClimateScreen
import com.example.data.aviation.AviationScreen
import com.example.data.marine.MarineScreen
import com.example.data.smartcity.SmartCityScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanLight
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WeatherGPTTheme
import com.example.ui.viewmodel.WeatherGPTViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherGPTViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherGPTTheme {
                WeatherGPTApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherGPTApp(viewModel: WeatherGPTViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    var showCityDialog by remember { mutableStateOf(false) }
    var showLocationRationaleDialog by remember { mutableStateOf(false) }
    var activeChatQuery by remember { mutableStateOf<String?>(null) }

    val currentLocation by viewModel.currentLocation.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedMode by viewModel.selectedUserMode.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val activeRedAlert by viewModel.activeRedAlert.collectAsState()

    // Trigger differentiated vibration patterns for severe alerts, flood warnings, and general weather alerts
    LaunchedEffect(activeRedAlert?.id) {
        val alert = activeRedAlert
        if (alert != null) {
            when {
                alert.isFloodAlert -> HapticFeedbackManager.triggerFloodWarning(context, isCritical = alert.severity == AlertSeverity.RED || alert.severity == AlertSeverity.ORANGE)
                alert.severity == AlertSeverity.RED -> HapticFeedbackManager.triggerSevereAlert(context)
                alert.severity == AlertSeverity.ORANGE || alert.severity == AlertSeverity.YELLOW -> HapticFeedbackManager.triggerWeatherAlert(context, alert.severity)
            }
        }
    }

    // Accompanist Multiple Permissions state for ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) { permissionsMap ->
        val isGranted = permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            Toast.makeText(context, "Acquiring live meteorological GPS coordinates...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission required for automatic GPS detection.", Toast.LENGTH_SHORT).show()
        }
        viewModel.onLocationPermissionResult(isGranted)
    }

    val requestLocationCoordinates: () -> Unit = {
        val hasPermission = locationPermissionsState.allPermissionsGranted ||
                locationPermissionsState.permissions.any { it.status.isGranted } ||
                viewModel.hasLocationPermission()

        if (hasPermission) {
            Toast.makeText(context, "Detecting GPS location...", Toast.LENGTH_SHORT).show()
            viewModel.fetchCurrentGpsLocation()
        } else if (locationPermissionsState.shouldShowRationale) {
            showLocationRationaleDialog = true
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopWeatherBar(
                    currentLocation = "${currentLocation.name}, ${currentLocation.state}",
                    selectedLanguage = selectedLanguage,
                    selectedMode = selectedMode,
                    isRealTime = weatherData.isRealTimeConnected,
                    onLocationClick = { showCityDialog = true },
                    onGpsClick = { requestLocationCoordinates() },
                    onLanguageSelected = { viewModel.setLanguage(it) },
                    onModeSelected = { viewModel.setUserMode(it) }
                )

                // Network Status Monitoring Banner: Notifies user when offline & displays last synced timestamp
                val ruralState by viewModel.ruralOfflineState.collectAsState()
                androidx.compose.animation.AnimatedVisibility(
                    visible = !ruralState.isOnline,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    val syncTimeStr = remember(ruralState.lastSyncTimestamp) {
                        java.text.SimpleDateFormat("hh:mm a, dd MMM", java.util.Locale.getDefault()).format(java.util.Date(ruralState.lastSyncTimestamp))
                    }
                    Surface(
                        color = Color(0xFFB91C1C),
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Offline Network Status",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "You are currently offline",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Last successfully synced forecast: $syncTimeStr",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            androidx.compose.material3.TextButton(
                                onClick = { viewModel.refreshWeather() },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Text("RETRY", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            WeatherVoiceBottomDock(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route == Screen.Radar.route && currentRoute != Screen.Radar.route) {
                        HapticFeedbackManager.triggerMapViewSwitch(context)
                    }
                    activeChatQuery = null
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = viewModel
            )
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepNavyBg)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    WeatherHomeScreen(
                        viewModel = viewModel,
                        onNavigateToChatWithQuery = { query ->
                            activeChatQuery = query
                            navController.navigate(Screen.Chat.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToGisRadar = {
                            HapticFeedbackManager.triggerMapViewSwitch(context)
                            navController.navigate(Screen.Radar.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(Screen.Radar.route) {
                    RadarForecastScreen(viewModel = viewModel)
                }

                composable(Screen.Krishi.route) {
                    KrishiScreen(
                        viewModel = viewModel,
                        onNavigateToChatWithQuery = { query ->
                            activeChatQuery = query
                            navController.navigate(Screen.Chat.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.Alerts.route) {
                    AlertsScreen(viewModel = viewModel)
                }

                composable(Screen.Climate.route) {
                    ClimateScreen(
                        currentLocation = currentLocation,
                        canonicalRepo = viewModel.canonicalWeatherRepo
                    )
                }

                composable(Screen.Aviation.route) {
                    AviationScreen(
                        currentLocation = currentLocation,
                        canonicalRepo = viewModel.canonicalWeatherRepo
                    )
                }

                composable(Screen.Marine.route) {
                    MarineScreen(
                        currentLocation = currentLocation,
                        canonicalRepo = viewModel.canonicalWeatherRepo
                    )
                }

                composable(Screen.SmartCity.route) {
                    SmartCityScreen(
                        currentLocation = currentLocation,
                        canonicalRepo = viewModel.canonicalWeatherRepo
                    )
                }

                composable(Screen.Profile.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToModule = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(Screen.Chat.route) {
                    ChatVoiceScreen(
                        viewModel = viewModel,
                        initialQuery = activeChatQuery,
                        onNavigateToGisRadar = {
                            HapticFeedbackManager.triggerMapViewSwitch(context)
                            navController.navigate(Screen.Radar.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToHomeTab = { _ ->
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }

            // Heads-up Severe Weather Alert Notification Banner
            AnimatedVisibility(
                visible = activeRedAlert != null && currentRoute != Screen.Alerts.route,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(100f)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                activeRedAlert?.let { alert ->
                    SevereWeatherAlertNotificationBanner(
                        alert = alert,
                        onViewAlert = {
                            navController.navigate(Screen.Alerts.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onDismiss = {
                            viewModel.dismissDisasterMode()
                        }
                    )
                }
            }

            if (showCityDialog) {
                CitySelectionDialog(
                    cities = viewModel.availableCities,
                    onCitySelected = { city ->
                        viewModel.loadWeatherForCity(city)
                        showCityDialog = false
                    },
                    onUseGpsLocation = {
                        showCityDialog = false
                        requestLocationCoordinates()
                    },
                    onDismiss = { showCityDialog = false }
                )
            }

            if (showLocationRationaleDialog) {
                LocationPermissionRationaleDialog(
                    onGrantPermission = {
                        showLocationRationaleDialog = false
                        locationPermissionsState.launchMultiplePermissionRequest()
                    },
                    onOpenSettings = {
                        showLocationRationaleDialog = false
                        try {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open settings: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDismiss = { showLocationRationaleDialog = false },
                    isPermanentlyDenied = !locationPermissionsState.shouldShowRationale && !locationPermissionsState.allPermissionsGranted
                )
            }
        }
    }
}

@Composable
fun CitySelectionDialog(
    cities: List<CityLocation>,
    onCitySelected: (CityLocation) -> Unit,
    onUseGpsLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchFilter by remember { mutableStateOf("") }
    val filteredCities = remember(searchFilter) {
        if (searchFilter.isBlank()) cities
        else cities.filter {
            it.name.contains(searchFilter, ignoreCase = true) || it.state.contains(searchFilter, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceNavy,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "METEOROLOGICAL HUBS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            ),
                            color = ElectricCyan
                        )
                        Text(
                            text = "Select Location / Station",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Quick Live GPS Coordinates Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ElectricCyan.copy(alpha = 0.25f),
                                    CyanAccent.copy(alpha = 0.12f)
                                )
                            )
                        )
                        .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .clickable { onUseGpsLocation() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Live GPS Telemetry",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Use Current Location (GPS)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Fetch coordinates via high-precision sensors",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "GPS Detection",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                OutlinedTextField(
                    value = searchFilter,
                    onValueChange = { searchFilter = it },
                    placeholder = { Text("Search Indian cities, states...", color = TextSecondary, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCities) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(0.5.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .clickable { onCitySelected(city) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextHighlight
                                )
                                Text(
                                    text = city.state,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "${city.latitude}°N, ${city.longitude}°E",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = CyanLight
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Heads-up persistent floating notification banner for active severe weather warnings
 * (Red & Orange severity alerts with direct advisory routing and dismissal)
 */
@Composable
fun SevereWeatherAlertNotificationBanner(
    alert: DisasterAlert,
    onViewAlert: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        AlertRed.copy(alpha = 0.95f),
                        DeepNavyBg.copy(alpha = 0.95f)
                    )
                )
            )
            .border(1.5.dp, AlertRed, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Severe Alert Warning",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🚨 SEVERE WEATHER ALERT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { onViewAlert() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "VIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = AlertRed
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Notification",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

