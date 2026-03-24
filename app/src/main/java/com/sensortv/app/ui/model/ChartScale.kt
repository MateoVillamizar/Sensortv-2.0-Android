package com.sensortv.app.ui.model

/**
 * Modelo de datos para lógica de coordenadas de Canvas.
 *
 */
data class ChartScale(
    val minX: Float,
    val maxX: Float,
    val maxY: Float,
    val paddingLeft: Float,
    val paddingTop: Float,
    val chartWidth: Float,
    val chartHeight: Float
)