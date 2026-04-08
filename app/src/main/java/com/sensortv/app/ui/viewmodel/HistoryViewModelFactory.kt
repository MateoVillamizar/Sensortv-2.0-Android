package com.sensortv.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.sensortv.app.data.datasource.AndroidCsvDataSource
import com.sensortv.app.data.datasource.AppDatabase
import com.sensortv.app.data.model.DatabaseProvider
import com.sensortv.app.data.repository.CaptureRepositoryImpl
import com.sensortv.app.domain.DeleteCaptureUseCase
import com.sensortv.app.domain.GetCaptureHistoryUseCase

/**
 * Fábrica personalizada para la creación del [HistoryViewModel].
 * 1. Obtiene la instancia única de la base de datos mediante [DatabaseProvider].
 * 2. Recupera el DAO necesario para la gestión de capturas.
 * 3. Construye el repositorio de datos de capturas utilizando el DAO.
 * 4. Inicializa los casos de uso de dominio asociados al historial.
 *
 * @param context Contexto necesario para que los DataSources accedan a los servicios de hardware.
 * @return [HistoryViewModelFactory] Instancia completa de [HistoryViewModel] lista para su uso en UI.
 */
class HistoryViewModelFactory(
    private val context: Context
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Se verifica que se esté solicitando el ViewModel (HistoryViewModel) correcto
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {

            // Construcción de dependencias (B.D, dataSources, Repository, UseCases)
            val db = DatabaseProvider.getDatabase(context)
            val captureDao = db.captureDao()

            val captureRepo = CaptureRepositoryImpl(captureDao)
            val csvDataSource = AndroidCsvDataSource(context)

            val getCaptureHistoryUseCase = GetCaptureHistoryUseCase(captureRepo)
            val deleteCaptureUseCase = DeleteCaptureUseCase(csvDataSource, captureRepo)

            return HistoryViewModel(getCaptureHistoryUseCase, deleteCaptureUseCase) as T
        }
        throw IllegalArgumentException("Error: Clase ViewModel no compatible con esta Factory")
    }
}