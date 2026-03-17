package com.sensortv.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sensortv.app.model.SensorMonitorInfo
import com.sensortv.app.presentation.viewmodel.SensorViewModel
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.components.getSensorIcon
import com.sensortv.app.ui.navigation.AppRoutes

/**
 * Pantalla que muestra la lectura y monitoreo de los sensores en tiempo real mediante una gráfica lineal.
 *
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

    Scaffold(
        topBar = { StandardTopBar("Monitoreo de Sensores") }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            // Lista de sensores en monitoreo (simulada)
            val sensorsMonitoringList = realSensors.map { sensor ->
                SensorMonitorInfo(
                    SensorName = sensor.displayName,
                    hardware = sensor.hardwareName,
                    baseCurrentMa = sensor.nominalConsumptionmA,
                    currentPowerMw = sensor.estimatedPowerMw
                )
            }

            item {
                GeneralInfoCard(
                    batteryPercent = batteryInfo?.percentage ?: 0,
                    batteryVoltage = batteryInfo?.voltage ?: 0f,
                    sensorsAvailable = sensorsMonitoringList.size,
                    averagePower = 23.7f
                )
            }

            item { PowerChartPlaceholder() }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text(
                    text = "Sensores en monitoreo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(sensorsMonitoringList) { sensor ->
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
                    onClick = { navController.navigate(AppRoutes.Menu.route) },
                    isPrimary = false
                )
            }
        }
    }
}

//Placeholder temporal de la gráfica
@Composable
private fun PowerChartPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(750.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Gráfica lineal de potencia por sensor (pendiente de implementar)",
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Componente Card que muestra la información general del monitoreo de sensores.
 *
 * @param batteryPercent Porcentaje de batería actual.
 * @param batteryVoltage Voltaje actual de la batería.
 * @param sensorsAvailable Cantidad de sensores disponibles.
 * @param averagePower Promedio de potencia en tiempo real de todos los sensores.
 */
@Composable
private fun GeneralInfoCard(
    batteryPercent: Int,
    batteryVoltage: Float,
    sensorsAvailable: Int,
    averagePower: Float
) {
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
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Batería: $batteryPercent %",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text("Voltaje: $batteryVoltage V",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text("Sensores disponibles: $sensorsAvailable",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text("Promedio total (PENDIENTE): $averagePower mW",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Componente Card que muestra información detallada de un sensor específico en el monitoreo.
 *
 * @param sensor Información del sensor a mostrar.
 */
@Composable
private fun SensorExpandableInfoCard(sensor: SensorMonitorInfo) {

    // Estado que controla si la tarjeta está expandida o colapsada
    var expanded by remember { mutableStateOf(false) }

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
                    imageVector = getSensorIcon(sensor.SensorName),
                    contentDescription = sensor.SensorName,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = sensor.SensorName,
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
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hardware: ${sensor.hardware}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Consumo base: ${sensor.baseCurrentMa} mA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Potencia actual: ${sensor.currentPowerMw} mW",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Componente Card que muestra la lista de sensores en monitoreo con información adicional.
 * Cada sensor tiene su propio card con detalles específicos de cada uno.
 *
 * @param sensors Lista de la información de sensores a mostrar.
 */
@Composable
fun SensorListSection(sensors: List<SensorMonitorInfo>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Sensores en monitoreo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        sensors.forEach { sensor ->
            SensorExpandableInfoCard(sensor)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MonitoringScreenPreview() {
    val dummyNavController = rememberNavController()
    MonitoringScreen(
        navController = dummyNavController,
        viewModel = TODO()
    )
}