package com.sensortv.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Selector de frecuencia de muestreo para la captura de datos.
 *
 * @param selected Valor de frecuencia actualmente seleccionada.
 * @param onSelected Callback invocado al seleccionar una nueva frecuencia.
 */
@Composable
fun SamplingFrequencySelector(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    val options = listOf(1, 3, 5)

    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { value ->
            // Componente visual de Material Design que puede estar activado o desactivado
            FilterChip(
                selected = selected == value,
                onClick = { onSelected(value) },
                label = { Text("$value s") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}