package com.verdy.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = Screen.Dashboard.route,
        labelRes = com.verdy.R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    object Plants : BottomNavItem(
        route = Screen.PlantList.route,
        labelRes = com.verdy.R.string.nav_plants,
        selectedIcon = Icons.Filled.Grass,
        unselectedIcon = Icons.Outlined.Grass
    )
    object Calendar : BottomNavItem(
        route = Screen.Calendar.route,
        labelRes = com.verdy.R.string.nav_calendar,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )
    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        labelRes = com.verdy.R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val items = listOf(Home, Plants, Calendar, Settings)
    }
}
