package com.sensortv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sensortv.app.ui.navigation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavHostController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SensorTV 2.0") }
            )
        }
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

            // Información del dispositivo (simulada)
            Card {
                Column(modifier = Modifier.padding(16.dp),) {
                    Text("Modelo: Pixel X")
                    Text("Android: 14")
                    Text("CPU: Snapdragon")
                    Text("Batería: 44%")
                }
            }

            // Total consumo simulado
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Consumo")
                    Text("7.3 mW")
                }
            }

            Button(
                onClick = {
                    navController.navigate(AppRoutes.Monitoring.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Monitorear Sensores")
            }

            Button(
                onClick = {
                    navController.navigate(AppRoutes.History.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Consultar Historial")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenPreview() {
    // Se crea un NavHostController simulado
    val dummyNavController = rememberNavController()
    MenuScreen(navController = dummyNavController)
}