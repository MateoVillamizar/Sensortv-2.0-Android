package com.sensortv.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sensortv.app.data.model.CaptureRecordEntity
import com.sensortv.app.domain.DeleteCaptureUseCase
import com.sensortv.app.domain.ExportAllCapturesUseCase
import com.sensortv.app.domain.GetCaptureHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel encargado de la gestión del historial de capturas.
 * Proporciona el estado de los registros y coordina las acciones de borrado y consulta.
 *
 * @param getCaptureHistoryUseCase Caso de uso para obtener el historial de capturas.
 * @param deleteCaptureUseCase Caso de uso para eliminar una captura.
 * @param exportAllCapturesUseCase Caso de uso para exportar todas las capturas al mismo tiempo.
 */
class HistoryViewModel(
    private val getCaptureHistoryUseCase: GetCaptureHistoryUseCase,
    private val deleteCaptureUseCase: DeleteCaptureUseCase,
    private val exportAllCapturesUseCase: ExportAllCapturesUseCase
): ViewModel() {

    /**
     * Flujo de capturas / registros obtenidos de la base de datos.
     * Se expone como StateFlow para que la UI de Compose lo observe.
     */
    val historyRecords: StateFlow<List<CaptureRecordEntity>> = getCaptureHistoryUseCase()
        // Convierte Flow en un StateFlow (puede ser observado por UI y mantiene último estado)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Solicita la eliminación de un registro y su archivo asociado.
     * @param record La entidad a eliminar.
     */
    fun deleteRecord(record: CaptureRecordEntity) {
        viewModelScope.launch {
            deleteCaptureUseCase(record)
        }
    }

    /**
     * Inicia el proceso de exportación masiva de los registros actuales.
     * - Se ejecuta en una corrutina ligada al ciclo de vida del ViewModel.
     * - Captura cualquier error durante el proceso de compresión.
     * - Notifica el resultado a la UI mediante el callback [onResult] al finalizar el proceso.
     *
     * @param onResult Función que se invoca al terminar, recibiendo el archivo ZIP o null si ocurrió un error.
     */
    fun exportAll(
        onResult: (File?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val zip = exportAllCapturesUseCase(historyRecords.value)
                onResult(zip)
            } catch (e: Exception) {
                Log.e("DEBUG_ZIP", "Error al exportar: ${e.message}", e)
                onResult(null)
            }
        }
    }
}