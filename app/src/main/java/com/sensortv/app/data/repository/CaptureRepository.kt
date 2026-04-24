package com.sensortv.app.data.repository

import com.sensortv.app.data.model.CaptureRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que gestiona la persistencia de los metadatos de las capturas.
 */
interface CaptureRepository {
    /**
     * Obtiene todos los registros almacenados.
     */
    fun getRecords(): Flow<List<CaptureRecordEntity>>

    /**
     * Persiste un nuevo registro de captura.
     */
    suspend fun saveRecord(record: CaptureRecordEntity)

    /**
     * Elimina un registro existente.
     */
    suspend fun deleteRecord(record: CaptureRecordEntity)
}