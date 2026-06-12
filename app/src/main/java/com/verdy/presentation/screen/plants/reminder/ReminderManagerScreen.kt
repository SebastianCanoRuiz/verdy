package com.verdy.presentation.screen.plants.reminder

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verdy.domain.model.Reminder
import com.verdy.domain.model.ReminderFrequency
import com.verdy.domain.model.enums.ReminderType
import com.verdy.presentation.component.EmptyState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderManagerScreen(
    plantId: Long,
    onBack: () -> Unit,
    viewModel: ReminderManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(plantId) {
        viewModel.loadReminders(plantId)
    }

    if (showAddDialog) {
        AddReminderDialog(
            plantId = plantId,
            onDismiss = { showAddDialog = false },
            onAdd = { reminder ->
                viewModel.addReminder(reminder)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar recordatorio")
            }
        }
    ) { paddingValues ->

        if (uiState.reminders.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Outlined.Notifications,
                    message = "Sin recordatorios",
                    subtitle = "Toca + para añadir un recordatorio"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.reminders, key = { it.id }) { reminder ->
                    ReminderItemCard(
                        reminder = reminder,
                        onToggle = { viewModel.toggleReminder(reminder.id) },
                        onDelete = { viewModel.deleteReminder(reminder.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ReminderItemCard(
    reminder: Reminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (reminder.type) {
                        ReminderType.WATERING -> "💧 Riego"
                        ReminderType.FERTILIZING -> "🌱 Abono"
                        ReminderType.REPOTTING -> "🪴 Trasplante"
                        ReminderType.PRUNING -> "✂️ Poda"
                        ReminderType.CUSTOM -> "⚙️ ${reminder.customLabel ?: "Personalizado"}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = frequencyLabel(reminder.frequency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Próximo: ${reminder.nextDueDate().format(DateTimeFormatter.ofPattern("d MMM"))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Switch(
                checked = reminder.isActive,
                onCheckedChange = { onToggle() }
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderDialog(
    plantId: Long,
    onDismiss: () -> Unit,
    onAdd: (Reminder) -> Unit
) {
    var selectedType by remember { mutableStateOf(ReminderType.WATERING) }
    var typeExpanded by remember { mutableStateOf(false) }
    var isOneTime by remember { mutableStateOf(false) }
    var customDays by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo recordatorio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type selector
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = reminderTypeLabel(selectedType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        ReminderType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(reminderTypeLabel(type)) },
                                onClick = { selectedType = type; typeExpanded = false }
                            )
                        }
                    }
                }

                // One-time toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Una sola vez", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isOneTime,
                        onCheckedChange = { isOneTime = it }
                    )
                }

                // Custom days input (hidden when one-time)
                if (!isOneTime) {
                    OutlinedTextField(
                        value = customDays,
                        onValueChange = { if (it.all { c -> c.isDigit() }) customDays = it },
                        label = { Text("Repetir cada (días)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val frequency = if (isOneTime) ReminderFrequency.Once
                    else ReminderFrequency.EveryXDays(customDays.toIntOrNull()?.coerceAtLeast(1) ?: 7)
                    val reminder = Reminder(
                        plantId = plantId,
                        type = selectedType,
                        startDate = LocalDate.now(),
                        frequency = frequency,
                        isActive = true
                    )
                    onAdd(reminder)
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun reminderTypeLabel(type: ReminderType): String = when (type) {
    ReminderType.WATERING -> "Riego"
    ReminderType.FERTILIZING -> "Abono"
    ReminderType.REPOTTING -> "Trasplante"
    ReminderType.PRUNING -> "Poda"
    ReminderType.CUSTOM -> "Otro"
}

private fun frequencyLabel(frequency: ReminderFrequency): String = when (frequency) {
    is ReminderFrequency.Once -> "Una vez"
    is ReminderFrequency.Daily -> "Diaria"
    is ReminderFrequency.Weekly -> "Semanal"
    is ReminderFrequency.EveryXDays -> "Cada ${frequency.days} días"
    is ReminderFrequency.Monthly -> "Mensual"
}
