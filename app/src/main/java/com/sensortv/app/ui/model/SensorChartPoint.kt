package com.sensortv.app.ui.model

/**
 * Modelo de UI para representar un punto en el gráfico lineal de potencia estimada vs tiempo.
 *
 * @property timeStamp Valor de tiempo (eje X) asociado al punto en la gráfica.
 * @property powerMw Valor de potencia en mW (eje y) asociado al punto en la gráfica.
 */
data class SensorChartPoint (
    val timeStamp: Float,
    val powerMw: Float
)