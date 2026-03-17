package com.sensortv.app.model

/**
 * Representa los resultados obtenidos de la medición de un sensor.
 *
 * @property type Identificador numérico del tipo de sensor según la clase [android.hardware.Sensor].
 * @property displayName Nombre legible para mostrar en la interfaz de usuario.
 * @property powerMw Potencia instantánea calculada en miliwatts (mW) mediante la relación P = V * I.
 * //@property TotalConsume Consumo total acumulado (usualmente expresado en mAh o Joules).
 * @property frequencyHz Frecuencia de muestreo actual medida en Hertz (Hz).
 */
data class SensorResult(
    val type: Int,
    val displayName: String,

    val powerMw: Float,
    //val totalConsume: Float,
    val frequencyHz: Float
)