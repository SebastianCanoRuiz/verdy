package com.verdy.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.verdy.R
import com.verdy.domain.model.PlantHealthEvaluationResult
import com.verdy.domain.model.enums.IdentificationConfidence
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.presentation.theme.ColorHealthy
import com.verdy.presentation.theme.ColorNeedsAttention
import com.verdy.presentation.theme.ColorRecovering

@Composable
fun PlantHealthEvaluationCard(
    result: PlantHealthEvaluationResult,
    modifier: Modifier = Modifier,
    statusApplied: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.health_eval_suggested_status),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HealthStatusBadge(status = result.suggestedStatus)
                }
                HealthConfidenceBadge(confidence = result.confidence)
            }

            if (result.summary.isNotBlank()) {
                Text(
                    text = result.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (statusApplied) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.health_eval_status_updated),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (result.observations.isNotBlank()) {
                HorizontalDivider()
                SectionBlock(
                    title = stringResource(R.string.health_eval_observations),
                    content = result.observations
                )
            }

            if (result.possibleIssues.isNotEmpty()) {
                SectionBlock(
                    title = stringResource(R.string.health_eval_issues),
                    items = result.possibleIssues
                )
            }

            if (result.recommendedActions.isNotEmpty()) {
                SectionBlock(
                    title = stringResource(R.string.health_eval_actions),
                    items = result.recommendedActions
                )
            }

            if (result.photoTips.isNotBlank()) {
                SectionBlock(
                    title = stringResource(R.string.health_eval_photo_tips),
                    content = result.photoTips
                )
            }
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    content: String? = null,
    items: List<String> = emptyList()
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    content?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    items.forEach { item ->
        Text(
            text = "• $item",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HealthStatusBadge(status: PlantStatus) {
    val (color, labelRes) = when (status) {
        PlantStatus.HEALTHY -> ColorHealthy to R.string.status_healthy
        PlantStatus.NEEDS_ATTENTION -> ColorNeedsAttention to R.string.status_needs_attention
        PlantStatus.RECOVERING -> ColorRecovering to R.string.status_recovering
    }
    Text(
        text = stringResource(labelRes),
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun HealthConfidenceBadge(confidence: IdentificationConfidence) {
    val (label, containerColor) = when (confidence) {
        IdentificationConfidence.HIGH -> stringResource(R.string.ai_confidence_high) to
            MaterialTheme.colorScheme.primaryContainer
        IdentificationConfidence.MEDIUM -> stringResource(R.string.ai_confidence_medium) to
            MaterialTheme.colorScheme.tertiaryContainer
        IdentificationConfidence.LOW -> stringResource(R.string.ai_confidence_low) to
            MaterialTheme.colorScheme.errorContainer
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
