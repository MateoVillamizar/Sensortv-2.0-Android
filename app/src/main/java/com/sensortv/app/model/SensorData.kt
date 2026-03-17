package com.sensortv.app.model

/**
 * Modela los datos asociados a un sensor.
 *
 * @property type Tipo de sensor interno utilizado por Android.
 * @property displayName Nombre mostrado para el sensor.
 * @property hardwareName Nombre del hardware del sensor.
 * @property values Lista de valores del sensor.
 * @property frequencyHz Frecuencia de muestreo del sensor.
 * @property available Indica si el sensor está disponible.
 * @property estimatedConsumption Consumo estimado del sensor por el fabricante en mA (miliamperios).
 */
data class SensorData(
    val type: Int,
    val displayName: String,
    val hardwareName: String,

    val values: List<Float> = emptyList(),
    val frequencyHz: Float = 0f,

    val available: Boolean = false,

    val estimatedConsumption: Float = 0f
)