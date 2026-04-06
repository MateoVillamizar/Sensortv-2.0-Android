package com.sensortv.app.data.repository

import com.sensortv.app.data.datasource.BatteryDataSource
import com.sensortv.app.data.model.BatteryData
import kotlinx.coroutines.flow.Flow

/**
 * Implementación concreta de [BatteryRepository].
 * Actúa como un puente directo hacia [BatteryDataSource].
 *
 * @param batteryDataSource Fuente de datos que provee la información de la batería.
 */
class BatteryRepositoryImpl(
    private val batteryDataSource: BatteryDataSource
) : BatteryRepository {

    /**
     * Observa los cambios en el estado de la batería mediante [BatteryDataSource].
     *
     * @return [Flow] que emite [BatteryData] cada vez que hay un cambio
     * en el nivel o voltaje de la batería.
     */
    override fun observeBattery(): Flow<BatteryData> = batteryDataSource.observeBattery()
}