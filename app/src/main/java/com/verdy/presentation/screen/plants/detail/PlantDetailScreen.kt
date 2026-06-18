package com.verdy.presentation.screen.plants.detail

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.verdy.R
import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.model.enums.SunExposure
import com.verdy.presentation.component.PlantHealthEvaluationCard
import com.verdy.presentation.component.StatusBadge
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: Long,
    onEditClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPhotoViewer by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val healthGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { copyUriToTemp(context, it)?.let { path -> viewModel.evaluateHealth(path) } }
    }

    val healthCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                copyUriToTemp(context, uri)?.let { path -> viewModel.evaluateHealth(path) }
            }
        }
    }

    val healthCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createHealthCameraUri(context)
            pendingCameraUri = uri
            healthCameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(plantId) {
        viewModel.loadPlant(plantId)
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onBack()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Fullscreen zoomable photo viewer
    if (showPhotoViewer && uiState.plant?.photoUri != null) {
        PhotoViewerDialog(
            photoUri = uiState.plant!!.photoUri!!,
            contentDescription = uiState.plant!!.customName,
            onDismiss = { showPhotoViewer = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.plant_delete_confirm)) },
            text = { Text(stringResource(R.string.plant_delete_confirm_detail)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deletePlant()
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    uiState.pendingHealthEvaluation?.let { pending ->
        AlertDialog(
            onDismissRequest = { viewModel.confirmPendingHealthEvaluation(apply = false) },
            title = { Text(stringResource(R.string.health_eval_confirm_low_confidence)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        pending.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    PlantHealthEvaluationCard(
                        result = pending,
                        statusApplied = false
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmPendingHealthEvaluation(apply = true) }) {
                    Text(stringResource(R.string.health_eval_apply_status))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.confirmPendingHealthEvaluation(apply = false) }) {
                    Text(stringResource(R.string.health_eval_keep_status))
                }
            }
        )
    }

    if (uiState.showHealthEvaluationSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissHealthEvaluation,
            sheetState = sheetState
        ) {
            HealthEvaluationSheetContent(
                isEvaluating = uiState.isEvaluatingHealth,
                evaluation = uiState.healthEvaluation,
                onCameraClick = { healthCameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                onGalleryClick = { healthGalleryLauncher.launch("image/*") }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plant?.customName ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRemindersClick,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = "Recordatorios")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val plant = uiState.plant ?: return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Hero photo
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    if (plant.photoUri != null) {
                        AsyncImage(
                            model = plant.photoUri,
                            contentDescription = plant.customName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showPhotoViewer = true }
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Grass,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Status badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        StatusBadge(plant.status)
                    }
                }
            }

            // Name and info
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = plant.customName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = plant.commonName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    plant.scientificName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                    plant.location?.let {
                        Text(
                            text = "📍 $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Care summary cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CareInfoCard(
                        icon = Icons.Outlined.WaterDrop,
                        label = "Riego",
                        value = "Cada ${plant.careInfo.wateringFrequencyDays}d",
                        secondary = uiState.lastWateringDate?.let { "Último: ${formatDate(it)}" },
                        modifier = Modifier.weight(1f)
                    )
                    CareInfoCard(
                        icon = Icons.Outlined.WbSunny,
                        label = "Luz",
                        value = when (plant.careInfo.sunExposure) {
                            SunExposure.INTERIOR -> "Interior"
                            SunExposure.SEMI_SHADE -> "Semisombra"
                            SunExposure.EXTERIOR -> "Exterior"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (plant.careInfo.fertilizingFrequencyDays != null) {
                        CareInfoCard(
                            icon = Icons.Outlined.Eco,
                            label = "Abono",
                            value = "Cada ${plant.careInfo.fertilizingFrequencyDays}d",
                            secondary = uiState.lastFertilizingDate?.let { "Último: ${formatDate(it)}" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Health evaluation section
            item {
                HealthEvaluationSection(
                    lastEvaluation = uiState.healthEvaluation,
                    onEvaluateClick = viewModel::openHealthEvaluation
                )
            }

            // Notes
            plant.notes?.let { notes ->
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Notas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Curiosities section
            item {
                CuriositiesSection(
                    curiosities = uiState.curiosities,
                    isLoading = uiState.loadingCuriosities
                )
            }

            // History section
            item {
                Text(
                    text = stringResource(R.string.plant_detail_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (uiState.history.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.plant_detail_no_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.history.take(20), key = { it.id }) { log ->
                    HistoryRow(log = log)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CuriositiesSection(
    curiosities: String?,
    isLoading: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "✨ Curiosidades",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(8.dp))
        when {
            isLoading -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        "Consultando IA...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            curiosities != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = curiosities,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            else -> {
                Text(
                    "Configura tu API key de OpenAI en local.properties para ver curiosidades.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun CareInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    secondary: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon, contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            secondary?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun HistoryRow(log: MaintenanceLog) {
    val isHealthEval = log.type == ReminderType.CUSTOM &&
        log.notes?.startsWith("Evaluación IA:") == true

    val emoji = when {
        isHealthEval -> "🩺"
        log.action == MaintenanceAction.DONE -> when (log.type) {
            ReminderType.WATERING -> "💧"
            ReminderType.FERTILIZING -> "🌱"
            ReminderType.REPOTTING -> "🪴"
            ReminderType.PRUNING -> "✂️"
            ReminderType.WATER_CHANGE -> "🫧"
            ReminderType.CUSTOM -> "✅"
        }
        log.action == MaintenanceAction.POSTPONED -> "⏰"
        else -> "✗"
    }

    val actionLabel = when {
        isHealthEval -> stringResource(R.string.history_health_evaluation)
        log.action == MaintenanceAction.DONE -> when (log.type) {
            ReminderType.WATERING -> "Riego realizado"
            ReminderType.FERTILIZING -> "Abono aplicado"
            ReminderType.REPOTTING -> "Trasplante realizado"
            ReminderType.PRUNING -> "Poda realizada"
            ReminderType.WATER_CHANGE -> "Cambio de agua realizado"
            ReminderType.CUSTOM -> log.notes ?: "Cuidado realizado"
        }
        log.action == MaintenanceAction.POSTPONED -> "Pospuesto"
        else -> "Ignorado"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, style = MaterialTheme.typography.bodyLarge)
        Column {
            Text(
                text = log.date.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM"))

@Composable
private fun HealthEvaluationSection(
    lastEvaluation: com.verdy.domain.model.PlantHealthEvaluationResult?,
    onEvaluateClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Healing,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        stringResource(R.string.health_eval_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    stringResource(R.string.health_eval_tips_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                listOf(
                    R.string.health_eval_tip_light,
                    R.string.health_eval_tip_focus,
                    R.string.health_eval_tip_angle,
                    R.string.health_eval_tip_background
                ).forEach { tipRes ->
                    Text(
                        "• ${stringResource(tipRes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onEvaluateClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        stringResource(R.string.health_eval_button),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        lastEvaluation?.let { result ->
            PlantHealthEvaluationCard(result = result, statusApplied = true)
        }
    }
}

@Composable
private fun HealthEvaluationSheetContent(
    isEvaluating: Boolean,
    evaluation: com.verdy.domain.model.PlantHealthEvaluationResult?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.health_eval_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            stringResource(R.string.health_eval_tips_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        listOf(
            R.string.health_eval_tip_light,
            R.string.health_eval_tip_focus,
            R.string.health_eval_tip_angle
        ).forEach { tipRes ->
            Text(
                "• ${stringResource(tipRes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isEvaluating) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    stringResource(R.string.health_eval_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (evaluation == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCameraClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Cámara", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Galería", style = MaterialTheme.typography.labelMedium)
                }
            }
        } else {
            PlantHealthEvaluationCard(result = evaluation, statusApplied = true)
        }
    }
}

private fun copyUriToTemp(context: Context, uri: Uri): String? =
    runCatching {
        val tmp = File(context.cacheDir, "health_eval_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { inp ->
            tmp.outputStream().use { out -> inp.copyTo(out) }
        }
        tmp.absolutePath
    }.getOrNull()

private fun createHealthCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "plant_photos").also { it.mkdirs() }
    val file = File(dir, "health_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

/**
 * Fullscreen dialog that shows a plant photo with pinch-to-zoom support.
 * The user can pinch to zoom in/out and pan the image. Tapping anywhere closes it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoViewerDialog(
    photoUri: String,
    contentDescription: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offset += panChange * scale
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformState)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            )
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
