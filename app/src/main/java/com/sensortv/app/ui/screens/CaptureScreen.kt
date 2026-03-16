package com.sensortv.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.SamplingFrequencySelector
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.navigation.AppRoutes

/**
 * Pantalla para configurar y controlar la captura de datos de sensores.
 * Permite definir la duración de la captura, la frecuencia de muestreo y acceder al historial de capturas.
 *
 * @param navController Controlador de navegación utilizado para cambiar de pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(navController: NavHostController) {

    // Estado simulado de captura
    var duration by remember { mutableStateOf("") }
    var isCapturing by remember { mutableStateOf(false) }
    var samplingFrequency by remember { mutableIntStateOf(3) }

    Scaffold(
        topBar = { StandardTopBar("Captura de Datos") }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CaptureConfigCard(
                duration = duration,
                samplingFrequency = samplingFrequency,
                onSamplingFrequencyChange = { samplingFrequency = it },
                onDurationChange = { it ->
                    // Solo actualiza si:
                    // 1. El nuevo texto (it) son solo números (o está vacío)
                    // 2. Y tiene 3 caracteres o menos
                    if (it.all { char -> char.isDigit() } && it.length <= 3) {
                        duration = it
                    }
                },
                sensorsAvailable = 5
            )

            CaptureStatusCard()
            CaptureControls(
                isCapturing = isCapturing,
                onToggleCapture = { isCapturing = !isCapturing },
                onNavigateToHistory = { navController.navigate(AppRoutes.History.route) }
            )

            Spacer(modifier = Modifier.weight(1f))

            AppButton(
                text = "Volver",
                onClick = { navController.popBackStack() },
            )
        }
    }
}

/**
 * Componente Card que permite configurar los parámetros de captura.
 *
 * @param duration Duración de la captura en minutos.
 * @param samplingFrequency Frecuencia de muestreo en segundos.
 * @param onDurationChange Callback invocado al modificar la duración.
 * @param onSamplingFrequencyChange Callback invocado al cambiar la frecuencia de muestreo.
 * @param sensorsAvailable Cantidad de sensores disponibles para la captura de datos.
 */
@Composable
fun CaptureConfigCard(
    duration: String,
    samplingFrequency: Int,
    onDurationChange: (String) -> Unit,
    onSamplingFrequencyChange: (Int) -> Unit,
    sensorsAvailable: Int
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = duration,
                onValueChange = onDurationChange,
                label = { Text("Duración (minutos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            Text(
                text = "Frecuencia de muestreo: $samplingFrequency segundos",
                style = MaterialTheme.typography.bodyLarge
            )

            SamplingFrequencySelector(
                selected = samplingFrequency,
                onSelected = onSamplingFrequencyChange
            )

            Text(
                text = "Sensores disponibles: $sensorsAvailable",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Componente Card informativa que describe el Estado actual de la medición y explica cómo
 * se almacenarán los datos capturados.
 */
@Composable
fun CaptureStatusCard() {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = "Estado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text("La captura de datos se guardará automáticamente como archivo CSV al finalizar la sesión.")
            Text("Ruta del archivo: /data/data/com.sensortv.app/files/captures/")
        }
    }
}

/**
 * Componente Card que muestra los controles para iniciar o detener la captura de datos.
 *
 * @param isCapturing Indica si la captura está activa.
 * @param onToggleCapture Callback que alterna el estado de captura.
 * @param onNavigateToHistory Callback que navega al historial de capturas.
 */
@Composable
fun CaptureControls(
    isCapturing: Boolean,
    onToggleCapture: () -> Unit,
    onNavigateToHistory: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isCapturing)
                "Tiempo restante: 04:59"
            else
                "Tiempo restante: 00:00",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        AppButton(
            text = if (isCapturing) "Detener Captura" else "Iniciar Captura",
            onClick = onToggleCapture,
            isPrimary = !isCapturing,
        )

        AppButton(
            text = "Consultar Historial",
            onClick = onNavigateToHistory,
            isPrimary = true,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CaptureScreenPreview() {
    val dummyNavController = rememberNavController()
    CaptureScreen(navController = dummyNavController)
}