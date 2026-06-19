package com.verdy.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.verdy.R
import com.verdy.domain.model.PlantEnvironment

@Composable
fun EnvironmentSectionHeader(
    environmentName: String,
    plantCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Outlined.MeetingRoom,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Text(
            text = environmentName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (plantCount == 1) {
                stringResource(R.string.environment_plant_count_one)
            } else {
                stringResource(R.string.environment_plant_count, plantCount)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnvironmentSelector(
    environments: List<PlantEnvironment>,
    selectedEnvironmentId: Long?,
    onEnvironmentSelected: (Long?) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.environment_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        if (environments.isEmpty()) {
            Text(
                text = stringResource(R.string.environment_select),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            environments.forEach { env ->
                FilterChip(
                    selected = selectedEnvironmentId == env.id,
                    onClick = {
                        onEnvironmentSelected(
                            if (selectedEnvironmentId == env.id) null else env.id
                        )
                    },
                    label = { Text(env.name) }
                )
            }
            FilterChip(
                selected = false,
                onClick = onCreateClick,
                label = { Text("+ ${stringResource(R.string.environment_create)}") }
            )
        }
    }
}
