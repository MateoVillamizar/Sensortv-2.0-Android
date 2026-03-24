package com.sensortv.app.data.repository

import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el puente entre datos crudos (DataSource) y lo que usará el ViewModel
 */
interface SensorRepository {
    fun observeSensors(): Flow<SensorData>
}