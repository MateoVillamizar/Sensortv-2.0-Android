package com.sensortv.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.sensortv.app.ui.navigation.AppNavHost
import com.sensortv.app.ui.theme.SensorTV20Theme

/**
 * Punto de entrada principal de la aplicación (Entry Point).
 *
 * Responsabilidades:
 * - Configurar el entorno visual mediante [enableEdgeToEdge] y el tema [SensorTV20Theme].
 * - Gestionar la solicitud de permisos críticos para el Foreground Service.
 * - Orquestar la navegación principal de la aplicación mediante un único NavHost: [AppNavHost].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verificación de permisos necesaria antes de que el usuario intente iniciar una captura
        checkAndRequestPermissions()

        setContent {
            SensorTV20Theme {
                val navController = rememberNavController()
                    AppNavHost(navController = navController)
            }
        }
    }

    /**
     * Verifica y solicita al usuario los permisos necesarios para el correcto funcionamiento
     * del sistema de notificaciones del servicio en dispositivos con Android 13 (API 33) o superior.
     *
     * - Nota: Este permiso no es requerido en versiones anteriores del sistema operativo.
     * - Nota: Sin este permiso, el servicio de captura se ejecutará, pero la notificación
     * obligatoria no será visible, incumpliendo los requisitos del sistema para servicios en
     * primer plano en versiones recientes de Android.
     */
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = Manifest.permission.POST_NOTIFICATIONS

            // Si el permiso no ha sido concedido previamente
            if (checkSelfPermission(notificationPermission) != PackageManager.PERMISSION_GRANTED) {
                // El código 101 identifica esta solicitud específica de permisos
                requestPermissions(arrayOf(notificationPermission), 101)
            }
        }
    }
}
