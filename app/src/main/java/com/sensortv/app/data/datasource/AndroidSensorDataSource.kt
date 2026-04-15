package com.sensortv.app.data.datasource

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementación concreta de [SensorDataSource] que interactúa con el hardware a través del [SensorManager].
 * - Convierte las lecturas de los sensores del sistema en un flujo [Flow] asíncrono,
 * gestionando el ciclo de vida del hardware para evitar consumo innecesario de energía.
 *
 * @param context Contexto necesario para obtener el servicio SENSOR_SERVICE.
 */
class AndroidSensorDataSource(
    private val context: Context
) : SensorDataSource, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lastTimestamps = mutableMapOf<Int, Long>()      // Identificador del sensor -> Último timestamp
    private lateinit var emitter: (SensorData) -> Unit

    /**
     * Activa los sensores seleccionados y comienza la emisión de datos.
     * - Registra los listeners al iniciar la recolección y los des-registra
     * automáticamente cuando el colector se detiene (gracias a [awaitClose]).
     *
     * @return [Flow] de [SensorData] con frecuencia en tiempo real.
     */
    override fun observeSensorData(): Flow<SensorData> = callbackFlow {
        // Configurar el emisor interno para conectar el callback con el Flow
        emitter = { data ->
            trySend(data)
        }

        val sensorTypes = listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_PROXIMITY
        )

        // Registro para cada sensor
        sensorTypes.forEach { type ->
            sensorManager.getDefaultSensor(type)?.let { sensor ->
                sensorManager.registerListener(
                    this@AndroidSensorDataSource,
                    sensor,
                    SensorManager.SENSOR_DELAY_UI // Velocidad de actualización apta para interfaz
                )
            }
        }

        // Garantía de limpieza al cerrar el flujo
        awaitClose {
            sensorManager.unregisterListener(this@AndroidSensorDataSource)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario en la aplicación actual. Implementación es obligatoria por contrato
    }

    /**
     * Callback invocado por el sistema Android cada vez que un sensor registra un cambio (nuevos datos).
     * Procesa cada actualización de hardware detectada por el sistema.
     *
     * 1. Calcula la frecuencia (Hz) basándose en el tiempo transcurrido desde el último evento.
     * 2. Empaqueta los datos en [SensorData] y lo envía al flujo mediante el 'emitter'.
     *
     * @param event paquete de datos de Android cada vez que un sensor cambia. Contiene valores,
     * tipo de sensor y consumo nominal de corriente (mA).
    */
    override fun onSensorChanged(event: SensorEvent?) {

        event ?: return

        val type = event.sensor.type
        val currentTime = System.nanoTime()
        val frequency = calculateFrequency(type, currentTime)

        // Guardar el timestamp actual para el cálculo de la siguiente muestra
        lastTimestamps[type] = currentTime

        val data = SensorData(
            type = type,
            displayName = getSensorDisplayName(type),
            hardwareName = event.sensor.name,
            values = event.values.toList(),
            frequencyHz = frequency,
            isAvailable = true,
            nominalConsumptionmA = event.sensor.power
        )

        emitter(data)
    }

    /**
     * Obtiene el nombre legible para el usuario según el tipo de sensor en Hardware.
     *
     * @param type Identificador único del sensor en Hardware.
     * @return Nombre legible para el usuario.
     */
    private fun getSensorDisplayName(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Acelerómetro"
            Sensor.TYPE_GYROSCOPE -> "Giroscopio"
            Sensor.TYPE_LIGHT -> "Luminosidad"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetómetro"
            Sensor.TYPE_PROXIMITY -> "Proximidad"
            else -> "Sensor"
        }
    }

    /**
     * Calcula la frecuencia de muestreo en Hercios (Hz) basándose en el intervalo de tiempo.
     *
     * @param type Identificador del sensor para buscar su última marca de tiempo.
     * @param currentTime Tiempo actual en nanosegundos.
     * @return Frecuencia calculada como 1/Δt, o 0f si es la primera lectura.
     */
    private fun calculateFrequency(type: Int, currentTime: Long): Float {
        val lastTime = lastTimestamps[type] ?: return 0F

        // Convertimos la diferencia de nanosegundos a segundos (1s = 1,000,000,000ns)
        val deltaSeconds = (currentTime - lastTime) / 1_000_000_000f

        return if (deltaSeconds > 0f) 1f / deltaSeconds else 0f
    }
}