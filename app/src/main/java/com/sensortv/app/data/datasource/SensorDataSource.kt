package com.sensortv.app.data.datasource

import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato para la obtención de datos de los sensores de hardware.
 */
interface SensorDataSource {
    /**
     * Expone un flujo continuo de datos de varios sensores en tiempo real.
     *
     * @return [Flow] que emite [SensorData] cada vez que se detecta un cambio en los sensores
     */
    fun observeSensorData(): Flow<SensorData>
}