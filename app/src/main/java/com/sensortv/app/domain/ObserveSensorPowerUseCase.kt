package com.sensortv.app.domain

import com.sensortv.app.data.model.SensorData
import com.sensortv.app.data.repository.BatteryRepository
import com.sensortv.app.data.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Caso de uso para obtener el flujo de sensores con la potencia calculada.
 * Encapsula la lógica de negocio: Potencia (mW) = Corriente nominal (mA) * Voltaje actual (V).
 *
 * @param sensorRepository Fuente de datos de los sensores (Contiene corriente nominal).
 * @param batteryRepository Fuente de datos de la batería (Contiene voltaje actual).
 */
class ObserveSensorPowerUseCase(
    private val sensorRepository: SensorRepository,
    private val batteryRepository: BatteryRepository
) {
    /**
     * Combina los flujos de sensores y batería para calcular la potencia en tiempo real.
     *
     * @return [Flow] de [SensorData] donde cada objeto contiene el cálculo de [estimatedPowerMw].
     */
    operator fun invoke(): Flow<SensorData> {
        return combine(
            sensorRepository.observeSensors(),
            batteryRepository.observeBattery()
        ) { sensorData, batteryData ->
            // Cálculo de potencia: P = V * I : Voltios (V) * Miliamperios (mA) = Miliwatts (mW)
            val estimatedPowermW =  batteryData.voltage * sensorData.nominalConsumptionmA

            sensorData.copy(estimatedPowerMw = estimatedPowermW)
        }
    }
}