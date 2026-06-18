package com.verdy.presentation.screen.plants.form

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.verdy.R
import com.verdy.domain.model.enums.PlantMedium
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.SunExposure
import com.verdy.presentation.component.IdentificationPreviewCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlantFormScreen(
    editPlantId: Long? = null,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: PlantFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(editPlantId) {
        editPlantId?.let { viewModel.loadPlant(it) }
    }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onSaved()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = copyPhotoToInternal(context, it)
            viewModel.onPhotoUriChange(savedPath)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                val savedPath = copyPhotoToInternal(context, uri)
                viewModel.onPhotoUriChange(savedPath)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Fuente de la foto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("  Cámara", modifier = Modifier.weight(1f))
                    }
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("  Galería", modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoSourceDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditMode) stringResource(R.string.plant_form_edit_title)
                        else stringResource(R.string.plant_form_add_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Photo & AI
            item {
                FormSectionCard(
                    title = stringResource(R.string.form_section_photo),
                    icon = Icons.Outlined.AddAPhoto
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { showPhotoSourceDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.photoUri != null) {
                                AsyncImage(
                                    model = uiState.photoUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.AddAPhoto,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        stringResource(R.string.form_photo_hint),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(" Cámara", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(" Galería", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        if (uiState.photoUri != null) {
                            Button(
                                onClick = { viewModel.identifyFromPhoto(uiState.photoUri!!) },
                                enabled = !uiState.isIdentifying,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isIdentifying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        stringResource(R.string.ai_identifying),
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        stringResource(R.string.ai_identify_button),
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }
                            }
                        }

                        uiState.lastIdentification?.let { result ->
                            IdentificationPreviewCard(result = result, showTitle = true)
                        }
                    }
                }
            }

            // Section 2: Basic info
            item {
                FormSectionCard(
                    title = stringResource(R.string.form_section_basic),
                    icon = Icons.Outlined.Info
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.customName,
                            onValueChange = viewModel::onCustomNameChange,
                            label = { Text(stringResource(R.string.plant_custom_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.commonName,
                            onValueChange = viewModel::onCommonNameChange,
                            label = { Text(stringResource(R.string.plant_common_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.scientificName,
                            onValueChange = viewModel::onScientificNameChange,
                            label = { Text(stringResource(R.string.plant_scientific_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.location,
                            onValueChange = viewModel::onLocationChange,
                            label = { Text(stringResource(R.string.plant_location)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            stringResource(R.string.plant_status),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PlantStatus.values().forEach { status ->
                                FilterChip(
                                    selected = uiState.status == status,
                                    onClick = { viewModel.onStatusChange(status) },
                                    label = {
                                        Text(
                                            when (status) {
                                                PlantStatus.HEALTHY -> stringResource(R.string.status_healthy)
                                                PlantStatus.NEEDS_ATTENTION -> stringResource(R.string.status_needs_attention)
                                                PlantStatus.RECOVERING -> stringResource(R.string.status_recovering)
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Medium & light
            item {
                FormSectionCard(
                    title = stringResource(R.string.form_section_medium_light),
                    icon = Icons.Outlined.WbSunny
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Medio de cultivo",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.medium == PlantMedium.SOIL,
                                onClick = { viewModel.onMediumChange(PlantMedium.SOIL) },
                                label = { Text("🌱 ${stringResource(R.string.plant_medium_soil)}") }
                            )
                            FilterChip(
                                selected = uiState.medium == PlantMedium.WATER,
                                onClick = { viewModel.onMediumChange(PlantMedium.WATER) },
                                label = { Text("💧 ${stringResource(R.string.plant_medium_water)}") }
                            )
                        }

                        Text(
                            stringResource(R.string.plant_sun_exposure),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SunExposure.values().forEach { sun ->
                                FilterChip(
                                    selected = uiState.sunExposure == sun,
                                    onClick = { viewModel.onSunExposureChange(sun) },
                                    label = {
                                        Text(
                                            when (sun) {
                                                SunExposure.INTERIOR -> "🏠 ${stringResource(R.string.sun_low)}"
                                                SunExposure.SEMI_SHADE -> "⛅ ${stringResource(R.string.sun_medium)}"
                                                SunExposure.EXTERIOR -> "☀️ ${stringResource(R.string.sun_high)}"
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Care
            item {
                FormSectionCard(
                    title = stringResource(R.string.form_section_care),
                    icon = Icons.Outlined.WaterDrop
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            stringResource(R.string.form_watering_section),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = uiState.wateringFrequencyDays,
                                onValueChange = viewModel::onWateringFrequencyChange,
                                label = { Text("Frecuencia (días)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = uiState.waterAmountMl,
                                onValueChange = viewModel::onWaterAmountChange,
                                label = { Text("Cantidad (ml)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        if (uiState.medium == PlantMedium.SOIL) {
                            Text(
                                stringResource(R.string.form_fertilizing_section),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = uiState.fertilizingFrequencyDays,
                                    onValueChange = viewModel::onFertilizingFrequencyChange,
                                    label = { Text("Frecuencia (días)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = uiState.fertilizerType,
                                    onValueChange = viewModel::onFertilizerTypeChange,
                                    label = { Text("Tipo de abono") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        if (uiState.medium == PlantMedium.WATER) {
                            Text(
                                stringResource(R.string.form_water_change_section),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = uiState.waterChangeFrequencyDays,
                                onValueChange = viewModel::onWaterChangeFrequencyChange,
                                label = { Text(stringResource(R.string.plant_water_change_frequency)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Section 5: Reminders
            if (!uiState.isEditMode) {
                item {
                    FormSectionCard(
                        title = stringResource(R.string.form_section_reminders),
                        icon = Icons.Outlined.Notifications
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ReminderSwitchRow(
                                label = "Recordatorio de riego automático",
                                hint = if ((uiState.wateringFrequencyDays.toIntOrNull() ?: 0) <= 0) {
                                    "Ingresa una frecuencia para activar"
                                } else null,
                                checked = uiState.autoCreateWateringReminder &&
                                    (uiState.wateringFrequencyDays.toIntOrNull() ?: 0) > 0,
                                enabled = (uiState.wateringFrequencyDays.toIntOrNull() ?: 0) > 0,
                                onCheckedChange = viewModel::onAutoWateringReminderChange
                            )

                            if (uiState.medium == PlantMedium.SOIL &&
                                (uiState.fertilizingFrequencyDays.toIntOrNull() ?: 0) > 0
                            ) {
                                ReminderSwitchRow(
                                    label = "Recordatorio de abono automático",
                                    checked = uiState.autoCreateFertilizingReminder,
                                    onCheckedChange = viewModel::onAutoFertilizingReminderChange
                                )
                            }

                            if (uiState.medium == PlantMedium.WATER &&
                                (uiState.waterChangeFrequencyDays.toIntOrNull() ?: 0) > 0
                            ) {
                                ReminderSwitchRow(
                                    label = "Recordatorio de cambio de agua",
                                    checked = uiState.autoCreateWaterChangeReminder,
                                    onCheckedChange = viewModel::onAutoWaterChangeReminderChange
                                )
                            }
                        }
                    }
                }
            }

            // Section 6: Notes
            item {
                FormSectionCard(
                    title = stringResource(R.string.form_section_notes),
                    icon = Icons.Outlined.Notes
                ) {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text(stringResource(R.string.plant_notes)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Save button
            item {
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isSaving &&
                        uiState.customName.isNotBlank() &&
                        uiState.commonName.isNotBlank(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Text(
                            stringResource(R.string.plant_save),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ReminderSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    hint: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            hint?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

private fun copyPhotoToInternal(context: Context, sourceUri: Uri): String? =
    runCatching {
        val dir = File(context.filesDir, "photos").also { it.mkdirs() }
        val dest = File(dir, "plant_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { inp ->
            dest.outputStream().use { out -> inp.copyTo(out) }
        }
        dest.absolutePath
    }.getOrNull()

private fun createCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "plant_photos").also { it.mkdirs() }
    val file = File(dir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
