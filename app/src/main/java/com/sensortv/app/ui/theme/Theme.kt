package com.sensortv.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary
)

private val LightColorScheme = lightColorScheme(

    // Colores principales de la identidad de la app (botones principales, acciones importantes)
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    // Colores para acciones secundarias y elementos interactivos de menor énfasis
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    // Colores para elementos complementarios o visualización de datos
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    // Colores para estados de error o advertencia en la aplicación
    error = Error,
    onError = onError,
    errorContainer = ErrorContainerRed,
    onErrorContainer = onErrorContainer,

    // Colores del fondo principal de las pantallas
    background = BackgroundLight,
    onBackground = OnSurface,

    // Colores de superficies como Cards, tablas o contenedores de información
    surface = Surface,
    onSurface = OnSurface,
    surfaceDim = SurfaceDim,
    onSurfaceVariant = onSurfaceVariant,

    // Color utilizado para bordes, divisores y elementos de separación
    outline = Outline
)

@Composable
fun SensorTV20Theme(
    /*
    No se Implementa el tema oscuro o dinámico
    darkTheme: Boolean = isSystemInDarkTheme(),
    Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    */

    content: @Composable () -> Unit
) {
    // Forzar siempre el tema claro
    val colorScheme = LightColorScheme

    // Configura la barra de estado del sistema para mostrar iconos oscuros
    val view = LocalView.current
    SideEffect {
        val window = (view.context as android.app.Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    /*
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    */

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}