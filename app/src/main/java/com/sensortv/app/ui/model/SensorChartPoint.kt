package com.sensortv.app.ui.model

/**
 * Modelo de datos para representar un punto en el gráfico lineal de tiempo y potencia.
 */
data class SensorChartPoint (
    val timeStamp: Float,
    val powerMw: Float
)