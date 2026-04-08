package com.sensortv.app.data.model

/**
 * Representa el resultado final de un sensor tras una sesión de captura.
 * Incluye el cálculo de energía acumulada.
 *
 * @property sensorType Identificador único (ej. Sensor.TYPE_ACCELEROMETER).
 * @property displayName Nombre legible del sensor para el usuario (ej. "Acelerómetro").
 * @property estimatedPowerMw Potencia estimada del sensor en miliwatts (mW) mediante la relación P = V * I.
 * @property totalEnergyJ Consumo acumulado estimado del sensor expresado en Joules (J).
 * @property timestamp Marca de tiempo en formato ISO 8601 (UTC).
 */
data class SensorResult(
    val sensorType: Int,
    val displayName: String,
    val estimatedPowerMw: Float,
    val totalEnergyJ: Float,
    val timestamp: String
)