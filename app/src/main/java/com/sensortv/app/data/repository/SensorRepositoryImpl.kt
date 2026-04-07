package com.sensortv.app.data.repository

import com.sensortv.app.data.datasource.SensorDataSource
import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.flow.Flow

/**
 * Implementación concreta de [SensorRepository] que se encarga de exponer los flujos de
 * datos de los sensores provenientes de la fuente de hardware.
 *
 * @param sensorDataSource Origen de las lecturas de hardware (Corriente nominal, valores, frecuencia).
 */
class SensorRepositoryImpl(
    private val sensorDataSource: SensorDataSource,
) : SensorRepository {
    /**
     * Expone el flujo de datos de sensores sin procesar lógica de negocio compleja.
     *
     * @return [Flow] reactivo que emite [SensorData] con la información del hardware.
     */
    override fun observeSensors(): Flow<SensorData> = sensorDataSource.observeSensorData()
}