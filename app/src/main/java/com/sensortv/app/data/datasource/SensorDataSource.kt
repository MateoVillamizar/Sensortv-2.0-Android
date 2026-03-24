package com.sensortv.app.data.datasource

import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el Contrato de la fuente de origen de los datos de los sensores.
 */
interface SensorDataSource {

    /**
     * Expone / emite un flujo continuo ([Flow]) de datos de sensores.
     * Cada vez que un sensor detecta un cambio, emite un nuevo objeto [SensorData].
     * @return Un flujo reactivo de datos de sensores.
     */
    fun observeSensorData(): Flow<SensorData>
}