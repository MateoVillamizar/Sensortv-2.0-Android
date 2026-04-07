package com.sensortv.app.data.model

/**
 * Resumen optimizado de los resultados de medición para persistencia y CSV.
 * Este modelo actúa como los datos para el registro final.
 *
 * @property sensorType Identificador único (ej. Sensor.TYPE_ACCELEROMETER).
 * @property displayName Nombre legible del sensor para el usuario (ej. "Acelerómetro").
 * @property estimatedPowerMw Potencia estimada del sensor en miliwatts (mW) mediante la relación P = V * I.
 * @property totalConsume Consumo acumulado estimado del sensor (expresado en Joules).
 */

data class SensorResult(
    val sensorType: Int,
    val displayName: String,
    val estimatedPowerMw: Float,
    val totalConsume: Float,
)