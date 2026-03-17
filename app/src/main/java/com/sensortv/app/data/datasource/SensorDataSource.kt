package com.sensortv.app.data.datasource

import com.sensortv.app.model.SensorData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato para obtener datos de sensores en tiempo real.
 */
interface SensorDataSource {

    /**
     * Emite datos de sensores en tiempo real.
     * Cada emisión representa una actualización de un sensor.
     */
    fun observeSensorData(): Flow<SensorData>
}