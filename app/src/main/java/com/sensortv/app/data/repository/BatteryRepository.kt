package com.sensortv.app.data.repository

import com.sensortv.app.data.model.BatteryData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de la capa de datos que gestiona la información de la batería.
 * Actúa como el punto de acceso principal para el resto de la aplicación.
 */
interface BatteryRepository {
    /**
     * Provee un flujo observable con el estado de la batería del dispositivo.
     *
     * @return [Flow] que emite [BatteryData] cada vez hay un cambio en el estado de la batería.
     */
    fun observeBattery(): Flow<BatteryData>
}