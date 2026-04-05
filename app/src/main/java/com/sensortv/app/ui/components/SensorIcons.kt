package com.sensortv.app.ui.components

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*

/**
 * Función que devuelve el icono correspondiente para un tipo de sensor específico
 *
 * @param sensorName Nombre del tipo de sensor.
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