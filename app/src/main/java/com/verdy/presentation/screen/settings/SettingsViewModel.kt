package com.verdy.presentation.screen.settings

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.verdy.data.local.datastore.AppPreferences
import com.verdy.data.transfer.GardenFileManager
import com.verdy.data.worker.BackupWorker
import com.verdy.domain.usecase.transfer.DecodeQRUseCase
import com.verdy.domain.usecase.transfer.ExportGardenUseCase
import com.verdy.domain.usecase.transfer.GardenExportData
import com.verdy.domain.usecase.transfer.GenerateQRUseCase
import com.verdy.domain.usecase.transfer.ImportGardenUseCase
import com.verdy.domain.usecase.transfer.QRResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportedFileUri: Uri? = null,
    val qrBitmap: android.graphics.Bitmap? = null,
    val qrTooLarge: Boolean = false,
    val importPreview: GardenExportData? = null,
    val successMessage: String? = null,
    val error: String? = null,
    val isBackingUp: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exportGardenUseCase: ExportGardenUseCase,
    private val importGardenUseCase: ImportGardenUseCase,
    private val generateQRUseCase: GenerateQRUseCase,
    private val decodeQRUseCase: DecodeQRUseCase,
    private val gardenFileManager: GardenFileManager,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    val lastBackupDate: StateFlow<String?> = appPreferences.lastBackupDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun exportZip() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            exportGardenUseCase().fold(
                onSuccess = { data ->
                    gardenFileManager.exportToZip(data).fold(
                        onSuccess = { uri ->
                            _uiState.update {
                                it.copy(isExporting = false, exportedFileUri = uri, successMessage = "Datos exportados correctamente")
                            }
                        },
                        onFailure = { e -> _uiState.update { it.copy(isExporting = false, error = e.message) } }
                    )
                },
                onFailure = { e -> _uiState.update { it.copy(isExporting = false, error = e.message) } }
            )
        }
    }

    fun generateQR() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            exportGardenUseCase().fold(
                onSuccess = { data ->
                    when (val qrResult = generateQRUseCase(data)) {
                        is QRResult.Success -> {
                            val bitmap = gardenFileManager.generateQRBitmap(qrResult.jsonPayload)
                            _uiState.update {
                                it.copy(isExporting = false, qrBitmap = bitmap.getOrNull(), qrTooLarge = false)
                            }
                        }
                        is QRResult.TooLarge -> {
                            _uiState.update { it.copy(isExporting = false, qrTooLarge = true) }
                        }
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isExporting = false, error = e.message) } }
            )
        }
    }

    fun importFromFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            gardenFileManager.importFromZip(uri).fold(
                onSuccess = { data -> _uiState.update { it.copy(isImporting = false, importPreview = data) } },
                onFailure = { e -> _uiState.update { it.copy(isImporting = false, error = e.message) } }
            )
        }
    }

    fun importFromQRPayload(payload: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            decodeQRUseCase(payload).fold(
                onSuccess = { data -> _uiState.update { it.copy(isImporting = false, importPreview = data) } },
                onFailure = { e -> _uiState.update { it.copy(isImporting = false, error = e.message) } }
            )
        }
    }

    fun importFromImageUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            runCatching {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: error("No se pudo abrir la imagen")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                    ?: error("Formato de imagen no soportado")

                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                val source = RGBLuminanceSource(width, height, pixels)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                QRCodeReader().decode(binaryBitmap).text
            }.fold(
                onSuccess = { payload -> importFromQRPayload(payload) },
                onFailure = { e ->
                    val msg = if (e is NotFoundException)
                        "No se encontró un código QR en la imagen"
                    else e.message ?: "Error al leer la imagen"
                    _uiState.update { it.copy(isImporting = false, error = msg) }
                }
            )
        }
    }

    fun triggerManualBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackingUp = true) }
            exportGardenUseCase().fold(
                onSuccess = { data ->
                    gardenFileManager.exportToInternalBackup(data).fold(
                        onSuccess = {
                            appPreferences.setLastBackupDate(java.time.LocalDate.now().toString())
                            _uiState.update {
                                it.copy(isBackingUp = false, successMessage = "Backup guardado correctamente")
                            }
                        },
                        onFailure = { e ->
                            _uiState.update { it.copy(isBackingUp = false, error = e.message) }
                        }
                    )
                },
                onFailure = { e -> _uiState.update { it.copy(isBackingUp = false, error = e.message) } }
            )
        }
    }

    fun confirmImport(data: GardenExportData, replace: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            importGardenUseCase(data, replace).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importPreview = null,
                            successMessage = "Datos importados: ${data.plants.size} plantas"
                        )
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isImporting = false, error = e.message) } }
            )
        }
    }

    fun clearQr() = _uiState.update { it.copy(qrBitmap = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null, exportedFileUri = null) }
    fun clearError() = _uiState.update { it.copy(error = null, qrTooLarge = false) }
    fun dismissImportPreview() = _uiState.update { it.copy(importPreview = null) }
}
