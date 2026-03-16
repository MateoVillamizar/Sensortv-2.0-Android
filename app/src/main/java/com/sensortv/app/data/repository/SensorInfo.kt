package com.sensortv.app.data.repository

/**
 * Representa la información básica al detectar un sensor.
 */
data class SensorInfo(
    val sensorType: String,
    val isAvailable: Boolean,
    val sensorpower: Float
)