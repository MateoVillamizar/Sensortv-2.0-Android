package com.sensortv.app.data.datasource

import com.sensortv.app.data.model.BatteryData

/**
 * Interfaz que define el Contrato de la fuente de origen de los datos de la batería.
 */
interface BatteryDataSource {

    fun observeBattery(): kotlinx.coroutines.flow.Flow<BatteryData>
}