package com.sensortv.app.domain

import android.util.Log
import com.sensortv.app.data.datasource.FileCompressor
import com.sensortv.app.data.model.CaptureRecordEntity
import java.io.File

/**
 * Caso de Uso encargado de coordinar la exportación masiva de registros.
 * - Se encarga de validar la existencia física de los archivos antes de enviarlos a compresión.
 *
 * @param compressor Implementación del contrato de FileCompressor (inyectado desde Data).
 */
class ExportAllCapturesUseCase(
    private val compressor: FileCompressor
) {
    /**
     * Ejecuta la lógica de preparación y exportación para el ZIP.
     * 1. Convierte las rutas de la base de datos en objetos [File].
     * 2. Filtra archivos inexistentes para evitar errores en el ZIP.
     * 3. Genera un nombre único basado en la marca de tiempo actual.
     *
     * @param records Lista de entidades obtenidas de la base de datos.
     * @return El archivo ZIP resultante.
     */
    suspend operator fun invoke(records: List<CaptureRecordEntity>): File {

        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))

        val files = records.mapNotNull { record ->
            val file = File(record.filePath)
            Log.d("DEBUG_ZIP", "Buscando: ${record.filePath} - ¿Existe?: ${file.exists()}")
            if (file.exists()) file else null
        }

        if (files.isEmpty()) throw IllegalStateException("No hay archivos para exportar")

        val zipName = "captures_$timestamp.zip"
        return compressor.zipFiles(files, zipName)
    }
}