package com.sensortv.app.data.repository

import com.sensortv.app.data.datasource.BatteryDataSource
import com.sensortv.app.data.datasource.SensorDataSource
import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Implementación concreta de [SensorRepository] que coordina múltiples fuentes de datos
 * para calcular el consumo.
 *
 * @param sensorDataSource Origen de las lecturas de hardware (Corriente nominal).
 * @param batteryDataSource Origen del estado eléctrico del dispositivo (Voltaje).
 */
class SensorRepositoryImpl(
    private val sensorDataSource: SensorDataSource,
    private val batteryDataSource: BatteryDataSource
) : SensorRepository {

    /**
     * Combina las lecturas de los sensores con el voltaje de la batería en tiempo real.
     * * El uso de [combine] permite que la potencia estimada (P = V * I) se actualice
     * instantáneamente si cambia cualquiera de los dos factores.
     *
     * @return [Flow] reactivo que emite [SensorData] actualizado con la potencia estimada calculada
     * cada vez que cambian los datos de los sensores o el voltaje de la batería.
     */
    override fun observeSensors(): Flow<SensorData> {
        return combine(
            sensorDataSource.observeSensorData(),
            batteryDataSource.observeBattery()
        ) { sensorData, batteryData ->

            // Cálculo de potencia: P = V * I -> Voltios (V) * Miliamperios (mA) = Miliwatts (mW)
            val estimatedPowermW = calculatePower(
                currentMa = sensorData.nominalConsumptionmA,
                batteryVoltage = batteryData.voltage
            )

            // Retornar una copia del modelo con el nuevo valor calculado
            sensorData.copy(
                estimatedPowerMw = estimatedPowermW
            )
        }
    }

    // NECESARIO reestructurar esta operación para no mezclar responsabilidad de repositorios
    /**
     * Realiza una estimación de la potencia consumida por el sensor en miliwatts (mW).
     *
     * @param currentMa La corriente nominal reportada por el sensor (mA).
     * @param batteryVoltage El voltaje actual de la batería (V).
     * @return La potencia calculada en mW.
     */
    private fun calculatePower(currentMa: Float, batteryVoltage: Float): Float {
        return currentMa * batteryVoltage
    }
}