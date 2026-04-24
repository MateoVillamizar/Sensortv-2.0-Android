package com.sensortv.app.data.service

import com.sensortv.app.data.model.CaptureState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor centralizado para la comunicación entre el Foreground Service y la Capa UI.
 *
 * - Actúa como un puente de datos. Implementa el patrón Singleton para centralizar el flujo de estado
 * (Single Source of Truth), permitiendo que la UI se sincronice con el proceso de fondo sin acoplamiento directo
 * al ciclo de vida del Servicio.
 */
object CaptureServiceManager {

    /** Flujo interno de estado mutable que solo el Manager puede modificar. */
    private val _captureState = MutableStateFlow(CaptureState())

    /** Flujo de estado público de solo lectura expuesto como StateFlow inmutable.
     * La UI se debe suscribir a este flujo para reaccionar a la captura.
     */
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    /**
     * Actualiza el estado global de la captura.
     * Invocado exclusivamente por [SensorCaptureService] durante su ejecución.
     */
    fun updateState(newState: CaptureState) {
        _captureState.value = newState
    }

    /**
     * Reinicia el estado a los valores por defecto al finalizar o cancelar una captura.
     */
    fun resetState() {
        _captureState.value = CaptureState()
    }
}