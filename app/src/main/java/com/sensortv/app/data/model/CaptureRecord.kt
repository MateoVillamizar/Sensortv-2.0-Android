package com.sensortv.app.data.model

/**
 * Representa la información de una sesión de captura completada.
 * Se utiliza para mostrar las "Cards" de cada registro en la pantalla de historial.
 *
 * @property id Identificador único de la sesión.
 * @property fileName Nombre del archivo de captura (es el timestamp de la sesión).
 * @property durationMinutes Duración de la captura en minutos.
 * @property filePath Ruta a la ubicación del archivo de captura.
 */
data class captureRecord(
    val id: Int = 0,
    val fileName: String,
    val durationMinutes: Int,
    val filePath: String
)