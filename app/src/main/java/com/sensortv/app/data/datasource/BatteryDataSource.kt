package com.sensortv.app.data.datasource

import com.sensortv.app.data.model.BatteryData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato para obtener datos del estado de la batería.
 */
interface BatteryDataSource {
    /**
     * Expone un flujo de datos en tiempo real sobre el porcentaje y voltaje.
     *
     * @return [Flow] que emite [BatteryData] ante cada cambio del sistema.
     */
    fun observeBattery(): Flow<BatteryData>
}