package com.sensortv.app.data.model

/**
 * Representa el estado actual de la sesión de captura activa dentro del servicio.
 * Centraliza la información necesaria para que la interfaz reaccione a los cambios del servicio.
 *
 * @property isCapturing Indica si hay un proceso de recolección de datos activo actualmente.
 * @property remainingSeconds Tiempo faltante para completar la sesión programada.
 * @property samplingFrequency Intervalo seleccionado (en segundos) para el registro de datos.
 * Tiene un valor por defecto de 3 segundos.
 * @property currentPowerData Última lectura de potencia procesada (para actualizar la UI).
 */
data class CaptureState(
    val isCapturing: Boolean = false,
    val remainingSeconds: Int = 0,
    val samplingFrequency: Int = 3,
    val currentPowerData: SensorData? = null
)