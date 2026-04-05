package com.sensortv.app.ui.model

/**
 * Modelo de datos UI que representa la escala y dimensiones del gráfico necesarias
 * para convertir valores de datos (tiempo, potencia) a coordenadas en el Canvas.
 *
 * @property minX Tiempo mínimo visible en la ventana (inicio del eje X).
 * @property maxX Tiempo máximo visible en la ventana (fin del eje X).
 * @property maxY Valor máximo de potencia (eje Y).
 * @property window Tamaño de la ventana de tiempo visible para el gráfico.
 * @property paddingLeft Margen izquierdo del gráfico.
 * @property paddingTop Margen superior del gráfico.
 * @property chartWidth Ancho utilizable del gráfico Canvas (sin padding).
 * @property chartHeight Alto utilizable del gráfico Canvas (sin padding).
*/
data class ChartScale(
    val minX: Float,
    val maxX: Float,
    val maxY: Float,
    val window: Float,
    val paddingLeft: Float,
    val paddingTop: Float,
    val chartWidth: Float,
    val chartHeight: Float
)