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
 * Implementación concreta de [BatteryDataSource] que monitorea el estado de la batería mediante el sistema Android.
 * Esta clase implementa [BroadcastReceiver] para escuchar cambios en el sistema mediante el Intent
 *
 * @param context Contexto de la aplicación necesario para acceder al servicio de batería.
 */
class AndroidBatteryDataSource(
    private val context: Context
) : BatteryDataSource {

    /**
     * Observa los cambios en el nivel y voltaje de la batería en tiempo real.
     *
     * @return Un [Flow] que emite [BatteryData] cada vez que el sistema notifica un cambio
     * en el estado de energía.
     */
    override fun observeBattery(): Flow<BatteryData> = callbackFlow {

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {

                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1

                val percentage = (level * 100) / scale
                val voltageVolts = voltage / 1000f

                trySend(
                    BatteryData(
                        percentage = percentage,
                        voltage = voltageVolts
                    )
                )
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}