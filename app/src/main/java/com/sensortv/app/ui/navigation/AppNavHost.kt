package com.sensortv.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sensortv.app.ui.screens.*

/**
 * Define el grafo principal de navegación de la aplicación.
 *
 * Centraliza la declaración de destinos y establece la
 * pantalla inicial del flujo de la app.
 */

@Composable
fun AppNavHost(navController: NavHostController) {

    // Contenedor principal de navegación que gestiona y muestra los destinos según el estado del NavController
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Menu.route
    ) {

        composable(AppRoutes.Menu.route) {
            MainMenuScreen(navController)
        }

        composable(AppRoutes.Monitoring.route) {
            MonitoringScreen(navController)
        }

        composable(AppRoutes.Capture.route) {
            CaptureScreen(navController)
        }

        composable(AppRoutes.History.route) {
            HistoryScreen(navController)
        }
    }
}