package com.sensortv.app.data.repository

import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que actúa como mediador para obtener datos de sensores
 */
interface SensorRepository {
    /**
     * Expone un flujo de sensores donde cada actualización incluye la potencia estimada calculada.
     *
     * @return [Flow] que emite [SensorData] ante nuevas lecturas del hardware.
     */
    fun observeSensors(): Flow<SensorData>
}