package com.sensortv.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.navigation.AppRoutes

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
            // Simulación de gráfica
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Gráfica (simulada)")
            }

            // Datos simulados de sensores
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Acelerómetro: 12.3 mW")
                    Text("Giroscopio: 8.4 mW")
                }
            }

            // Promedio total
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Promedio total: 23.7 mW")
                }
            }

            AppButton(
                text = "Captura de Datos",
                onClick = { navController.navigate(AppRoutes.Capture.route) },
                isPrimary = true,
            )

            //Posible cambio a outlinedButton
            AppButton(
                text = "Volver",
                onClick = { navController.navigate(AppRoutes.Menu.route) },
                isPrimary = false
            )
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