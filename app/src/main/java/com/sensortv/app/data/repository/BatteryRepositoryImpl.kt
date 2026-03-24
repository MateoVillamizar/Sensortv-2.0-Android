package com.sensortv.app.data.repository

import com.sensortv.app.data.datasource.BatteryDataSource
import com.sensortv.app.data.model.BatteryData
import kotlinx.coroutines.flow.Flow

class BatteryRepositoryImpl(
    private val batteryDataSource: BatteryDataSource
) : BatteryRepository {

    /**
     * Observa los cambios en el estado de la batería mediante [BatteryDataSource].
     *
     * @return Un [Flow] que emite [BatteryData] cada vez que el estado de la batería cambia.
     */
    override fun observeBattery(): Flow<BatteryData> = batteryDataSource.observeBattery()
}