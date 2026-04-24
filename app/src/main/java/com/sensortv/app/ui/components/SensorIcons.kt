package com.sensortv.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Función que devuelve el icono correspondiente para un tipo de sensor específico
 *
 * @param sensorName Nombre legible del sensor (displayName).
 * @return El icono correspondiente al tipo de sensor.
 */
fun getSensorIcon(sensorName: String): ImageVector {
    return when (sensorName) {
        "Acelerómetro" -> Icons.Default.Sensors
        "Giroscopio" -> Icons.AutoMirrored.Filled.RotateRight
        "Proximidad" -> Icons.Default.Visibility
        "Luminosidad" -> Icons.Default.LightMode
        "Magnetómetro" -> Icons.Default.Explore
        else -> Icons.Default.DeviceUnknown
    }
}