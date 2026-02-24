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
import com.sensortv.app.ui.navigation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringScreen(navController: NavHostController) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Monitoreo de Sensores") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    titleContentColor = Color(0xFF1F3A8A)
                )
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

            // Simulación de gráfica
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Gráfica (simulada)")
            }

            Button(
                onClick = {
                    navController.navigate(AppRoutes.Capture.route)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F3A8A), // Color de fondo del botón
                    contentColor = Color.White           // Color del texto del botón
                )
            ) {
                Text("Captura de Datos")
            }

            OutlinedButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F3A8A)
                )
            ) {
                Text("Volver")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MonitoringScreenPreview() {
    // Se crea un NavHostController simulado
    val dummyNavController = rememberNavController()
    MonitoringScreen(navController = dummyNavController)
}