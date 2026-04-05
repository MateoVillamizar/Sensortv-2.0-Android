package com.sensortv.app.ui.model

/**
 * Modelo de datos para representar un sensor en el gráfico lineal de potencia estimada en tiempo real.
 *
 * @property sensorType Identificador del tipo de sensor.
 * @property displayName Nombre a mostrar del sensor.
 * @property points Lista de puntos de datos asociados al sensor.
 */
data class SensorChartData(
    val sensorType: Int,
    val displayName: String,
    val points: List<SensorChartPoint>
)