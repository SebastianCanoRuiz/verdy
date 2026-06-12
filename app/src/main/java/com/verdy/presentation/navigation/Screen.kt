package com.verdy.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object PlantList : Screen("plant_list")
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")

    object PlantDetail : Screen("plant_detail/{plantId}") {
        fun createRoute(plantId: Long) = "plant_detail/$plantId"
    }
    object PlantForm : Screen("plant_form?plantId={plantId}") {
        fun createRoute(plantId: Long? = null) =
            if (plantId != null) "plant_form?plantId=$plantId" else "plant_form?plantId=-1"
    }
    object ReminderManager : Screen("reminder_manager/{plantId}") {
        fun createRoute(plantId: Long) = "reminder_manager/$plantId"
    }
}
