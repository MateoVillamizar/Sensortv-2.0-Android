package com.sensortv.app.model

/**
 * Representa los resultados obtenidos de la medición de un sensor.
 *
 * @property type Tipo de sensor interno utilizado por Android.
 * @property displayName Nombre mostrado para el sensor.
 * @property powerMw Potencia medida en miliwatts (mW) producto de P = V * I.
 * @property averagePower Potencia promedio medida en miliwatts (mW).
 * @property frequencyHz Frecuencia de muestreo del sensor.
 */
data class SensorResult(
    val type: Int,
    val displayName: String,
    val powerMw: Float,
    val TotalConsume: Float,
    val frequencyHz: Float
)