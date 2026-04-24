package com.sensortv.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.SamplingFrequencySelector
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.navigation.AppRoutes
import com.sensortv.app.ui.utils.UiEvent
import com.sensortv.app.ui.viewmodel.SensorViewModel

/**
 * Pantalla para configurar y controlar la captura de datos de sensores.
 * Permite definir la duración de la captura, la frecuencia de muestreo y acceder al historial de capturas.
 *
 * @param navController Controlador de navegación utilizado para cambiar de pantalla.
 * @param viewModel instancia de [SensorViewModel] asociado a la captura de datos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    viewModel: SensorViewModel,
    navController: NavHostController
) {

    var duration by remember { mutableStateOf("") }
    var samplingFrequency by remember { mutableIntStateOf(1) }

    val isValidDuration = duration.isNotEmpty() && duration.toIntOrNull() != null && duration.toInt() > 0
    val context = LocalContext.current

    val isCapturing by viewModel.isCapturing.collectAsStateWithLifecycle()
    val remainingTime by viewModel.remainingTime.collectAsStateWithLifecycle()

    /** Estado para controlar la visibilidad de la alerta de cancelación */
    var showCancelDialog by remember { mutableStateOf(false) }

    var currentToast by remember { mutableStateOf<Toast?>(null) }
    /**
     * Colector de eventos únicos provenientes del ViewModel.
     * [LaunchedEffect] con Unit lanza una corrutina asociada a este Composable que se
     * inicia cuando entra en composición y se cancela cuando sale.
     *
     */

    // Lógica del Toast - notificación
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {

                    // Lógica Anti-Spam: Cancela el anterior si aún es visible
                    currentToast?.cancel()
                    currentToast = Toast.makeText(context, event.message, Toast.LENGTH_SHORT)
                    currentToast?.show()
                }
            }
        }
    }

    // Lógica del dialogo
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = "¿Interrumpir captura?",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Si cancelas ahora, todos los datos recolectados en esta sesión se perderán permanentemente.",
                    textAlign = TextAlign.Justify,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelCapture()
                        showCancelDialog = false
                    }
                ) {
                    Text("Detener y descartar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Continuar midiendo")
                }
            }
        )
    }

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
                onToggleCapture = {
                    if (!isCapturing) {

                        if(!isValidDuration) {
                            viewModel.sendUiMessage("Duración no válida")
                            return@CaptureControls
                        }

                        // Inicia captura
                        viewModel.startCapture(duration.toInt(), samplingFrequency)

                    } else {
                        // activamos el diálogo de cancelación -> Detiene la captura
                        showCancelDialog = true
                    }
                },
                isValidDuration = isValidDuration,
                remainingTime = remainingTime,
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

            Text("La captura de datos se guardará automáticamente en dos archivo CSV (mediciones y Total) al finalizar la sesión.")
            Text("Ruta de almacenamiento: Directorio interno privado de la aplicación (Android/data/.../captures/)")
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
    remainingTime: Int,
    isValidDuration: Boolean,
    onToggleCapture: () -> Unit,
    onNavigateToHistory: () -> Unit,
) {

    val minutes = remainingTime / 60
    val seconds = remainingTime % 60

    val formattedTime = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isCapturing) "Tiempo restante: $formattedTime" else "Tiempo restante: 00:00",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        AppButton(
            text = if (isCapturing) "Cancelar Captura" else "Iniciar Captura",
            onClick = onToggleCapture,
            isPrimary = !isCapturing,
            enabled = isCapturing || isValidDuration
        )

        AppButton(
            text = "Consultar Historial",
            onClick = onNavigateToHistory,
            isPrimary = true,
        )
    }
}
