package com.verdy.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.verdy.R
import com.verdy.domain.model.PlantIdentificationResult
import com.verdy.domain.model.enums.IdentificationConfidence
import com.verdy.domain.model.enums.SunExposure

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdentificationPreviewCard(
    result: PlantIdentificationResult,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
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
            if (showTitle) {
                Text(
                    text = stringResource(R.string.ai_identification_result),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.commonName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (result.scientificName.isNotBlank()) {
                        Text(
                            text = result.scientificName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
                ConfidenceBadge(confidence = result.confidence)
            }

            if (result.careSummary.isNotBlank()) {
                Text(
                    text = result.careSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CareChip(
                    label = when (result.sunExposure) {
                        SunExposure.INTERIOR -> "🏠 ${stringResource(R.string.sun_low)}"
                        SunExposure.SEMI_SHADE -> "⛅ ${stringResource(R.string.sun_medium)}"
                        SunExposure.EXTERIOR -> "☀️ ${stringResource(R.string.sun_high)}"
                    }
                )
                CareChip(label = "💧 ${stringResource(R.string.plant_detail_watering, result.wateringFrequencyDays)}")
                result.fertilizingFrequencyDays?.let { days ->
                    CareChip(label = "🌿 ${stringResource(R.string.ai_fertilizing_every, days)}")
                }
                result.fertilizerType?.takeIf { it.isNotBlank() }?.let { type ->
                    CareChip(label = "🧪 $type")
                }
                result.hasFlowers?.let { hasFlowers ->
                    CareChip(
                        label = if (hasFlowers) {
                            "🌸 ${stringResource(R.string.ai_has_flowers)}"
                        } else {
                            "🍃 ${stringResource(R.string.ai_no_flowers)}"
                        }
                    )
                }
            }

            if (result.regions.isNotBlank() || result.curiosities.isNotBlank()) {
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.ai_about_plant),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (result.regions.isNotBlank()) {
                    Text(
                        text = "📍 ${result.regions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (result.curiosities.isNotBlank()) {
                    Text(
                        text = result.curiosities,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (result.confidence == IdentificationConfidence.LOW) {
                if (result.alternativeNames.isNotEmpty()) {
                    HorizontalDivider()
                    Text(
                        text = stringResource(R.string.ai_alternatives),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    result.alternativeNames.forEach { name ->
                        Text(
                            text = "• $name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (result.suggestions.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.ai_suggestions),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = result.suggestions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: IdentificationConfidence) {
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

@Composable
private fun CareChip(label: String) {
    FilterChip(
        selected = true,
        onClick = {},
        enabled = false,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}
