package com.sensortv.app.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Componente de la barra superior estándar para las pantallas de la aplicación.
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
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}