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
     * @param allMeasurements Lista de todas las filas de mediciones.csv.
     * @param sensorResults Lista de resultados totales de cada sensor.
     */
    suspend operator fun invoke(
        timestamp: String,
        durationMinutes: Int,
        samplingFrequency: Int,
        allMeasurements: List<String>,
        sensorResults: List<SensorResult>
    ) {
        // Archivo de Mediciones (Log histórico)
        val medicionesHeader = "timestamp,sensor,potencia_mw"
        val medicionesContent = listOf(medicionesHeader) + allMeasurements
        val medicionesFile = csvDataSource.writeCsv("${timestamp}_mediciones.csv", medicionesContent)

        // Archivo de Totales (Resumen final)
        val totalesContent = mutableListOf("sensor,energia_total_j")
        sensorResults.forEach {
            totalesContent.add("${it.displayName},${it.totalEnergyJ}")
        }

        val totalesFile = csvDataSource.writeCsv("${timestamp}_totales.csv", totalesContent)

        // Persistencia en Room (registros - metadatos de CSV)
        val medicionesEntity = CaptureRecordEntity(
            fileName = "${timestamp}_mediciones.csv",
            durationMinutes = durationMinutes,
            samplingFrequencySeconds = samplingFrequency,
            dateMillis = System.currentTimeMillis(),
            filePath = medicionesFile.absolutePath
        )

        val totalesEntity = CaptureRecordEntity(
            fileName = "${timestamp}_totales.csv",
            durationMinutes = durationMinutes,
            samplingFrequencySeconds = samplingFrequency,
            dateMillis = System.currentTimeMillis(),
            filePath = totalesFile.absolutePath
        )

        captureRepository.saveRecord(medicionesEntity)
        captureRepository.saveRecord(totalesEntity)
    }
}