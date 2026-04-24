package com.sensortv.app.ui.model

/**
 * Objeto encargado de calcular la escala del gráfico (ChartScale)
 * a partir de los datos de sensores y la configuración definida.
 *
 * Separa la lógica de cálculo de escala del renderizado en Canvas.
 */
object ChartScaleCalculator {

    /**
     * Calcula la escala del gráfico a partir de los datos de sensores y la configuración.
     *
     * - Combina todos los puntos de todos los sensores en una sola secuencia
     * para calcular los rangos de tiempo y potencia (máximos y mínimos de tiempo y potencia).
     * - Calcula la escala del eje X (tiempo)
     * - Calcula la escala del eje Y (potencia)
     * - Aplica un margen superior (10%) al valor máximo del eje Y para evitar que los datos queden pegados al borde.
     * - Devuelve un objeto ChartScale con las escalas calculadas.
     */
    fun calculate(
        chartDataList: List<SensorChartData>,
        config: SensorChartConfig,
        chartWidth: Float,
        chartHeight: Float
    ): ChartScale {

        val allPoints = chartDataList
            .asSequence()
            .flatMap { it.points }

        // Escala X (tiempo)
        val maxX = allPoints.maxOfOrNull { it.timeStamp } ?: 0f     // Tiempo más grande / reciente
        val window = config.windowSize.coerceAtLeast(1f)
        val minX = (maxX - window).coerceAtLeast(0f)    // Tiempo más pequeño / antiguo

        // Escala Y (potencia)
        val rawMaxY = allPoints
            .maxOfOrNull { it.powerMw }
            ?.coerceAtLeast(1f) ?: 1f

        val maxY = (rawMaxY * 1.1f).coerceAtLeast(1f)

        return ChartScale(
            minX = minX,
            maxX = maxX,
            maxY = maxY,
            window = window,
            paddingLeft = config.paddingLeft,
            paddingTop = config.paddingTop,
            chartWidth = chartWidth,
            chartHeight = chartHeight
        )
    }
}