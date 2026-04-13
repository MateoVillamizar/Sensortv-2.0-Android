package com.sensortv.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la tabla 'capture_records' en la base de datos local.
 * Almacena los metadatos necesarios para listar las capturas en el historial de registros.
 */
@Entity(tableName = "capture_records")
data class CaptureRecordEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,
    val durationMinutes: Int,
    val samplingFrequencySeconds: Int,
    val dateMillis: Long,       // Fecha en milisegundos para ordenar fácilmente
    val filePath: String,        // Ruta absoluta del archivo .csv
)