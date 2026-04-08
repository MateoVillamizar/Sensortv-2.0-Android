package com.sensortv.app.domain

import com.sensortv.app.data.datasource.CsvDataSource
import com.sensortv.app.data.model.CaptureRecordEntity
import com.sensortv.app.data.model.SensorResult
import com.sensortv.app.data.repository.CaptureRepository

/**
 * Caso de uso que coordina la persistencia completa de una sesión de captura.
 * 1. Genera las líneas de texto para el formato CSV.
 * 2. Guarda los archivos físicos (mediciones y totales).
 * 3. Registra los metadatos en la base de datos local.
 *
 * @param csvDataSource Implementación de [CsvDataSource] que gestiona la persistencia de archivos CSV.
 * @param captureRepository Implementación de [CaptureRepository] para el registro de metadatos en Room.
 */
class SaveCaptureUseCase(
    private val csvDataSource: CsvDataSource,
    private val captureRepository: CaptureRepository
) {
    /**
     * Ejecuta el proceso de guardado integral: genera los strings del CSV,
     * escribe los archivos en disco y guarda la referencia en la base de datos local.
     *
     * @param timestamp Identificador de tiempo para el nombre del archivo (formato YYYY-MM-DD...).
     * @param durationMinutes Duración total que tuvo la captura en minutos.
     * @param samplingFrequency Frecuencia en segundos utilizada (1, 3 o 5s).
     * @param sensorResults Lista de resultados procesados de cada sensor.
     */
    suspend operator fun invoke(
        timestamp: String,
        durationMinutes: Int,
        samplingFrequency: Int,
        sensorResults: List<SensorResult>
    ) {
        // Preparar contenido para mediciones.csv
        val medicionesContent = mutableListOf<String>("timestamp,sensor,potencia_mw")
        sensorResults.forEach {
            medicionesContent.add("${it.timestamp},${it.displayName},${it.estimatedPowerMw}")
        }

        // Preparar contenido para totales.csv
        val totalesContent = mutableListOf<String>("sensor,energia_total_j")
        sensorResults.forEach {
            totalesContent.add("${it.displayName},${it.totalEnergyJ}")
        }

        //Guardar archivos físicos
        val medicionesFile = csvDataSource.writeCsv("${timestamp}_mediciones.csv", medicionesContent)
        val totalesFile = csvDataSource.writeCsv("${timestamp}_totales.csv", totalesContent)

        // Guardar metadatos en Room (usamos el archivo de mediciones como referencia principal)
        val entity = CaptureRecordEntity(
            fileName = "${timestamp}_mediciones.csv",
            durationMinutes = durationMinutes,
            samplingFrequencySeconds = samplingFrequency,
            dateMillis = System.currentTimeMillis(),
            filePath = medicionesFile.absolutePath
        )
        captureRepository.saveRecord(entity)
    }
}