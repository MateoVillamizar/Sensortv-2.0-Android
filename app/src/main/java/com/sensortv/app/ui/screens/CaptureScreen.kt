package com.sensortv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(navController: NavHostController) {

    // Estado simulado de captura
    var duration by remember { mutableStateOf("") }
    var isCapturing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Captura de Datos") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duración (minutos)") },
                modifier = Modifier.fillMaxWidth()
            )

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("La captura se guardará automáticamente")
                    Text("como archivo CSV al finalizar la sesión.")
                }
            }

            Text("Frecuencia: 3 segundos")
            Text("Sensores disponibles: 5")

            Button(
                onClick = {
                    isCapturing = !isCapturing
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F3A8A),
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (isCapturing) "Detener Captura"
                    else "Iniciar Captura"
                )
            }

            Text(
                if (isCapturing) "Tiempo restante: 04:59"
                else "Tiempo restante: 00:00"
            )

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
fun CaptureScreenPreview() {
    // Se crea un NavHostController simulado
    val dummyNavController = rememberNavController()
    CaptureScreen(navController = dummyNavController)
}