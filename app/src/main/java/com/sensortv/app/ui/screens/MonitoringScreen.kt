package com.sensortv.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.sensortv.app.data.model.SensorData
import com.sensortv.app.ui.viewmodel.SensorViewModel
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.Chart.SensorPowerChart
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.components.getSensorIcon
import com.sensortv.app.ui.navigation.AppRoutes

/**
 * Pantalla que muestra la lectura y monitoreo de los sensores en tiempo real mediante una gráfica lineal.
 *
 * @param viewModel Instancia de [SensorViewModel] asociado al monitoreo de sensores.
 * @param navController Controlador de navegación para manejar la navegación entre pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringScreen(
    viewModel: SensorViewModel,
    navController: NavHostController
) {

    val realSensors by viewModel.sensorList.collectAsStateWithLifecycle()
    val batteryInfo by viewModel.batteryState.collectAsStateWithLifecycle()

    val chartData by viewModel.sensorChartData.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { StandardTopBar("Monitoreo de Sensores") }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            item {
                GeneralInfoCard(
                    batteryPercent = batteryInfo?.percentage ?: 0,
                    batteryVoltage = batteryInfo?.voltage ?: 0f,
                    sensorsAvailable = realSensors.size,
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { SensorPowerChart(
                chartDataList = chartData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(680.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text(
                    text = "Sensores en monitoreo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(realSensors) { sensor ->
                SensorExpandableInfoCard(sensor)
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppButton(
                    text = "Captura de Datos",
                    onClick = { navController.navigate(AppRoutes.Capture.route) },
                    isPrimary = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppButton(
                    text = "Volver",
                    onClick = { navController.popBackStack() },
                    isPrimary = false
                )
            }
        }
    }
}

/**
 * Componente Card que muestra la información general del monitoreo de sensores.
 *
 * @param batteryPercent Porcentaje de batería actual.
 * @param batteryVoltage Voltaje actual de la batería.
 * @param sensorsAvailable Cantidad de sensores disponibles.
 */
@Composable
private fun GeneralInfoCard(
    batteryPercent: Int,
    batteryVoltage: Float,
    sensorsAvailable: Int,
) {

    val formattedVoltage = "%.3f V".format(batteryVoltage)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Información General",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface

            )

            Text(
                text = "Batería: $batteryPercent %",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text("Voltaje: $formattedVoltage",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text("Sensores disponibles: $sensorsAvailable",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Componente Card que muestra información detallada de un sensor específico en el monitoreo.
 *
 * @param sensor Información del sensor a mostrar en detalle.
 */
@Composable
private fun SensorExpandableInfoCard(sensor: SensorData) {

    // Estado que controla si la tarjeta está expandida o colapsada
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = getSensorIcon(sensor.displayName),
                    contentDescription = sensor.displayName,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = sensor.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expandir",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(
                        // Rota la flecha 180° si la tarjeta está expandida, 0° si está colapsada
                        if (expanded) 180f else 0f
                    )
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Hardware: ${sensor.hardwareName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Consumo base: ${sensor.nominalConsumptionmA} mA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Potencia actual: ${"%.4f".format(sensor.estimatedPowerMw)} mW",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}