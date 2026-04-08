package com.sensortv.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.sensortv.app.data.model.CaptureRecordEntity
import com.sensortv.app.ui.components.AppButton
import com.sensortv.app.ui.components.StandardTopBar
import com.sensortv.app.ui.viewmodel.HistoryViewModel

/**
 * Pantalla que muestra el historial de registros de datos capturados.
 * Permite exportar y ver los registros.
 *
 * @param viewModel Instancia de [HistoryViewModel] asociado al historial.
 * @param navController Controlador de navegación utilizado para cambiar de pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    navController: NavHostController
) {
    // Convertir el StateFlow en un Estado de Compose
    val records by viewModel.historyRecords.collectAsStateWithLifecycle()

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
                    onDelete = { viewModel.deleteRecord(record) },
                    onShare = { /* Futura acción al exportar */ },
                    onView = { /* Futura acción al ver */ }
                )
            }

            if (records.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No hay registros guardados actualmente",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
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
 * @param onDelete Callback invocado al eliminar un registro
 * @param onShare Callback invocado al exportar un registro.
 * @param onView Callback invocado al ver un registro.
 */
@Composable
fun HistoryRecordCard(
    record: CaptureRecordEntity,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onView: () -> Unit
) {
    // Formateador de fecha simple
    val dateLabel = java.time.Instant.ofEpochMilli(record.dateMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = "Duración: ${record.durationMinutes} min | Muestreo: ${record.samplingFrequencySeconds}(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Fecha: $dateLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Utilización de pesos (weight) para que los botones se repartan el espacio
                Box(modifier = Modifier.weight(1f)) {
                    AppButton(text = "Ver", onClick = onView, isPrimary = true)
                }
                Box(modifier = Modifier.weight(1f)) {
                    AppButton(text = "Compartir", onClick = onShare, isPrimary = true)
                }
                // Botón de eliminar (puedes usar un IconButton si prefieres)
                Box(modifier = Modifier.weight(0.5f)) {
                    AppButton(text = "X", onClick = onDelete, isPrimary = false)
                }
            }
        }
    }
}