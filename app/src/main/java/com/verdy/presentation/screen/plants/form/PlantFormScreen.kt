package com.verdy.presentation.screen.plants.form

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.verdy.R
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.SunExposure
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
            // Copy to internal storage to avoid losing access to the content URI after restart
            val savedPath = copyPhotoToInternal(context, it)
            viewModel.onPhotoUriChange(savedPath)
        }
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { galleryLauncher.launch("image/*") },
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
                            "Foto",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Basic info
            SectionHeader("Información básica")

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

            HorizontalDivider()

            // Status
            SectionHeader(stringResource(R.string.plant_status))
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

            HorizontalDivider()

            // Sun exposure
            SectionHeader(stringResource(R.string.plant_sun_exposure))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SunExposure.values().forEach { sun ->
                    FilterChip(
                        selected = uiState.sunExposure == sun,
                        onClick = { viewModel.onSunExposureChange(sun) },
                        label = {
                            Text(
                                when (sun) {
                                    SunExposure.INTERIOR -> stringResource(R.string.sun_low)
                                    SunExposure.SEMI_SHADE -> stringResource(R.string.sun_medium)
                                    SunExposure.EXTERIOR -> stringResource(R.string.sun_high)
                                }
                            )
                        }
                    )
                }
            }

            HorizontalDivider()

            // Watering section
            SectionHeader("Riego")

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

            if (!uiState.isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Crear recordatorio de riego automáticamente",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (uiState.wateringFrequencyDays.toIntOrNull() == null ||
                            uiState.wateringFrequencyDays.toIntOrNull() == 0) {
                            Text(
                                "Ingresa una frecuencia para activar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = uiState.autoCreateWateringReminder &&
                                (uiState.wateringFrequencyDays.toIntOrNull() ?: 0) > 0,
                        onCheckedChange = viewModel::onAutoWateringReminderChange,
                        enabled = (uiState.wateringFrequencyDays.toIntOrNull() ?: 0) > 0
                    )
                }
            }

            HorizontalDivider()

            // Fertilizing section
            SectionHeader("Abono")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.fertilizingFrequencyDays,
                    onValueChange = viewModel::onFertilizingFrequencyChange,
                    label = { Text("Frecuencia (días, opcional)") },
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

            if (!uiState.isEditMode && (uiState.fertilizingFrequencyDays.toIntOrNull() ?: 0) > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Crear recordatorio de abono automáticamente",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.autoCreateFertilizingReminder,
                        onCheckedChange = viewModel::onAutoFertilizingReminderChange
                    )
                }
            }

            HorizontalDivider()

            // Notes
            SectionHeader("Notas")
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

            // Save
            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSaving && uiState.customName.isNotBlank() && uiState.commonName.isNotBlank(),
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

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * Copies an image from a content URI to the app's internal photos directory.
 * Returns the absolute path of the saved file, or null on error.
 *
 * Using internal storage avoids the issue where content URIs become invalid
 * after app restart (the system revokes temporary read permissions).
 */
private fun copyPhotoToInternal(context: Context, sourceUri: Uri): String? =
    runCatching {
        val dir = File(context.filesDir, "photos").also { it.mkdirs() }
        val dest = File(dir, "plant_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { inp ->
            dest.outputStream().use { out -> inp.copyTo(out) }
        }
        dest.absolutePath
    }.getOrNull()
