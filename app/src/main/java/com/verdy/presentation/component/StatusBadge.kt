package com.verdy.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.verdy.R
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.presentation.theme.ColorHealthy
import com.verdy.presentation.theme.ColorNeedsAttention
import com.verdy.presentation.theme.ColorRecovering

@Composable
fun StatusBadge(
    status: PlantStatus,
    modifier: Modifier = Modifier
) {
    val (color, labelRes) = when (status) {
        PlantStatus.HEALTHY -> ColorHealthy to R.string.status_healthy
        PlantStatus.NEEDS_ATTENTION -> ColorNeedsAttention to R.string.status_needs_attention
        PlantStatus.RECOVERING -> ColorRecovering to R.string.status_recovering
    }

    Text(
        text = stringResource(labelRes),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
