package com.sensortv.app.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Caso de uso encargado de gestionar el temporizador de una sesión de captura.
 *
 * Encapsula la lógica de conversión de minutos a segundos y la cuenta regresiva
 * mediante un flujo reactivo.
 */
class StartCaptureTimerUseCase {

    /**
     * Inicia una cuenta regresiva que emite los segundos restantes.
     *
     * @param durationMinutes Tiempo total de captura solicitado en minutos.
     * @return [Flow] que emite el tiempo restante en segundos cada 1000ms (1s).
     */
    operator fun invoke(durationMinutes: Int): Flow<Int> = flow {
        var remainingSeconds = durationMinutes * 60

        while(remainingSeconds >= 0) {
            emit(remainingSeconds)
            delay(1000)
            remainingSeconds--
        }
    }
}