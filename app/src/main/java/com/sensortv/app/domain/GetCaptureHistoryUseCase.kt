package com.sensortv.app.domain

import com.sensortv.app.data.model.CaptureRecordEntity
import com.sensortv.app.data.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para obtener el flujo de registros históricos.
 */
class GetCaptureHistoryUseCase(
    private val captureRepository: CaptureRepository
) {
    /**
     * Retorna un Flow con la lista de capturas, permitiendo actualizaciones
     * en tiempo real en la UI cuando se elimine un registro de la base de datos
     */
    operator fun invoke(): Flow<List<CaptureRecordEntity>> {
        return captureRepository.getRecords()
    }
}