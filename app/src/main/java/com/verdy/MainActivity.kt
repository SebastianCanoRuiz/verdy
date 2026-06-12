package com.verdy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.verdy.data.worker.NotificationHelper
import com.verdy.presentation.navigation.BottomNavItem
import com.verdy.presentation.navigation.VerdyNavGraph
import com.verdy.presentation.theme.VerdyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannel(this)

        setContent {
            VerdyTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomBarRoutes = BottomNavItem.items.map { it.route }.toSet()
                val showBottomBar = currentRoute in bottomBarRoutes

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                BottomNavItem.items.forEach { item ->
                                    val selected = currentRoute == item.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            if (!selected) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = stringResource(item.labelRes)
                                            )
                                        },
                                        label = { Text(stringResource(item.labelRes)) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    VerdyNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
