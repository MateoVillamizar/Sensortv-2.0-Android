package com.sensortv.app.ui.model

/**
 * Modelo de UI para representar un punto en el gráfico lineal potencia estimada vs tiempo.
 *
 * @property timeStamp Valor de tiempo asociado al punto en la gráfica.
 * @property powerMw Valor de potencia en mW asociado al punto en la gráfica.
 */
data class SensorChartPoint (
    val timeStamp: Float,
    val powerMw: Float
)