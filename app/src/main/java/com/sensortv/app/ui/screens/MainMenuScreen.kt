package com.sensortv.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.sensortv.app.data.model.SensorData
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.navigation.AppRoutes
import com.sensortv.app.ui.viewmodel.SensorViewModel

/**
 * Pantalla del menú principal de SensorTV 2.0.
 * Gestiona la presentación del estado de la batería y la lista reactiva de sensores detectados.
 *
 * @param navController Gestor de navegación para redirigir a monitoreo o historial
 * @param viewModel Instancia de [SensorViewModel] que provee el flujo de datos de sensores.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    viewModel: SensorViewModel,
    navController: NavHostController
) {
    val realSensors by viewModel.sensorList.collectAsStateWithLifecycle()
    val batteryInfo by viewModel.batteryState.collectAsStateWithLifecycle()

    var firstTimeMonitoring by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = { StandardTopBar("SensorTV 2.0") }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // Información de la batería en tiempo real (simulada)
            BatteryInfoCard(
                batteryPercentage = batteryInfo?.percentage ?: 0,
                batteryVoltage = batteryInfo?.voltage ?: 0f
            )

            SensorTableCard(realSensors)

            AppButton(
                text = "Monitorear Sensores",
                onClick = {
                    if (firstTimeMonitoring) {
                        viewModel.restartMonitoring()
                        firstTimeMonitoring = false
                    }

                    navController.navigate(AppRoutes.Monitoring.route)
                },
                isPrimary = true
            )

            AppButton(
                text = "Consultar Historial",
                onClick = { navController.navigate(AppRoutes.History.route) },
                isPrimary = false
            )
        }
    }
}

/**
 * Componente Card que muestra la información de la batería en tiempo real.
 *
 * @param batteryPercentage Porcentaje de batería actual.
 * @param batteryVoltage Voltaje actual de la batería.
 */
@Composable
private fun BatteryInfoCard(
    batteryPercentage: Int,
    batteryVoltage: Float
) {

    val formattedVoltage = "%.3f V".format(batteryVoltage)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            Text(
                text = "Información de la Batería",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Porcentaje de Batería: $batteryPercentage%",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Voltaje (tiempo real): $formattedVoltage",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Componente Card que muestra una tabla con información de los sensores detectados.
 *
 * @param sensors Lista del tipo SensorData que contiene la información de sensores.
 */
@Composable
private fun SensorTableCard(sensors: List<SensorData>) {
    Card (
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Sensores Detectados",
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            SensorTableHeader()
            HorizontalDivider(Modifier, DividerDefaults.Thickness, MaterialTheme.colorScheme.outline)

            sensors.forEach { sensor ->
                SensorRow(sensor)
            }
        }
    }
}

/**
 * Componente Row que define la cabecera (header) de la tabla de sensores.
 */
@Composable
private fun SensorTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Uso de IntrinsicSize Min para que la fila se ajuste a la altura del texto
            // y permite que los VerticalDivider ocupen el 100% de esa altura (sean visibles).
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell("Nombre del Sensor", weight = 1.4f, isHeader = true)

        VerticalDivider(
            modifier = Modifier
            .fillMaxHeight()
            .width(1.dp), color = MaterialTheme.colorScheme.outline)

        TableCell("Estado", weight = 1.2f, isHeader = true)

        VerticalDivider(
            modifier = Modifier
            .fillMaxHeight()
            .width(1.dp), color = MaterialTheme.colorScheme.outline)

        TableCell("Consumo (mA)", isHeader = true)
    }
}

/**
 * Componente Row que define una fila de la tabla de sensores.
 *
 * @param sensor Información del según el modelo SensorData.
 */
@Composable
private fun SensorRow(sensor: SensorData) {

    val availabilityText =
        if (sensor.isAvailable) "Disponible" else "No disponible"

    val availabilityColor =
        if (sensor.isAvailable)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        TableCell(sensor.displayName, weight = 1.4f, textColor = availabilityColor)

        VerticalDivider(
            modifier = Modifier
            .fillMaxHeight()
            .width(1.dp), color = MaterialTheme.colorScheme.outline)

        TableCell(availabilityText, weight = 1.2f, textColor = availabilityColor)

        VerticalDivider(
            modifier = Modifier
            .fillMaxHeight()
            .width(1.dp), color = MaterialTheme.colorScheme.outline)

        TableCell("${sensor.nominalConsumptionmA} mA", textColor = availabilityColor)
    }
}

/**
 * Componente Box que define y renderiza una celda individual dentro de la tabla de sensores.
 *
 * @param text Contenido textual de la celda.
 * @param weight Proporción de ancho en la fila (basado en [RowScope.weight]).
 * @param isHeader Si es verdadero, aplica estilo resaltado de cabecera.
 * @param textColor Color específico para el texto; si es nulo, usa el esquema por defecto.
 */
@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float = 1f,
    isHeader: Boolean = false,
    textColor: Color? = null
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = text,
            textAlign = TextAlign.Center,

            style = if (isHeader)
                MaterialTheme.typography.titleSmall
            else
                MaterialTheme.typography.bodyLarge,

            color = textColor ?:
            if (isHeader)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}