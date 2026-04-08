package com.sensortv.app.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sensortv.app.data.model.CaptureRecordEntity

/**
 * Configuración principal de la base de datos Room.
 */
@Database(entities = [CaptureRecordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun captureDao(): CaptureDao
}