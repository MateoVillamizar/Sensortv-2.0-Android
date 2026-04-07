package com.sensortv.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sensortv.app.presentation.viewmodel.SensorViewModel
import com.sensortv.app.presentation.viewmodel.SensorViewModelFactory
import com.sensortv.app.ui.screens.CaptureScreen
import com.sensortv.app.ui.screens.HistoryScreen
import com.sensortv.app.ui.screens.MainMenuScreen
import com.sensortv.app.ui.screens.MonitoringScreen

/**
 * Define el grafo de navegación principal de SensorTV 2.0.
 *
 * - Gestiona el intercambio de pantallas y la persistencia del estado de navegación.
 * - Centraliza la declaración de destinos y establece la pantalla inicial del flujo de la app.
 *
 * @param navController El controlador de navegación que gestiona la pila de pantallas.
 */
@Composable
fun AppNavHost(navController: NavHostController) {

    // Obtenemos el contexto de Android para la fábrica
    val context = LocalContext.current
    val viewModel: SensorViewModel = viewModel(
        factory = SensorViewModelFactory(context)
    )

    // Contenedor principal de navegación que gestiona y muestra los destinos según el estado del NavController
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Menu.route
    ) {
        composable(AppRoutes.Menu.route) {
            MainMenuScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(AppRoutes.Monitoring.route) {
            MonitoringScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(AppRoutes.Capture.route) {
            CaptureScreen(
                viewModel = viewModel,
                navController = navController)
        }

        composable(AppRoutes.History.route) {
            HistoryScreen(navController = navController)
        }
    }
}