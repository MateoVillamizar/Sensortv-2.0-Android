package com.sensortv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.sensortv.app.ui.navigation.AppNavHost
import com.sensortv.app.ui.theme.SensorTV20Theme

/**
 * Punto de entrada principal de la aplicación (Entry Point).
 * Esta Activity es la encargada de:
 * - Establecer el tema global de la aplicación [SensorTV20Theme].
 * - Inicializar el controlador de navegación principal [NavHostController].
 * - Servir como contenedor para la jerarquía de Compose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SensorTV20Theme {

                val navController = rememberNavController()

                AppNavHost(navController = navController)
            }
        }
    }
}
