package com.sensortv.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sensortv.app.ui.model.ChartScale
import com.sensortv.app.ui.model.SensorChartConfig
import com.sensortv.app.ui.model.SensorChartData
import com.sensortv.app.ui.theme.SensorAccelerometer
import com.sensortv.app.ui.theme.SensorGyroscope
import com.sensortv.app.ui.theme.SensorLight
import com.sensortv.app.ui.theme.SensorMagnetometer
import com.sensortv.app.ui.theme.SensorProximity
import kotlin.math.ceil

@Composable
fun SensorPowerChart(
    chartDataList: List<SensorChartData>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val axisTextColor = MaterialTheme.colorScheme.onBackground
    val pointTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val axisColor = MaterialTheme.colorScheme.onSurface

    // Texto de ejes (números, labels)
    val axisPaint = remember(density) {
        android.graphics.Paint().apply {
            color = axisTextColor.toArgb()
            textSize = with(density) { 12.sp.toPx() } // tamaño escalable, convertir a pixeles
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    // Texto de los puntos (valores de potencia)
    val pointPaint = remember(density) {
        android.graphics.Paint().apply {
            color = pointTextColor.toArgb()
            textSize = with(density) { 8.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val yLabelPaint = remember(axisTextColor, density) {
        android.graphics.Paint(axisPaint).apply {
            textAlign = android.graphics.Paint.Align.LEFT
        }
    }

    Text(
        text = "Potencia (mW) vs Tiempo (Segundos)",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        textAlign = TextAlign.Center
    )

    val config = remember { SensorChartConfig() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
    ) {

        // Si no hay sensores o sensores sin datos, se muestra un mensaje
        if (
            chartDataList.isEmpty() ||
            chartDataList.all { it.points.isEmpty() }
            ) {
            drawContext.canvas.nativeCanvas.drawText(
                "Recolectando datos...",
                size.width / 2,
                size.height / 2,
                axisPaint
            )
            return@Canvas
        }

        // Dimensiones del gráfico

        val paddingLeft = config.paddingLeft
        val paddingBottom = config.paddingBottom
        val paddingTop = config.paddingTop
        val paddingRight = config.paddingRight

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingBottom - paddingTop

        // Escalas del gráfico

        val allPoints = chartDataList
        .asSequence()
        .flatMap { it.points }

        // Escala X (tiempo)
        val maxX = allPoints.maxOfOrNull { it.timeStamp } ?: 0f // Tiempo más grande / reciente
        val window = config.windowSize.coerceAtLeast(1f)
        val minX = (maxX - window).coerceAtLeast(0f) // Tiempo más pequeño / antiguo

        // Escala Y (potencia)
        val rawMaxY = allPoints
            .maxOfOrNull { it.powerMw }
            ?.coerceAtLeast(1f) ?: 1f
        val maxY = (rawMaxY * 1.1f).coerceAtLeast(1f) // Gráfica no toca borde superior

        // GRID del Eje Y
        val stepY = 1f

        // Redondear maxY al siguiente múltiplo de stepY
        val roundedMaxY = (ceil(maxY / stepY)) * stepY

        var currentY = 0f
        while (currentY <= roundedMaxY) {

            val y = paddingTop + chartHeight - (currentY / roundedMaxY) * chartHeight
            val textWidth = axisPaint.measureText(String.format("%.1f", currentY)) // Ancho del texto en pixeles

            val textX = paddingLeft - textWidth - 8f // 8px de margen
            val textY = y + axisPaint.textSize / 2 // centrado verticalmente

            drawLine(
                Color.LightGray.copy(alpha = 0.5f),
                Offset(paddingLeft, y),
                Offset(paddingLeft + chartWidth, y),
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

        // GRID del Eje X
        val stepX = 3f

        //  Línea vertical ajustada según stepX
        var currentX = (ceil(minX / stepX)) * stepX

        while (currentX <= maxX) {

            val normalizedX = (currentX - minX) / window
            val x = paddingLeft + normalizedX * chartWidth

            val textY = paddingTop + chartHeight + axisPaint.textSize + 4f

            // Dibujar línea vertical
            drawLine(
                Color.LightGray.copy(alpha = 0.5f),
                Offset(x, paddingTop),
                Offset(x, paddingTop + chartHeight),
                2f
            )

            // Dibujar texto abajo
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.0f", currentX),
                x,
                textY,
                axisPaint
            )
            currentX += stepX
        }

        // Ejes de la Gráfica

        // Eje Y
        drawLine(
            axisColor,
            Offset(paddingLeft, paddingTop),
            Offset(paddingLeft, paddingTop + chartHeight),
            2f
        )

        // Eje X
        drawLine(
            axisColor,
            Offset(paddingLeft, paddingTop + chartHeight),
            Offset(paddingLeft + chartWidth, paddingTop + chartHeight),
            2f
        )

        // Sensores (Líneas)

        chartDataList
            .sortedBy { it.sensorType } // consistencia de colores
            .forEach { sensorChart ->
                val color = getSensorColor(sensorChart.displayName)
                val visiblePoints = sensorChart.points.filter { it.timeStamp >= minX }

                val path = Path()
                visiblePoints.forEachIndexed { i, point ->

                    val x = paddingLeft + ((point.timeStamp - minX) / window) * chartWidth
                    val y = paddingTop + chartHeight - (point.powerMw / maxY) * chartHeight

                    if (i == 0) path.moveTo(x, y) // primer punto
                    else path.lineTo(x, y)

                    drawCircle(
                        color,
                        6f,
                        Offset(x, y)
                    )

                    if (visiblePoints.size < 15) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "%.2f".format(point.powerMw),
                            x,
                            if (sensorChart.displayName == "Sensor de Luz") {
                                y + 30f
                            } else {
                                y - 20f
                            },
                            pointPaint
                        )
                    }
                }

                // Dibuja línea del sensor
                drawPath(
                    path,
                    color,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }

        // Labels (Etiquetas)
        val xLabelY = paddingTop + chartHeight + axisPaint.textSize * 2
        val yLabelX = paddingLeft - axisPaint.textSize
        val yLabelY = paddingTop - 8f

        // Etiquetas X
        drawContext.canvas.nativeCanvas.drawText(
            "Tiempo (s)",
            paddingLeft + chartWidth / 2,
            xLabelY,
            axisPaint
        )

        // Etiquetas y
        drawContext.canvas.nativeCanvas.drawText(
            "(mW)",
            yLabelX,
            yLabelY,
            yLabelPaint
        )
    }

    Spacer(modifier = Modifier.height(25.dp))

    // Leyenda Dinámica

    // FlowRow para que los items salten de línea si no caben
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        chartDataList
            .sortedBy { it.sensorType }
            .forEach { sensorData ->
            LegendItem(
                name = sensorData.displayName,
                color = getSensorColor(sensorData.displayName) // El mismo color que en el Canvas
            )
        }
    }
}

@Composable
fun LegendItem(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        // Círculo de color que representa la línea
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getSensorColor(sensorName: String): Color {
    return when (sensorName) {
        "Sensor de Luz" -> SensorLight
        "Proximidad" -> SensorProximity
        "Acelerómetro" -> SensorAccelerometer
        "Magnetómetro" -> SensorMagnetometer
        "Giroscopio" -> SensorGyroscope
        else -> Color.Gray
    }
}

fun ChartScale.toX(time: Float): Float {
    val rangeX = (maxX - minX).coerceAtLeast(1f)
    return paddingLeft + ((time - minX) / rangeX) * chartWidth
}

fun ChartScale.toY(value: Float): Float {
    return paddingTop + chartHeight - (value / maxY) * chartHeight
}