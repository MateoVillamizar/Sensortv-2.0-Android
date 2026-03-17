package com.sensortv.app.model

/**
 * Modelo de datos para representar un punto en el gráfico lineal de tiempo y potencia.
 */
data class SensorChartPoint (
    val timeStamp: Long,
    val powerMw: Float
)