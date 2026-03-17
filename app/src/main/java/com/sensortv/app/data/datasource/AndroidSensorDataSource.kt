package com.sensortv.app.data.datasource

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.sensortv.app.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementación concreta de [SensorDataSource] que interactúa directamente con el hardware de Android.
 *
 * Esta clase implementa [SensorEventListener] para recibir actualizaciones del [SensorManager]
 * y las transforma en un flujo de datos asíncrono.
 *
 * @param context Contexto de la aplicación necesario para acceder al servicio de sensores.
 */
class AndroidSensorDataSource(
    private val context: Context
) : SensorDataSource, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /** Mapa para rastrear el último timestamp de cada sensor y calcular su frecuencia. */
    private val lastTimestamps = mutableMapOf<Int, Long>()

    // fCanal interno para emitir datos hacia el Flow.
    private lateinit var emitter: (SensorData) -> Unit

    /**
     * Registra los listeners para los sensores seleccionados y abre un flujo de datos.
     * Al cerrarse el flujo, se cancela automáticamente el registro de los sensores.
     *
     * @note La recolección de este Flow está sujeta al ciclo de vida del colector.
     * Si se utiliza en una ViewModel vinculada a una UI, la medición se detendrá
     * al destruir o pausar dicha UI debido a [awaitClose]. Para mediciones en
     * segundo plano, se recomienda ejecutar este Flow dentro de un Foreground Service.
     */
    override fun observeSensorData(): Flow<SensorData> = callbackFlow {

        emitter = { data ->
            trySend(data).isSuccess
        }

        val sensorTypes = listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_PROXIMITY
        )

        sensorTypes.forEach { type ->
            sensorManager.getDefaultSensor(type)?.let { sensor ->
                sensorManager.registerListener(
                    this@AndroidSensorDataSource,
                    sensor,
                    SensorManager.SENSOR_DELAY_UI // Velocidad de actualización apta para interfaz
                )
            }
        }

        // Se ejecuta cuando el Flow se cancela o se deja de observar
        awaitClose {
            sensorManager.unregisterListener(this@AndroidSensorDataSource)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No necesario por ahora
    }

    /**
     * Callback invocado por el sistema Android cada vez que un sensor tiene nuevos datos.
     * Realiza el cálculo de frecuencia y empaqueta la información en [SensorData].
     *
     * @param event Objeto [SensorEvent] que contiene los datos del sensor.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        val type = event.sensor.type
        val currentTime = System.nanoTime()
        val last = lastTimestamps[type] ?: 0L

        // Cálculo de frecuencia: f = 1 / (tiempo_actual - tiempo_previo)
        val frequency = if (last != 0L) {
            val delta = (currentTime - last) / 1_000_000_000f

            if (delta > 0f)
                1f / delta
            else
                0f

        } else {
            0f
        }

        lastTimestamps[type] = currentTime

        val data = SensorData(
            type = type,
            displayName = getSensorDisplayName(type),
            hardwareName = event.sensor.name,
            values = event.values.toList(),
            frequencyHz = frequency,
            available = true,
            nominalConsumptionmA = event.sensor.power
        )

        emitter(data)
    }

    /**
     * Obtiene el nombre legible del tipo de sensor de Android.
     */
    private fun getSensorDisplayName(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Acelerómetro"
            Sensor.TYPE_GYROSCOPE -> "Giroscopio"
            Sensor.TYPE_LIGHT -> "Sensor de Luz"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetómetro"
            Sensor.TYPE_PROXIMITY -> "Proximidad"
            else -> "Sensor"
        }
    }
}