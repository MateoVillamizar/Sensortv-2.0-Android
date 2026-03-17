package com.sensortv.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sensortv.app.model.SensorChartData

@Composable
fun SensorPowerChart(
    chartDataList: List<SensorChartData>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Magenta, Color.Cyan)

    val axisPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = with(density) { 12.sp.toPx() } // tamaño escalable
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val pointPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = with(density) { 8.sp.toPx() } // tamaño escalable
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Text(
        text = "Potencia (mW) vs Tiempo (Segundos)",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        textAlign = TextAlign.Center
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {

        if (chartDataList.isEmpty() || chartDataList.all { it.points.isEmpty() }) {
            drawContext.canvas.nativeCanvas.drawText(
                "Recolectando datos...",
                size.width / 2,
                size.height / 2,
                axisPaint
            )
            return@Canvas
        }

        // ========================
        // DIMENSIONES
        // ========================
        val paddingLeft = 60f
        val paddingBottom = 80f
        val paddingTop = 60f // Espacio para el "(mW)"

        val chartWidth = size.width - paddingLeft - 20f
        val chartHeight = size.height - paddingBottom - paddingTop

        // ========================
        // ESCALAS
        // ========================
        val allPoints = chartDataList.flatMap { it.points }

        val maxX = allPoints.maxOfOrNull { it.timeStamp } ?: 0f
        val window = 20f
        val minX = (maxX - window).coerceAtLeast(0f)

        val rawMaxY = allPoints.maxOfOrNull { it.powerMw }?.coerceAtLeast(1f) ?: 1f
        val maxY = rawMaxY * 1.1f

        // ========================
        // GRID Y (dinámico)
        // ========================
        val stepY = 1f

        // Redondear maxY al siguiente múltiplo de 0.5
        val roundedMaxY = (kotlin.math.ceil(maxY / stepY)) * stepY

        var currentY = 0f

        while (currentY <= roundedMaxY) {

            val y = paddingTop + chartHeight - (currentY / roundedMaxY) * chartHeight

            drawLine(
                Color.LightGray.copy(alpha = 0.5f),
                Offset(paddingLeft, y),
                Offset(paddingLeft + chartWidth, y),
                2f
            )

            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.1f", currentY),
                paddingLeft - 60f,
                y + 10f,
                axisPaint
            )

            currentY += stepY
        }

    // ========================
    // GRID X (Lógica simplificada tipo Y)
    // ========================
        val stepX = 3f
    // Buscamos el primer múltiplo de 3 justo después de minX para que el grid no salte
        var currentX = (kotlin.math.ceil(minX / stepX)) * stepX

        while (currentX <= maxX) {
            // Calculamos posición relativa a la ventana de 20s
            val normalizedX = (currentX - minX) / window
            val x = paddingLeft + normalizedX * chartWidth

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
                paddingTop + chartHeight + 50f,
                axisPaint
            )

            currentX += stepX
        }

        // ========================
        // EJES
        // ========================
        drawLine(
            Color.Black,
            Offset(paddingLeft, paddingTop),
            Offset(paddingLeft, paddingTop + chartHeight),
            4f
        )

        drawLine(
            Color.Black,
            Offset(paddingLeft, paddingTop + chartHeight),
            Offset(paddingLeft + chartWidth, paddingTop + chartHeight),
            4f
        )

        // ========================
        // SENSORES (ENCIMA DE TO_DO)
        // ========================
        chartDataList
            .sortedBy { it.sensorType } // estabilidad de colores
            .forEach { sensorChart ->
                // USAMOS LA FUNCIÓN PARA GARANTIZAR EL COLOR
                val color = getSensorColor(sensorChart.displayName)
                val path = Path()

                val visiblePoints = sensorChart.points.filter { it.timeStamp >= minX }

                visiblePoints.forEachIndexed { i, point ->

                    val x = paddingLeft + ((point.timeStamp - minX) / window) * chartWidth
                    val y = paddingTop + chartHeight - (point.powerMw / maxY) * chartHeight

                    if (i == 0) path.moveTo(x, y)
                    else path.lineTo(x, y)

                    drawCircle(color, 5f, Offset(x, y))

                    if (visiblePoints.size < 15) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "%.2f".format(point.powerMw),
                            x,
                            y - 20f,
                            pointPaint
                        )
                    }
                }

                drawPath(
                    path,
                    color,
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
            }

        // ========================
        // LABELS
        // ========================

        // X
        drawContext.canvas.nativeCanvas.drawText(
            "Tiempo (s)",
            paddingLeft + chartWidth / 2,
            paddingTop + chartHeight + 100f,
            axisPaint
        )

        // Y (Texto en la parte superior, sin rotar)
        drawContext.canvas.nativeCanvas.drawText(
            "(mW)",
            paddingLeft - 10f,
            paddingTop - 25f,
            axisPaint.apply { textAlign = android.graphics.Paint.Align.LEFT }
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // --- LEYENDA DINÁMICA ---
    // Usamos FlowRow para que los items salten de línea si no caben
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        chartDataList.forEach { sensorData ->
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
                .size(10.dp)
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
    val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Magenta, Color.Cyan, Color.Yellow)
    // Usamos el hashCode del nombre para que siempre devuelva el mismo índice para el mismo nombre
    val index = Math.abs(sensorName.hashCode()) % colors.size
    return colors[index]
}