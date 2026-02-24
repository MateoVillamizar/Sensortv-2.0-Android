package com.sensortv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sensortv.app.model.Record

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController) {

    // Datos simulados (luego vendrán del repository)
    val records = listOf(
        Record("Registro_2026-02-18_14-30.csv", "18/02/2026"),
        Record("Registro_2026-02-20_09-00.csv", "20/02/2026"),
        Record("Registro_2026-02-20_16-45.csv", "20/02/2026"),
        Record("Registro_2026-02-21_11-10.csv", "21/02/2026")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial de Registros") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    titleContentColor = Color(0xFF1F3A8A)
                )
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            items(records) { record ->

                Card {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(text = record.fileName)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Fecha: ${record.date}")

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { /* futura exportación */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1F3A8A),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Exportar")
                            }

                            OutlinedButton(
                                onClick = { /* futura vista detalle */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF1F3A8A)
                                )
                            ) {
                                Text("Ver")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistoryScreenPreview() {
    // Se crea un NavHostController simulado
    val dummyNavController = rememberNavController()
    HistoryScreen(navController = dummyNavController)
}