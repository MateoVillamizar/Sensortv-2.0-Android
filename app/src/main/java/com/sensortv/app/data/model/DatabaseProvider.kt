package com.sensortv.app.data.model

import android.content.Context
import androidx.room.Room
import com.sensortv.app.data.datasource.AppDatabase

/**
 * Objeto responsable de proveer una única instancia de la base de datos Room.
 *
 * Evita múltiples instancias de la base de datos,
 * optimizando el uso de recursos y garantizando consistencia en el acceso a los datos.
 */
object DatabaseProvider {

    // Referencia interna a la instancia de la base de datos.
    private var INSTANCE: AppDatabase? = null

    /**
     * Proporciona una instancia única de [AppDatabase].
     *
     * Verifica si ya existe una instancia creada:
     * 1. Si existe, la retorna inmediatamente.
     * 2. Si no existe entra en un bloque sincronizado (thread-safe)
     * crea la base de datos usando Room, guardando la instancia para futuras llamadas.
     *
     * @param context Contexto de aplicación necesario para inicializar Room.
     * @return Instancia única de [AppDatabase].
     */
    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sensor_tv_2.0_db"
            ).build()

            INSTANCE = instance
            instance
        }
    }
}