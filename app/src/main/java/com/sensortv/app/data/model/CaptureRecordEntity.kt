package com.sensortv.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la tabla 'capture_records' en la base de datos local.
 * Almacena los metadatos necesarios para listar las capturas en el historial de registros.
 *
 * @property id Identificador único generado automáticamente por Room.
 * @property fileName Nombre del archivo CSV (con extensión).
 * @property durationMinutes Duración total de la captura en minutos.
 * @property samplingFrequencySeconds Frecuencia de muestreo utilizada (en segundos).
 * @property dateMillis Marca de tiempo en milisegundos desde epoch (Unix time).
 * Se usa para ordenar los registros cronológicamente (más recientes primero).
 * @property filePath Ruta absoluta en el almacenamiento del dispositivo donde se guarda el archivo CSV.
 */
@Entity(tableName = "capture_records")
data class CaptureRecordEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,
    val durationMinutes: Int,
    val samplingFrequencySeconds: Int,
    val dateMillis: Long,
    val filePath: String,
)