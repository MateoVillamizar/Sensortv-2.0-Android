package com.sensortv.app.ui.model

/**
 * Representa la información básica al monitorear un sensor.
 *
 * @property sensorName El nombre a mostrar del sensor.
 * @property hardware El identificador hardware del sensor a nivel de fabricante.
 * @property baseCurrentMa El valor nominal base del sensor en mA.
 * @property currentPowerMw El consumo actual estimado del sensor en mW.
 */
data class SensorMonitorInfo(
    val sensorName: String,
    val hardware: String,
    val baseCurrentMa: Float,
    val currentPowerMw: Float
)