package com.verdy.presentation.screen.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.verdy.BuildConfig
import com.verdy.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lastBackupDate by viewModel.lastBackupDate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val importFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.importFromFile(it) } }

    val importImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.importFromImageUri(it) } }

    val qrScanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { payload -> viewModel.importFromQRPayload(payload) }
    }

    LaunchedEffect(uiState.exportedFileUri) {
        uiState.exportedFileUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir jardín"))
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // QR dialog
    uiState.qrBitmap?.let { bitmap ->
        AlertDialog(
            onDismissRequest = viewModel::clearQr,
            title = { Text("Código QR") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Código QR",
                        modifier = Modifier.size(256.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Escanea este QR desde otro dispositivo con Verdy para importar tus plantas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::clearQr) { Text("Cerrar") }
            }
        )
    }

    if (uiState.qrTooLarge) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("QR no disponible") },
            text = { Text(stringResource(R.string.settings_qr_too_large)) },
            confirmButton = {
                Button(onClick = { viewModel.clearError(); viewModel.exportZip() }) {
                    Text("Exportar ZIP")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::clearError) { Text("Cancelar") }
            }
        )
    }

    uiState.importPreview?.let { data ->
        AlertDialog(
            onDismissRequest = viewModel::dismissImportPreview,
            title = { Text("Importar datos") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se encontraron:")
                    Text("• ${data.plants.size} plantas")
                    Text("• ${data.reminders.size} recordatorios")
                    Text("• ${data.maintenanceLogs.size} registros de historial")
                    Spacer(Modifier.height(8.dp))
                    Text("¿Cómo deseas importar?", style = MaterialTheme.typography.titleSmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmImport(data, replace = false) },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Añadir") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = viewModel::dismissImportPreview) { Text("Cancelar") }
                    OutlinedButton(
                        onClick = { viewModel.confirmImport(data, replace = true) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Reemplazar todo") }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export section
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_export)) {
                    Text(
                        stringResource(R.string.settings_export_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingsButton(
                            text = "Exportar ZIP",
                            icon = Icons.Outlined.Share,
                            onClick = viewModel::exportZip,
                            isLoading = uiState.isExporting,
                            modifier = Modifier.weight(1f)
                        )
                        SettingsButton(
                            text = "Generar QR",
                            icon = Icons.Outlined.QrCode,
                            onClick = viewModel::generateQR,
                            isLoading = uiState.isExporting,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Import section
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_import)) {
                    Text(
                        stringResource(R.string.settings_import_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsButton(
                            text = "ZIP",
                            icon = Icons.Outlined.Download,
                            onClick = { importFileLauncher.launch("application/zip") },
                            isLoading = uiState.isImporting,
                            modifier = Modifier.weight(1f)
                        )
                        SettingsButton(
                            text = "QR cámara",
                            icon = Icons.Outlined.QrCodeScanner,
                            onClick = {
                                val options = ScanOptions().apply {
                                    setPrompt("Escanea el código QR de Verdy")
                                    setBeepEnabled(false)
                                    setOrientationLocked(false)
                                }
                                qrScanLauncher.launch(options)
                            },
                            isLoading = uiState.isImporting,
                            modifier = Modifier.weight(1f)
                        )
                        SettingsButton(
                            text = "QR imagen",
                            icon = Icons.Outlined.Image,
                            onClick = { importImageLauncher.launch("image/*") },
                            isLoading = uiState.isImporting,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Backup section
            item {
                SettingsSectionCard(title = stringResource(R.string.backup_title)) {
                    Text(
                        stringResource(R.string.backup_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lastBackupDate?.let { dateStr ->
                                runCatching {
                                    val date = LocalDate.parse(dateStr)
                                    val daysAgo = ChronoUnit.DAYS.between(date, LocalDate.now())
                                    when (daysAgo) {
                                        0L -> "Último backup: hoy"
                                        1L -> "Último backup: ayer"
                                        else -> "Último backup: hace $daysAgo días"
                                    }
                                }.getOrDefault("Último backup: $dateStr")
                            } ?: stringResource(R.string.backup_never),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = viewModel::triggerManualBackup,
                            enabled = !uiState.isBackingUp,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState.isBackingUp) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Outlined.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(4.dp))
                                Text(stringResource(R.string.backup_now), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // About section
            item {
                SettingsSectionCard(title = "Acerca de Verdy") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Versión", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            BuildConfig.VERSION_NAME,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.about_free),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.about_license),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.about_repo),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            val repoUrl = context.getString(R.string.about_repo_url)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(14.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
