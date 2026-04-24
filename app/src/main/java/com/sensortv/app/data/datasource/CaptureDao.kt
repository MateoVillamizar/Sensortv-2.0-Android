package com.sensortv.app.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sensortv.app.data.model.CaptureRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones de base de datos para los registros de captura.
 */
@Dao
interface CaptureDao {
    /**
     * Inserta un nuevo registro de captura.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) // si se inserta un registro con id existente, lo reemplaza.
    suspend fun insertRecord(record: CaptureRecordEntity)

    /**
     * Obtiene todos los registros ordenados por fecha descendente (el más reciente primero).
     *
     * @return Flujo reactivo que emite una nueva lista cada vez que cambian los datos en la tabla.
     */
    @Query("SELECT * FROM capture_records ORDER BY dateMillis DESC")
    fun getAllRecords(): Flow<List<CaptureRecordEntity>>

    /**
     * Elimina un registro específico.
     */
    @Delete
    suspend fun deleteRecord(record: CaptureRecordEntity)
}
