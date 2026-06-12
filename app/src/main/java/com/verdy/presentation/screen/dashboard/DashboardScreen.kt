package com.verdy.presentation.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verdy.R
import com.verdy.domain.model.enums.ReminderType
import com.verdy.presentation.component.EmptyState
import com.verdy.presentation.component.ReminderCard
import kotlinx.coroutines.launch

private enum class FabMenuStep { NONE, MAIN, PLANT_SELECTOR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onPlantClick: (Long) -> Unit,
    onAddPlant: () -> Unit,
    onAddReminder: (Long) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var fabMenuStep by remember { mutableStateOf(FabMenuStep.NONE) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun closeSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            fabMenuStep = FabMenuStep.NONE
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_greeting),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Verdy",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { fabMenuStep = FabMenuStep.MAIN },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SummaryCard(totalPlants = uiState.totalPlants, todayCount = uiState.todayItems.size)
                Spacer(Modifier.height(8.dp))
            }

            item {
                Text(
                    text = stringResource(R.string.dashboard_today_pending),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
            }

            if (uiState.todayItems.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.WbSunny,
                        message = stringResource(R.string.dashboard_no_pending),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(uiState.todayItems, key = { it.reminder.id }) { item ->
                    ReminderCard(
                        plant = item.plant,
                        reminder = item.reminder,
                        onDone = { viewModel.markAsDone(item.reminder) },
                        onPostpone = { viewModel.postpone(item.reminder) }
                    )
                }
            }

            if (uiState.upcomingItems.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.dashboard_upcoming),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(uiState.upcomingItems, key = { "up_${it.reminder.id}" }) { item ->
                    UpcomingReminderRow(item = item, onClick = { onPlantClick(item.plant.id) })
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // FAB bottom sheet
    if (fabMenuStep != FabMenuStep.NONE) {
        ModalBottomSheet(
            onDismissRequest = { fabMenuStep = FabMenuStep.NONE },
            sheetState = sheetState
        ) {
            when (fabMenuStep) {
                FabMenuStep.MAIN -> {
                    Column(modifier = Modifier.padding(bottom = 32.dp)) {
                        Text(
                            text = "¿Qué quieres agregar?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        HorizontalDivider()
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Grass, contentDescription = null) },
                            label = { Text("Nueva planta") },
                            selected = false,
                            onClick = {
                                closeSheet()
                                onAddPlant()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                            label = {
                                Column {
                                    Text("Nuevo recordatorio")
                                    Text(
                                        "Selecciona una planta",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            selected = false,
                            onClick = { fabMenuStep = FabMenuStep.PLANT_SELECTOR },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                FabMenuStep.PLANT_SELECTOR -> {
                    Column(modifier = Modifier.padding(bottom = 32.dp)) {
                        Text(
                            text = "Selecciona una planta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        HorizontalDivider()
                        if (uiState.allPlants.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Agrega una planta primero",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            uiState.allPlants.forEach { plant ->
                                NavigationDrawerItem(
                                    icon = {
                                        Icon(
                                            Icons.Outlined.Grass,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Column {
                                            Text(plant.customName)
                                            Text(
                                                plant.commonName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    selected = false,
                                    onClick = {
                                        closeSheet()
                                        onAddReminder(plant.id)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                FabMenuStep.NONE -> Unit
            }
        }
    }
}

@Composable
private fun SummaryCard(totalPlants: Int, todayCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Outlined.Grass,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = if (todayCount == 0)
                        stringResource(R.string.dashboard_total_plants, totalPlants)
                    else
                        stringResource(R.string.dashboard_pending_count, todayCount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (totalPlants > 0) {
                    Text(
                        text = stringResource(R.string.dashboard_total_plants, totalPlants),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpcomingReminderRow(item: PlantReminderItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.plant.customName, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = when (item.reminder.type) {
                        ReminderType.WATERING -> "Riego"
                        ReminderType.FERTILIZING -> "Abono"
                        ReminderType.REPOTTING -> "Trasplante"
                        ReminderType.PRUNING -> "Poda"
                        ReminderType.CUSTOM -> item.reminder.customLabel ?: "Cuidado personalizado"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "en ${
                    java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.now(),
                        item.reminder.nextDueDate()
                    )
                } días",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
