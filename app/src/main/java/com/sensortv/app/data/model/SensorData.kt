package com.sensortv.app.data.model

/**
 * Modelo integral de datos que captura el estado y la identidad de un sensor físico.
 *
 * @property type Identificador único (ej. Sensor.TYPE_ACCELEROMETER).
 * @property displayName Nombre legible del sensor para el usuario (ej. "Acelerómetro").
 * @property hardwareName Identificador del componente físico del fabricante.
 * @property values Lista de valores crudos capturados (ejes X,Y,Z o valores únicos).
 * @property frequencyHz Velocidad de muestreo actual del sensor calculada en Hertz (Hz).
 * @property isAvailable Indica si el hardware del sensor está presente y operativo en el dispositivo.
 * @property nominalConsumptionmA Corriente teórica fija consumida por el sensor en mA. (miliamperios).
 * @property estimatedPowerMw Potencia estimada del sensor en miliwatts (mW)
 * calculada como Voltaje Batería * nominalConsumptionmA.
 */
data class SensorData(
    val type: Int,
    val displayName: String,
    val hardwareName: String,

    val values: List<Float> = emptyList(),
    val frequencyHz: Float = 0f,

    val isAvailable: Boolean = false,
    val nominalConsumptionmA: Float = 0f,
    val estimatedPowerMw: Float = 0f
)