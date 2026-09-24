package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Water
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 4-TAB NAVIGATION STRUCTURE WITH PROMINENT CENTER GLOWING VOICE FAB
 * 1. Weather
 * 2. Radar & GIS
 * 3. Krishi (Agro-Meteorology)
 * 4. Safety & Alerts
 */
sealed class Screen(
    val route: String,
    val label: String,
    val navTitle: String,
    val icon: ImageVector
) {
    object Home : Screen(
        route = "home",
        label = "Weather",
        navTitle = "WEATHER",
        icon = Icons.Default.Thunderstorm
    )

    object Radar : Screen(
        route = "radar",
        label = "Radar",
        navTitle = "RADAR & GIS",
        icon = Icons.Default.Map
    )

    object Krishi : Screen(
        route = "krishi",
        label = "Krishi",
        navTitle = "KRISHI SALAH",
        icon = Icons.Default.Agriculture
    )

    object Alerts : Screen(
        route = "alerts",
        label = "Alerts",
        navTitle = "SAFETY & ALERTS",
        icon = Icons.Default.Emergency
    )

    object Climate : Screen(
        route = "climate",
        label = "Climate",
        navTitle = "CLIMATE ANALYSIS",
        icon = Icons.Default.Timeline
    )

    object Aviation : Screen(
        route = "aviation",
        label = "Aviation",
        navTitle = "AVIATION WEATHER",
        icon = Icons.Default.AirplanemodeActive
    )

    object Marine : Screen(
        route = "marine",
        label = "Marine",
        navTitle = "MARINE WEATHER",
        icon = Icons.Default.Water
    )

    object SmartCity : Screen(
        route = "smart_city",
        label = "Smart City",
        navTitle = "URBAN WEATHER",
        icon = Icons.Default.LocationCity
    )

    object Profile : Screen(
        route = "profile",
        label = "Profile",
        navTitle = "DIAGNOSTICS & SETUP",
        icon = Icons.Default.Person
    )

    object Chat : Screen(
        route = "chat",
        label = "AI Assistant",
        navTitle = "AI CHAT",
        icon = Icons.Default.Forum
    )
}

val leftNavScreens = listOf(
    Screen.Home,
    Screen.Radar
)

val rightNavScreens = listOf(
    Screen.Krishi,
    Screen.Alerts
)

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Radar,
    Screen.Krishi,
    Screen.Alerts
)
