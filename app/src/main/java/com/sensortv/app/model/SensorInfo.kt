package com.sensortv.app.model

/**
 * Representa la información básica al detectar un sensor.
 */
data class SensorInfo(
    val sensorType: String,
    val isAvailable: Boolean,
    val sensorPower: Float
)