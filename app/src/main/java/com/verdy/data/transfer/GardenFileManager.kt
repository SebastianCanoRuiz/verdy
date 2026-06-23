package com.verdy.data.transfer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.verdy.domain.usecase.transfer.GardenExportData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GardenFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val exportDir: File get() = File(context.filesDir, "export").also { it.mkdirs() }
    private val backupDir: File get() = File(context.filesDir, "backups").also { it.mkdirs() }

    /** Writes all garden data to a ZIP file and returns a shareable URI */
    fun exportToZip(data: GardenExportData): Result<Uri> = runCatching {
        val dto = data.toDto()
        val jsonStr = dto.toJsonString()

        val zipFile = File(exportDir, "verdy_backup_${System.currentTimeMillis()}.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // Write garden JSON
            zos.putNextEntry(ZipEntry("garden.json"))
            zos.write(jsonStr.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // Copy plant photos into zip
            data.plants.forEach { plant ->
                val photoUri = plant.photoUri ?: return@forEach
                runCatching {
                    val uri = Uri.parse(photoUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val fileName = "photos/plant_${plant.id}.jpg"
                        zos.putNextEntry(ZipEntry(fileName))
                        stream.copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }
        }

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zipFile)
    }

    /** Reads a ZIP file from a content URI and parses the garden.json inside */
    fun importFromZip(uri: Uri): Result<GardenExportData> = runCatching {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: error("No se pudo abrir el archivo")

        var gardenData: GardenExportData? = null

        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "garden.json") {
                    val json = zis.readBytes().toString(Charsets.UTF_8)
                    gardenData = gardenDtoFromJson(json).toDomain()
                }
                entry = zis.nextEntry
            }
        }

        gardenData ?: error("Archivo ZIP inválido: falta garden.json")
    }

    /**
     * Saves a backup ZIP to internal storage (filesDir/backups/).
     * Keeps only the last [maxBackups] files; older ones are deleted automatically.
     */
    fun exportToInternalBackup(data: GardenExportData, maxBackups: Int = 7): Result<File> = runCatching {
        val dto = data.toDto()
        val jsonStr = dto.toJsonString()
        val dateTag = java.time.LocalDate.now().toString() // YYYY-MM-DD
        val backupFile = File(backupDir, "verdy_backup_$dateTag.zip")

        ZipOutputStream(FileOutputStream(backupFile)).use { zos ->
            zos.putNextEntry(ZipEntry("garden.json"))
            zos.write(jsonStr.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        // Prune old backups
        val allBackups = backupDir.listFiles { f -> f.name.endsWith(".zip") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
        allBackups.drop(maxBackups).forEach { it.delete() }

        backupFile
    }

    /** Returns all backup ZIP files sorted by most recent first */
    fun listBackups(): List<File> =
        backupDir.listFiles { f -> f.name.endsWith(".zip") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()

    /** Reads a backup ZIP from internal storage and parses the garden.json inside */
    fun importFromBackupFile(file: File): Result<GardenExportData> = runCatching {
        var gardenData: GardenExportData? = null
        ZipInputStream(file.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "garden.json") {
                    val json = zis.readBytes().toString(Charsets.UTF_8)
                    gardenData = gardenDtoFromJson(json).toDomain()
                }
                entry = zis.nextEntry
            }
        }
        gardenData ?: error("Backup inválido: falta garden.json")
    }

    /** Generates a QR code bitmap from a text payload */
    fun generateQRBitmap(payload: String, size: Int = 512): Result<Bitmap> = runCatching {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        bitmap
    }
}
