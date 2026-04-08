package com.sensortv.app.data.repository

import com.sensortv.app.data.model.CaptureRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que gestiona la persistencia de los metadatos de las capturas.
 */
interface CaptureRepository {
    fun getRecords(): Flow<List<CaptureRecordEntity>>
    suspend fun saveRecord(record: CaptureRecordEntity)
    suspend fun deleteRecord(record: CaptureRecordEntity)
}