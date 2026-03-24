package com.sensortv.app.data.repository

import com.sensortv.app.data.model.BatteryData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el Contrato de la fuente de origen de los datos de la batería.
 */
interface BatteryRepository {
    fun observeBattery(): Flow<BatteryData>
}