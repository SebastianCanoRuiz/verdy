package com.verdy.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.verdy.R
import com.verdy.domain.model.Plant
import com.verdy.domain.model.Reminder
import com.verdy.domain.model.enums.ReminderType

@Composable
fun ReminderCard(
    plant: Plant,
    reminder: Reminder,
    onDone: () -> Unit,
    onPostpone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = reminder.type.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plant.customName,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = reminder.type.displayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.action_done))
                }
                OutlinedButton(
                    onClick = onPostpone,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.action_postpone))
                }
            }
        }
    }
}

@Composable
private fun ReminderType.displayName(): String = when (this) {
    ReminderType.WATERING -> stringResource(R.string.reminder_watering)
    ReminderType.FERTILIZING -> stringResource(R.string.reminder_fertilizing)
    ReminderType.REPOTTING -> stringResource(R.string.reminder_repotting)
    ReminderType.PRUNING -> stringResource(R.string.reminder_pruning)
    ReminderType.CUSTOM -> stringResource(R.string.reminder_custom)
}

private fun ReminderType.icon(): ImageVector = when (this) {
    ReminderType.WATERING -> Icons.Outlined.WaterDrop
    ReminderType.FERTILIZING -> Icons.Outlined.Eco
    ReminderType.REPOTTING -> Icons.Outlined.MoveDown
    ReminderType.PRUNING -> Icons.Outlined.ContentCut
    ReminderType.CUSTOM -> Icons.Outlined.Notifications
}
