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
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sensortv.app.data.repository.SensorInfo
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.navigation.AppRoutes

/**
 * Pantalla principal que muestra el estado de la batería y los sensores detectados en el dispositivo.
 *
 * @param navController Controlador de navegación para manejar la navegación entre pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavHostController) {
    Scaffold(
        topBar = { StandardTopBar("SensorTV 2.0") }
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

            // Información de la batería en tiempo real (simulada)
            BatteryInfoCard(
                batteryPercentage = 96,
                batteryVoltage = 4.314
            )

            // Lista de sensores del sistema (simulada)
            val sensors = listOf(
                SensorInfo("Acelerómetro", true, 2.3f),
                SensorInfo("Giroscopio", true, 1.5f),
                SensorInfo("Proximidad", true, 0.5f),
                SensorInfo("Luminosidad", true, 3.0f),
                SensorInfo("Magnetómetro", false, 0.0f)
            )

            SensorTableCard(sensors)

            AppButton(
                text = "Monitorear Sensores",
                onClick = { navController.navigate(AppRoutes.Monitoring.route) },
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
    batteryVoltage: Double
) {
    Card {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            Text(
                text = "Información de la Batería",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Text("Porcentaje de Batería: $batteryPercentage%")
            Text("Voltaje (tiempo real): $batteryVoltage V")
        }
    }
}

/**
 * Componente Card que muestra una tabla con información de los sensores detectados.
 *
 * @param sensors Lista del tipo SensorInfo que contiene la información de sensores.
 */
@Composable
private fun SensorTableCard(sensors: List<SensorInfo>) {
    Card (
        modifier = Modifier.padding(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SensorTableHeader()

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            sensors.forEach { sensor ->
                SensorRow(sensor)
            }
        }
    }
}

/**
 * Componente Row que define la cabecera (header) de la tabla de sensores.
 *
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
        TableCell("Tipo de Sensor", weight = 1.4f, isHeader = true)

        VerticalDivider(modifier = Modifier
            .fillMaxHeight()
            .width(1.dp), color = DividerDefaults.color)

        TableCell("Estado", weight = 1.2f, isHeader = true)

        VerticalDivider(modifier = Modifier
            .fillMaxHeight()
            .width(1.dp), color = DividerDefaults.color)

        TableCell("Consumo (mA)", isHeader = true)
    }
}

/**
 * Componente Row que define una fila de la tabla de sensores.
 *
 * @param sensor Información del sensor.
 */
@Composable
private fun SensorRow(sensor: SensorInfo) {

    val AvailabilityText =
        if (sensor.isAvailable) "Disponible" else "No disponible"

    val AvailibilityColor =
        if (sensor.isAvailable) Color(0xFF1F3A8A) else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        TableCell(sensor.SensorType, weight = 1.4f)

        VerticalDivider(modifier = Modifier
            .fillMaxHeight()
            .width(1.dp), color = DividerDefaults.color)

        TableCell(AvailabilityText, weight = 1.2f, textColor = AvailibilityColor)

        VerticalDivider(modifier = Modifier
            .fillMaxHeight()
            .width(1.dp), color = DividerDefaults.color)

        TableCell("${sensor.Sensorpower} mA", textColor = AvailibilityColor)
    }
}

/**
 * Componente Box que define una celda de la tabla de sensores.
 *
 * @param text Texto a mostrar en la celda.
 * @param weight Peso de la celda en la distribución horizontal.
 * @param isHeader Indica si la celda es la cabecera (header).
 * @param textColor Color del texto (opcional).
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
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = textColor ?: if (isHeader) Color.Black else Color(0xFF1F3A8A)
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenPreview() {
    //  Un NavHostController simulado para el preview
    val dummyNavController = rememberNavController()
    MainMenuScreen(navController = dummyNavController)
}