package com.sensortv.app.data.model

/**
 * Resumen optimizado de los resultados de medición para visualización o persistencia.
 * * Este modelo actúa como una "vista" simplificada de [SensorData], eliminando
 * metadatos de hardware innecesarios para el registro final.
 *
 * @property type Identificador único (ej. Sensor.TYPE_ACCELEROMETER).
 * @property displayName Nombre legible del sensor para el usuario (ej. "Acelerómetro").
 * @property estimatedPowerMw Potencia estimada del sensor en miliwatts (mW) mediante la relación P = V * I.
 * //@property TotalConsume Consumo acumulado estimado del sensor (expresado en Joules).
 */

// NO SE TIENE CLARO SI SEGUIRÁ EXISTIENDO O SE DEBE MODIFICAR. PENDIENTE
data class SensorResult(
    val type: Int,
    val displayName: String,

    val estimatedPowerMw: Float,
    //val totalConsume: Float,
)