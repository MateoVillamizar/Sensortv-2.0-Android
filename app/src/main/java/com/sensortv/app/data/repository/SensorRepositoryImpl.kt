package com.sensortv.app.data.repository

import com.sensortv.app.data.datasource.SensorDataSource
import com.sensortv.app.model.SensorData
import kotlinx.coroutines.flow.Flow

/**
 * Implementación concreta de [SensorRepository] que interactúa con [SensorDataSource].
 *
 * @param dataSource Fuente para obtener los datos de sensores.
 */
class SensorRepositoryImpl(
    private val dataSource: SensorDataSource
) : SensorRepository {

    override fun observeSensors(): Flow<SensorData> {
        return dataSource.observeSensorData()
    }
}