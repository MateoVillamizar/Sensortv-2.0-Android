package com.sensortv.app.ui.model

/**
 * Modelo para la configuración general del gráfico Canvas de potencia estimada en tiempo real.
 *
 * @property paddingLeft Espaciado izquierdo del gráfico.
 * @property paddingBottom Espaciado inferior del gráfico.
 * @property paddingTop Espaciado superior del gráfico.
 * @property paddingRight Espaciado derecho del gráfico.
 * @property stepX Incremento en el eje X (tiempo).
 * @property stepY Incremento en el eje Y (potencia).
 * @property windowSize Tamaño de la ventana de tiempo visible para el gráfico.
 */
data class SensorChartConfig(
    val paddingLeft: Float = 60f,
    val paddingBottom: Float = 80f,
    val paddingTop: Float = 60f,
    val paddingRight: Float = 20f,

    val stepX: Float = 3f,
    val stepY: Float = 1f,

    val windowSize: Float = 22f
)