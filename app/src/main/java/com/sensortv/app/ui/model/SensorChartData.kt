package com.sensortv.app.ui.model

/**
 * Modelo de datos para representar un sensor en el gráfico lineal de tiempo y potencia.
 */
data class SensorChartData(
    val sensorType: Int,
    val displayName: String,
    val points: List<SensorChartPoint>
)