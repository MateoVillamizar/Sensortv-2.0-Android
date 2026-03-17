package com.sensortv.app.model

/**
 * Representa la información básica al monitorear un sensor.
 */
data class SensorMonitorInfo(
    val sensorName: String,
    val hardware: String,
    val baseCurrentMa: Float,
    val currentPowerMw: Float
)