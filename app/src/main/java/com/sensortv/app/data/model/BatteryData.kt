package com.sensortv.app.data.model

/**
 * Representa el estado energético actual del dispositivo móvil.
 *
 * @property percentage Nivel de carga de la batería (0-100%).
 * @property voltage Tensión eléctrica actual expresada en Voltios (V).
 * Es el factor 'V' en la ecuación de potencia P = V * I.
 */
data class BatteryData(
    val percentage: Int,
    val voltage: Float
)