package com.sensortv.app.ui.components.Chart

import android.graphics.Paint
import android.hardware.Sensor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.sensortv.app.ui.model.ChartScale
import com.sensortv.app.ui.model.SensorChartConfig
import com.sensortv.app.ui.model.SensorChartData
import kotlin.math.ceil

/**
 * Dibuja las líneas horizontales del grid y sus etiquetas (eje Y) junto con sus
 * etiquetas numéricas. Usa stepY como separación entre valores.
 *
 * @param chartScale Escala calculada del gráfico (coordenadas).
 * @param config Configuración del gráfico (incluye stepY).
 * @param axisPaint Paint utilizado para dibujar los valores del eje Y.
 */
fun DrawScope.drawGridY(
    chartScale: ChartScale,
    config: SensorChartConfig,
    axisPaint: Paint
) {
    val stepY = config.stepY
    val maxY = chartScale.maxY

    val roundedMaxY = (ceil(maxY / stepY)) * stepY
    var currentY = 0f

    while (currentY < roundedMaxY) {

        val y = chartScale.toY(currentY)

        val textWidth = axisPaint.measureText(String.format("%.1f", currentY))
        val textX = chartScale.paddingLeft - textWidth - 8f
        val textY = y + axisPaint.textSize / 2

        drawLine(
            Color.LightGray.copy(alpha = 0.5f),
            Offset(chartScale.paddingLeft, y),
            Offset(chartScale.paddingLeft + chartScale.chartWidth, y),
            2f
        )

        drawContext.canvas.nativeCanvas.drawText(
            String.format("%.1f", currentY),
            textX,
            textY,
            axisPaint
        )

        currentY += stepY
    }
}

/**
 * Dibuja las líneas verticales del grid y las etiquetas de tiempo (eje X) junto con
 * sus etiquetas de tiempo. Alineadas al stepX definido en la configuración.
 *
 * @param chartScale Escala calculada del gráfico
 * @param config Configuración del gráfico (incluye stepX).
 * @param axisPaint Paint utilizado para dibujar los valores del eje X.
 */
fun DrawScope.drawGridX(
    chartScale: ChartScale,
    config: SensorChartConfig,
    axisPaint: Paint
) {
    val stepX = config.stepX

    var currentX = (ceil(chartScale.minX / stepX)) * stepX

    while (currentX <= chartScale.maxX) {

        val x = chartScale.toX(currentX)

        val textY = chartScale.paddingTop + chartScale.chartHeight + axisPaint.textSize + 4f

        drawLine(
            Color.LightGray.copy(alpha = 0.5f),
            Offset(x, chartScale.paddingTop),
            Offset(x, chartScale.paddingTop + chartScale.chartHeight),
            2f
        )

        drawContext.canvas.nativeCanvas.drawText(
            String.format("%.0f", currentX),
            x,
            textY,
            axisPaint
        )

        currentX += stepX
    }
}

/**
 * Dibuja los ejes principales del gráfico (X e Y).
 *
 * @param chartScale Escala calculada del gráfico.
 * @param axisColor Color de los ejes.
 */
fun DrawScope.drawAxes(
    chartScale: ChartScale,
    axisColor: Color
) {
    // Eje Y
    drawLine(
        axisColor,
        Offset(chartScale.paddingLeft, chartScale.paddingTop),
        Offset(chartScale.paddingLeft, chartScale.paddingTop + chartScale.chartHeight),
        2f
    )

    // Eje X
    drawLine(
        axisColor,
        Offset(chartScale.paddingLeft, chartScale.paddingTop + chartScale.chartHeight),
        Offset(chartScale.paddingLeft + chartScale.chartWidth, chartScale.paddingTop + chartScale.chartHeight),
        2f
    )
}

/**
 * Dibuja las líneas y puntos de cada sensor en el gráfico.
 *
 * - Filtra puntos visibles según la ventana actual del gráfico en tiempo (minX)
 * - Convierte cada punto a coordenadas usando ChartScale.
 * - Dibuja círculos en cada punto y opcionalmente etiquetas de valor.
 *
 * @param chartDataList Lista de sensores con sus datos.
 * @param chartScale Escala del gráfico.
 * @param pointPaint Paint utilizado para dibujar los valores de los puntos.
 */
fun DrawScope.drawSensorLines(
    chartDataList: List<SensorChartData>,
    chartScale: ChartScale,
    pointPaint: Paint
) {
    chartDataList
        .sortedBy { it.sensorType }
        .forEach { sensorChart ->

            val color = getSensorColor(sensorChart.sensorType)
            val visiblePoints = sensorChart.points.filter { it.timeStamp >= chartScale.minX }

            val path = Path()

            visiblePoints.forEachIndexed { index, point ->

                val x = chartScale.toX(point.timeStamp)
                val y = chartScale.toY(point.powerMw)

                if (index == 0) path.moveTo(x, y)
                else path.lineTo(x, y)

                drawCircle(color, 6f, Offset(x, y))

                // Dibuja texto del punto solo si hay suficiente espacio horizontal (en píxeles) respecto al punto anterior.
                val shouldDrawTextPoint = if (index == 0) {
                    true
                } else {
                    val previousX = chartScale.toX(visiblePoints[index - 1].timeStamp)
                    (x - previousX) > 50f
                }

                if (shouldDrawTextPoint) {
                    drawContext.canvas.nativeCanvas.drawText(
                        "%.2f".format(point.powerMw),
                        x,
                        if (sensorChart.sensorType == Sensor.TYPE_LIGHT) {
                            y + 30f
                        } else {
                            y - 20f
                        },
                        pointPaint
                    )
                }
            }

            drawPath(
                path,
                color,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
}

/**
 * Dibuja las etiquetas correspondientes al eje X (tiempo) y el eje Y (potencia).
 *
 * @param chartScale Escala del gráfico.
 * @param axisPaint Paint utilizado para dibujar la etiqueta del eje X.
 * @param yAxisPaint Paint utilizado para dibujar la etiqueta del eje Y.
 */
fun DrawScope.drawLabels(
    chartScale: ChartScale,
    axisPaint: Paint,
    yAxisPaint: Paint
) {
    val xLabelX = chartScale.paddingLeft + chartScale.chartWidth / 2
    val xLabelY = chartScale.paddingTop + chartScale.chartHeight + axisPaint.textSize * 3

    val yLabelX = chartScale.paddingLeft - axisPaint.textSize
    val yLabelY = chartScale.paddingTop / 2

    drawContext.canvas.nativeCanvas.drawText(
        "Tiempo (s)",
        xLabelX,
        xLabelY,
        axisPaint
    )

    drawContext.canvas.nativeCanvas.drawText(
        "(mW)",
        yLabelX,
        yLabelY,
        yAxisPaint
    )
}