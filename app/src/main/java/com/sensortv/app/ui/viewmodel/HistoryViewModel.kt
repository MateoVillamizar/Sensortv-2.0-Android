package com.sensortv.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sensortv.app.data.model.CaptureRecordEntity
import com.sensortv.app.domain.DeleteCaptureUseCase
import com.sensortv.app.domain.GetCaptureHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la gestión del historial de capturas.
 * Proporciona el estado de los registros y coordina las acciones de borrado y consulta.
 *
 * @param getCaptureHistoryUseCase Caso de uso para obtener el historial de capturas.
 * @param deleteCaptureUseCase Caso de uso para eliminar una captura
 */
class HistoryViewModel(
    private val getCaptureHistoryUseCase: GetCaptureHistoryUseCase,
    private val deleteCaptureUseCase: DeleteCaptureUseCase,
): ViewModel() {

    /**
     * Flujo de capturas obtenidas de la base de datos.
     * Se expone como StateFlow para que la UI de Compose lo observe.
     */
    val historyRecords: StateFlow<List<CaptureRecordEntity>> = getCaptureHistoryUseCase()
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
}