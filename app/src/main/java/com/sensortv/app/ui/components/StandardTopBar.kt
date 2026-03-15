package com.sensortv.app.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Componente de la barra superior estándar para las pantallas de la aplicación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopBar(TopTitle: String) {
    CenterAlignedTopAppBar(
        title = { Text(TopTitle) },
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = Color(0xFF1F3A8A)
        )
    )
}