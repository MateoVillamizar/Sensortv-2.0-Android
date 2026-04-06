package com.sensortv.app.ui.components.Chart

import android.graphics.Paint
import android.hardware.Sensor
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sensortv.app.ui.model.ChartScaleCalculator
import com.sensortv.app.ui.model.SensorChartConfig
import com.sensortv.app.ui.model.SensorChartData
import com.sensortv.app.ui.theme.SensorAccelerometer
import com.sensortv.app.ui.theme.SensorGyroscope
import com.sensortv.app.ui.theme.SensorLight
import com.sensortv.app.ui.theme.SensorMagnetometer
import com.sensortv.app.ui.theme.SensorProximity

/**
 * Gráfico de potencia estimada de sensores vs tiempo real en segundos utilizando Canvas.
 *
 * @param chartDataList Lista de sensores para construir el gráfico. Cada SensorChartData tiene un tipo de sensor, nombre y lista de puntos.
 * @param modifier Modificadores para personalizar el diseño visual del gráfico.
 */
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
        Paint().apply {
            color = axisTextColor.toArgb()
            textSize = with(density) { 12.sp.toPx() }   // tamaño escalable, convertir a pixeles (px)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val yAxisPaint = remember(axisTextColor, density) {
        Paint(axisPaint).apply {
            textAlign = Paint.Align.LEFT
        }
    }

    // Texto de los puntos (valores de potencia)

    val pointPaint = remember(density) {
        Paint().apply {
            color = pointTextColor.toArgb()
            textSize = with(density) { 8.sp.toPx() }
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val config = remember { SensorChartConfig() }

    Text(
        text = "Potencia (mW) vs Tiempo (Segundos)",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        textAlign = TextAlign.Center
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
    ) {

        // Si no hay sensores o solo sensores sin datos, se muestra un mensaje
        if (
            chartDataList.isEmpty() ||
            chartDataList.all { it.points.isEmpty() }
            ) {
            drawContext.canvas.nativeCanvas.drawText(
                "No hay datos disponibles actualmente...",
                size.width / 2,
                size.height / 2,
                axisPaint
            )
            return@Canvas
        }

        // Dimensiones del gráfico (canvas)

        val paddingLeft = config.paddingLeft
        val paddingBottom = config.paddingBottom
        val paddingTop = config.paddingTop
        val paddingRight = config.paddingRight

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingBottom - paddingTop

        // Escala del gráfico
        val chartScale = ChartScaleCalculator.calculate(
            chartDataList = chartDataList,
            config = config,
            chartWidth = chartWidth,
            chartHeight = chartHeight
        )

        // Dibujar elementos del gráfico (Grid, Ejes, líneas de cada sensor, etiquetas)
        drawGridY(chartScale, config, axisPaint)
        drawGridX(chartScale, config, axisPaint)
        drawAxes(chartScale, axisColor)
        drawSensorLines(chartDataList, chartScale, pointPaint)
        drawLabels(chartScale, axisPaint, yAxisPaint)
    }

    Spacer(modifier = Modifier.height(30.dp))

    // Leyenda Dinámica con FlowRow para que los items salten de línea si son demasiados
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        chartDataList
            .sortedBy { it.sensorType }
            .forEach { sensorData ->
            LegendItem(
                name = sensorData.displayName,
                color = getSensorColor(sensorData.sensorType)
            )
        }
    }
}

/**
 * Componente de la leyenda que muestra el color y nombre de un sensor.
 *
 * @param name Nombre del sensor.
 * @param color Color para del sensor en la leyenda.
 */
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

/**
 * Asigna un color consistente a cada tipo de sensor
 * para mantener coherencia visual entre gráfico y leyenda.
 *
 * @param sensorType Tipo del sensor definido por el hardware.
 * @return Color correspondiente al tipo de sensor.
 */
fun getSensorColor(sensorType: Int): Color {
    return when (sensorType) {
        Sensor.TYPE_LIGHT -> SensorLight
        Sensor.TYPE_PROXIMITY -> SensorProximity
        Sensor.TYPE_ACCELEROMETER -> SensorAccelerometer
        Sensor.TYPE_MAGNETIC_FIELD -> SensorMagnetometer
        Sensor.TYPE_GYROSCOPE -> SensorGyroscope
        else -> Color.Gray
    }
}