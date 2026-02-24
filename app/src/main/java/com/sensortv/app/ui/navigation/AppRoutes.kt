package com.sensortv.app.ui.navigation

/**
 * Contrato central de navegación de la aplicación.
 * Define las rutas principales de navegación
 *
 * Centraliza las rutas para facilitar el
 * mantenimiento y refactorización.
 *
 * Se utiliza sealed class en lugar de enum para permitir
 * futura extensión con rutas dinámicas si el proyecto crece.
 */

sealed class AppRoutes(val route: String) {
    object Menu : AppRoutes("menu")
    object Monitoring : AppRoutes("monitoring")
    object Capture : AppRoutes("capture")
    object History : AppRoutes("history")
}