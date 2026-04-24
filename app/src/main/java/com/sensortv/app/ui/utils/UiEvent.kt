package com.sensortv.app.ui.utils

/**
 * Sealed class que representa los eventos únicos de interfaz de usuario.
 *
 * Se utiliza para comunicar acciones desde el ViewModel que no representan
 * un estado persistente, sino una acción efímera no persistente en el estado de la UI.
 */
sealed class UiEvent {
    /**
     * Evento para mostrar una notificación breve en pantalla.
     * @param message El texto que se mostrará al usuario.
     */
    data class ShowToast(val message: String) : UiEvent()
}