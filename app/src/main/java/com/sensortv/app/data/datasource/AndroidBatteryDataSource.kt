package com.sensortv.app.data.datasource

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.sensortv.app.data.model.BatteryData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementación concreta de [BatteryDataSource] que actúa como un puente entre el sistema operativo Android y la aplicación.
 * * Utiliza un [BroadcastReceiver] para capturar eventos del sistema y convertirlos
 * en un flujo de datos reactivo mediante [callbackFlow].
 *
 * @param context Contexto necesario para registrar receptores de señales del sistema.
 */
class AndroidBatteryDataSource(
    private val context: Context
) : BatteryDataSource {

    /**
     * Inicia el monitoreo reactivo del estado de la batería.
     * * Registra un receptor de emisiones (reciever) del sistema para capturar cambios en el nivel
     * y voltaje de la batería.
     * * Al dejar de observar este flujo, el receptor se destruye automáticamente para optimizar recursos.
     *
     * @return Un [Flow] que emite objetos [BatteryData] con valores normalizados (0-100% y Voltios).
     */
    override fun observeBattery(): Flow<BatteryData> = callbackFlow {
        // Definición del receptor de eventos
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {

                // Extracción y normalización de datos crudos del sistema
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1 // nivel actual de batería
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1 // nivel máximo posible de batería
                val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1 // Valor en milivoltios de batería

                val percentage = (level * 100) / scale
                val voltageVolts = voltage / 1000f

                // Emisión del dato procesado hacia el flujo
                trySend(BatteryData(percentage = percentage, voltage = voltageVolts))
            }
        }

        // Sintonización y activación del monitoreo, registro para empezar a escuchar
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        // Garantía de limpieza al cerrar el flujo
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}