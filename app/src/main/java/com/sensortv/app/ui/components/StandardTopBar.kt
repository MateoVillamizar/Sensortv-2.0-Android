package com.sensortv.app.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign

/**
 * Componente de una barra superior estándar para todas las pantallas de la aplicación.
 *
 * @param topTitle Título a mostrar en la barra superior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopBar(topTitle: String) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = topTitle,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}