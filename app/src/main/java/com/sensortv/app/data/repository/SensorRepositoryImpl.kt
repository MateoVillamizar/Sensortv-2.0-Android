package com.sensortv.app.data.repository

import com.sensortv.app.data.datasource.BatteryDataSource
import com.sensortv.app.data.datasource.SensorDataSource
import com.sensortv.app.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Implementación concreta de [SensorRepository] que interactúa con [SensorDataSource].
 *
 * @param dataSource Fuente para obtener los datos de sensores.
 */
class SensorRepositoryImpl(
    private val sensorDataSource: SensorDataSource,
    private val batteryDataSource: BatteryDataSource
) : SensorRepository {

    /**
     * Observa el flujo de datos de los sensores y enriquece cada emisión con
     * cálculos de consumo basados en el estado actual de la batería.
     * @return Un [Flow] de [SensorData] actualizado.
     */
    override fun observeSensors(): Flow<SensorData> {
        return combine(
            sensorDataSource.observeSensorData(),
            batteryDataSource.observeBattery()
        ) { sensorData, batteryData ->

            // P = V * I -> Voltaje (V) * Corriente (mA) = Potencia (mW)
            val powermW = calculatePower(
                currentMa = sensorData.nominalConsumptionmA,
                batteryVoltage = batteryData.voltage
            )

            sensorData.copy(
                estimatedPowerMw = powermW
            )
        }
    }

    /**
     * Calcula la potencia consumida por el sensor en miliwatts.
     * @param currentMa La corriente nominal reportada por el sensor (mA).
     * @param batteryVoltage El voltaje actual de la batería (V).
     * @return La potencia calculada en mW.
     */
    private fun calculatePower(currentMa: Float, batteryVoltage: Float): Float {
        return currentMa * batteryVoltage
    }
}