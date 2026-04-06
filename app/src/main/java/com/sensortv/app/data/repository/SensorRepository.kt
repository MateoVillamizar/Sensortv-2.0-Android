package com.sensortv.app.data.repository

import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que actúa como mediador para obtener datos de sensores ya procesados.
 * El repositorio entrega información enriquecida con cálculos de negocio
 * (como el consumo energético).
 */
interface SensorRepository {
    /**
     * Expone un flujo de sensores donde cada actualización incluye la potencia estimada calculada.
     *
     * @return [Flow] que emite [SensorData] cada vez que hay un cambio en los sensores.
     */
    fun observeSensors(): Flow<SensorData>
}