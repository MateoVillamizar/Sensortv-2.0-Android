package com.sensortv.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.model.Record
import com.sensortv.app.ui.viewmodel.HistoryViewModel

/**
 * Pantalla que muestra el historial de registros de datos capturados.
 * Permite exportar y ver los registros.
 *
 * @param navController Controlador de navegación utilizado para cambiar de pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    navController: NavHostController
) {

    // Datos simulados (luego vendrán del repository - Capa de data)
    val records = listOf(
        Record(1, "Registro_2026-02-18_14-30.csv", "18/02/2026"),
        Record(2, "Registro_2026-02-20_09-00.csv", "20/02/2026"),
        Record(3, "Registro_2026-02-20_16-45.csv", "20/02/2026"),
        Record(4, "Registro_2026-02-21_11-10.csv", "21/02/2026")
    )

    Scaffold(
        topBar = { StandardTopBar("Historial de Registros") }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(records) { record ->
                HistoryRecordCard(
                    record = record,
                    onExport = { /* Futura acción al exportar */ },
                    onView = { /* Futura acción al ver */ }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                AppButton(
                    text = "Volver",
                    onClick = { navController.popBackStack() },
                )
            }
        }
    }
}

/**
 * Componente Card que muestra un registro de datos capturados con la posibilidad de exportar y ver.
 *
 * @param record Información del registro.
 * @param onExport Callback invocado al exportar un registro.
 * @param onView Callback invocado al ver un registro.
 */
@Composable
fun HistoryRecordCard(
    record: Record,
    onExport: () -> Unit,
    onView: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = record.fileName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "ID: ${record.id}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Fecha: ${record.date}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                AppButton(
                    text = "Exportar",
                    onClick = onExport,
                    isPrimary = true
                )

                AppButton(
                    text = "Ver",
                    onClick = onView,
                    isPrimary = false
                )
            }
        }
    }
}