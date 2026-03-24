package com.sensortv.app.data.model

/**
 * Modela los datos asociados a un sensor.
 *
 * @property type Tipo de sensor definido por las constantes de [android.hardware.Sensor].
 * @property displayName Nombre simplificado del sensor para el usuario.
 * @property hardwareName Nombre técnico del componente físico del fabricante.
 * @property values Lista de valores capturados (ej. aceleración en m/s² para los ejes X, Y, Z).
 * @property frequencyHz Velocidad de actualización del sensor calculada en tiempo real.
 * @property available Indica si el hardware del sensor está presente y operativo en el dispositivo.
 * @property nominalConsumptionmA Consumo de corriente del sensor estimada por el fabricante en mA (miliamperios).
 */
data class SensorData(
    val type: Int,
    val displayName: String,
    val hardwareName: String,

    val values: List<Float> = emptyList(),
    val frequencyHz: Float = 0f,

    val available: Boolean = false,
    val nominalConsumptionmA: Float = 0f,
    val estimatedPowerMw: Float = 0f
)