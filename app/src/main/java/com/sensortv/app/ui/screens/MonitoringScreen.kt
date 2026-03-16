package com.sensortv.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sensortv.app.model.SensorMonitorInfo
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.navigation.AppRoutes

/**
 * Pantalla que muestra la lectura y monitoreo de los sensores en tiempo real mediante una gráfica lineal.
 *
 * @param navController Controlador de navegación para manejar la navegación entre pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringScreen(navController: NavHostController) {
    Scaffold(
        topBar = { StandardTopBar("Monitoreo de Sensores") }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Lista de sensores en monitoreo (simulada)
            val sensors = listOf(
                SensorMonitorInfo("Acelerómetro", "Bosch BMA400", 0.2f, 12.3f),
                SensorMonitorInfo("Giroscopio", "Invensense MPU6500", 0.3f, 8.4f),
                SensorMonitorInfo("Proximidad", "Liteon LTR553", 0.1f, 2.1f)
            )

            GeneralInfoCard(
                batteryPercent = 84,
                batteryVoltage = 4.1,
                sensorsAvailable = sensors.size,
                averagePower = 23.7f
            )

            PowerChartPlaceholder()
            Spacer(modifier = Modifier.height(16.dp))

            SensorListSection(sensors)

            AppButton(
                text = "Captura de Datos",
                onClick = { navController.navigate(AppRoutes.Capture.route) },
                isPrimary = true,
            )

            AppButton(
                text = "Volver",
                onClick = { navController.navigate(AppRoutes.Menu.route) },
                isPrimary = false
            )
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
                .height(150.dp),
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
    batteryVoltage: Double,
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
            Text("Promedio total: $averagePower mW",
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
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
    // Un NavHostController simulado para el preview
    val dummyNavController = rememberNavController()
    MonitoringScreen(navController = dummyNavController)
}