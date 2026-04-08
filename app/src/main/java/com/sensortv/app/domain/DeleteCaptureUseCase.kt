package com.sensortv.app.domain

import com.sensortv.app.data.datasource.CsvDataSource
import com.sensortv.app.data.model.CaptureRecordEntity
import com.sensortv.app.data.repository.CaptureRepository

/**
 * Caso de uso para eliminar de forma íntegra una captura.
 * Se encarga de borrar tanto el archivo físico como el registro en la base de datos.
 *
 * @param csvDataSource Implementación de [CsvDataSource] que gestiona la persistencia de archivos CSV.
 * @param captureRepository Implementación de [CaptureRepository] para el registro de metadatos en Room.
 */
class DeleteCaptureUseCase(
    private val csvDataSource: CsvDataSource,
    private val captureRepository: CaptureRepository
) {
    /**
     * Ejecuta la eliminación sincronizada.
     * 1. Borra el archivo físico del almacenamiento externo.
     * 2. Borra el registro de la base de datos Room.
     *
     * @param record Entidad que contiene la ruta del archivo y el ID en la BD.
     */
    suspend operator fun invoke(record: CaptureRecordEntity) {
        csvDataSource.deleteCsvFile(record.filePath)
        captureRepository.deleteRecord(record)
    }
}