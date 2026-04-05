package com.sensortv.app.ui.components.Chart

import com.sensortv.app.ui.model.ChartScale

/**
 * Convierte un valor de tiempo a coordenada X en el Canvas.
 *
 * - Normaliza el tiempo dentro del rango visible (minX..maxX)
 * - Escala al ancho del gráfico (* chartWidth)
 * - Aplica padding izquierdo (paddingLeft)
 *
 * @return Coordenada X en el Canvas.
 */
fun ChartScale.toX(time: Float): Float {
    return paddingLeft + ((time - minX) / window) * chartWidth
}

/**
 * Convierte un valor de potencia (mW) a coordenada Y en el Canvas.
 *
 * - Normaliza respecto al valor máximo (maxY)
 * - Escala al alto del gráfico (* chartHeight)
 * - Invierte el eje Y (Canvas tiene origen arriba) (chartHeight -)
 * - Aplica padding superior (paddingTop)
 *
 * @return Coordenada Y en el Canvas.
 */
fun ChartScale.toY(value: Float, maxY: Float = this.maxY): Float {
    return paddingTop + chartHeight - (value / maxY) * chartHeight
}