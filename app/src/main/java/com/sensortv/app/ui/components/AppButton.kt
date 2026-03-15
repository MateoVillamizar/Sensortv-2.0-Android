package com.sensortv.app.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Componente de botón personalizado para todas las pantallas de la aplicación.
 * Dependiendo de si el botón es primario o secundario, se mostrará un estilo de botón
 * 'filled' o 'tonal' respectivamente.
 *
 * @param text Texto del botón.
 * @param onClick Función a ejecutar al hacer clic en el botón.
 * @param modifier Modificadores para personalizar el diseño del botón.
 * @param isPrimary Indica si el botón es de alto énfasis (true) o de énfasis medio (false).
 * @param enabled Indica si el botón está habilitado para ser interactuado.
 * @param icon Icono a mostrar en el botón (opcional).
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    enabled: Boolean = true,
) {
    // Botón de alto énfasis (Filled Button)
    if (isPrimary) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = modifier
                .wrapContentSize()
                .height(48.dp),
            enabled = enabled,
            shape = ButtonDefaults.shape
        ) {
            Text(
                text = text,
                // Aplica automáticamente 14.sp y otros lineamientos de M3
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }

    // Botón de énfasis medio (Tonal)
    else {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier
                .wrapContentSize()
                .height(48.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            shape = ButtonDefaults.shape
        ) {
            Text(
                text = text,
                // Aplica automáticamente 14.sp y otros lineamientos de M3
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}