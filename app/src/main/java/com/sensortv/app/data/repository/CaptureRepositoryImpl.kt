package com.sensortv.app.data.repository

import com.sensortv.app.data.datasource.CaptureDao
import com.sensortv.app.data.model.CaptureRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Implementación de [CaptureRepository] que utiliza Room como fuente de datos.
 *
 * @param captureDao Instancia de [CaptureDao] que proporciona acceso a la base de datos.
 */
class CaptureRepositoryImpl(private val captureDao: CaptureDao) : CaptureRepository {
    override fun getRecords(): Flow<List<CaptureRecordEntity>> = captureDao.getAllRecords()
    override suspend fun saveRecord(record: CaptureRecordEntity) = captureDao.insertRecord(record)
    override suspend fun deleteRecord(record: CaptureRecordEntity) = captureDao.deleteRecord(record)
}