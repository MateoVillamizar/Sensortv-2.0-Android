package com.sensortv.app.domain

/**
 * Caso de uso que aplica la lógica de cálculo para convertir potencia en energía.
 * Fórmula: Energía (J) = (Potencia (mW) / 1000) * Tiempo (s)
 */
class CalculateEnergyUseCase {

    /**
     * Calcula el incremento de energía entre dos muestras.
     * @param estimatedPowerMw Potencia actual estimada medida en miliwatts.
     * @param intervalSeconds Tiempo transcurrido desde la última muestra (frecuencia de muestreo).
     * @return Energía consumida en ese intervalo expresada en Joules (J).
     */
    operator fun invoke(estimatedPowerMw: Float, intervalSeconds: Int): Float {
        return if (estimatedPowerMw <= 0) 0F
        else (estimatedPowerMw / 1000f) * intervalSeconds
    }
}