package com.sensortv.app.data.datasource

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.sensortv.app.model.BatteryData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementación concreta de [BatteryDataSource] que interactúa directamente con el hardware de Android.
 * Esta clase implementa [BroadcastReceiver] para recibir actualizaciones del nivel de batería.
 *
 * @param context Contexto de la aplicación necesario para acceder al servicio de batería.
 */
class AndroidBatteryDataSource(
    private val context: Context
) : BatteryDataSource {

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