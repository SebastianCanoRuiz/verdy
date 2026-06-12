package com.verdy.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.verdy.presentation.screen.calendar.CalendarScreen
import com.verdy.presentation.screen.dashboard.DashboardScreen
import com.verdy.presentation.screen.plants.detail.PlantDetailScreen
import com.verdy.presentation.screen.plants.form.PlantFormScreen
import com.verdy.presentation.screen.plants.list.PlantListScreen
import com.verdy.presentation.screen.plants.reminder.ReminderManagerScreen
import com.verdy.presentation.screen.settings.SettingsScreen

@Composable
fun VerdyNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onPlantClick = { plantId ->
                    navController.navigate(Screen.PlantDetail.createRoute(plantId))
                },
                onAddPlant = {
                    navController.navigate(Screen.PlantForm.createRoute())
                },
                onAddReminder = { plantId ->
                    navController.navigate(Screen.ReminderManager.createRoute(plantId))
                }
            )
        }

        composable(Screen.PlantList.route) {
            PlantListScreen(
                onPlantClick = { plantId ->
                    navController.navigate(Screen.PlantDetail.createRoute(plantId))
                },
                onAddPlant = {
                    navController.navigate(Screen.PlantForm.createRoute())
                }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onPlantClick = { plantId ->
                    navController.navigate(Screen.PlantDetail.createRoute(plantId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(
            route = Screen.PlantDetail.route,
            arguments = listOf(navArgument("plantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
            PlantDetailScreen(
                plantId = plantId,
                onEditClick = {
                    navController.navigate(Screen.PlantForm.createRoute(plantId))
                },
                onRemindersClick = {
                    navController.navigate(Screen.ReminderManager.createRoute(plantId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PlantForm.route,
            arguments = listOf(
                navArgument("plantId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId")
                ?.takeIf { it != -1L }
            PlantFormScreen(
                editPlantId = plantId,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ReminderManager.route,
            arguments = listOf(navArgument("plantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
            ReminderManagerScreen(
                plantId = plantId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
