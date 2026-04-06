package com.sensortv.app.ui.model

/**
 * Representa la información básica al detectar un sensor.
 *
 * @property sensorType El tipo de sensor detectado.
 * @property isAvailable Indica si el sensor está disponible en el dispositivo.
 * @property sensorPower Consumo nominal fijo estimado por fabricante en mA.
 */
data class SensorInfo(
    val sensorType: String,
    val isAvailable: Boolean,
    val sensorPower: Float
)