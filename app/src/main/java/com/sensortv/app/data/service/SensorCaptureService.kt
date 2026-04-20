package com.sensortv.app.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sensortv.app.R
import com.sensortv.app.data.datasource.AndroidBatteryDataSource
import com.sensortv.app.data.datasource.AndroidCsvDataSource
import com.sensortv.app.data.datasource.AndroidSensorDataSource
import com.sensortv.app.data.model.DatabaseProvider
import com.sensortv.app.data.model.SensorResult
import com.sensortv.app.data.repository.BatteryRepositoryImpl
import com.sensortv.app.data.repository.CaptureRepositoryImpl
import com.sensortv.app.data.repository.SensorRepositoryImpl
import com.sensortv.app.domain.CalculateEnergyUseCase
import com.sensortv.app.domain.ObserveSensorPowerUseCase
import com.sensortv.app.domain.SaveCaptureUseCase
import com.sensortv.app.domain.StartCaptureTimerUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Servicio de primer plano encargado de la recolección (captura) persistente de datos.
 *
 * - Garantiza que la medición y el guardado en CSV no se interrumpan al minimizar la app
 * o bloquear el dispositivo, gestionando su propio ciclo de vida y recursos.
 */
class SensorCaptureService : Service() {

    // Scope principal del servicio y jobs para controlar la captura y el monitoreo de sensores
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var captureJob: Job? = null
    private var monitoringJob: Job? = null

    // Dependencias de la Capa de Dominio. Se inicializan en onCreate()
    private lateinit var observeSensorPowerUseCase: ObserveSensorPowerUseCase
    private lateinit var startCaptureTimerUseCase: StartCaptureTimerUseCase
    private lateinit var calculateEnergyUseCase: CalculateEnergyUseCase
    private lateinit var saveCaptureUseCase: SaveCaptureUseCase

    /** Lista temporal de registros de mediciones para el archivo CSV */
    private val capturedLines = mutableListOf<String>()

    /** Mapa de energía acumulada por tipo de sensor durante la sesión */
    private val sensorEnergyMap = mutableMapOf<Int, Float>()

    /** Mapa para almacenar el último valor de potencia (mW) recibido para cada sensor */
    private val latestSensorValues = mutableMapOf<Int, Float>()

    /**
     * Crea el servicio de captura en segundo plano.
     *
     * - Configura las dependencias necesarias para la recolección de datos.
     * - Crea el canal de notificación requerido para servicios en primer plano.
     * - Inicia el monitoreo continuo de sensores para mantener valores actualizados.
     *
     * Se ejecuta una única vez durante el ciclo de vida del servicio (Creación del servicio).
     */
    override fun onCreate() {
        super.onCreate()
        initializeDependencies()
        createNotificationChannel()
        startContinuousMonitoring()
        Log.d("SERVICE_DEBUG", "Servicio creado")
    }

    /**
     * Inicializa manualmente las dependencias necesarias siguiendo el patrón
     * DataSources → Repositories → UseCases.
     *
     * - Requiere el contexto del sistema para acceder a recursos como sensores,
     * base de datos y almacenamiento.
     */
    private fun initializeDependencies() {
        val db = DatabaseProvider.getDatabase(this)
        val captureRepo = CaptureRepositoryImpl(db.captureDao())
        val sensorRepo = SensorRepositoryImpl(AndroidSensorDataSource(this))
        val batteryRepo = BatteryRepositoryImpl(AndroidBatteryDataSource(this))
        val csvDataSource = AndroidCsvDataSource(this)

        observeSensorPowerUseCase = ObserveSensorPowerUseCase(sensorRepo, batteryRepo)
        startCaptureTimerUseCase = StartCaptureTimerUseCase()
        calculateEnergyUseCase = CalculateEnergyUseCase()
        saveCaptureUseCase = SaveCaptureUseCase(csvDataSource, captureRepo)
    }

    /**
     * Inicia una corrutina persistente que recolecta datos de los sensores en segundo plano.
     *
     * - Escucha continuamente el flujo de potencia estimada de cada sensor.
     * - Actualiza el mapa latestSensorValues con el último valor disponible por tipo.
     * - Desacopla la adquisición de datos del proceso de muestreo
     *
     * El monitoreo permanece activo durante toda la vida del servicio.
     */
    private fun startContinuousMonitoring() {
        monitoringJob = serviceScope.launch(Dispatchers.Default) { // default está optimizado para tareas de CPU fuera del hilo principal
            observeSensorPowerUseCase().collect { data ->
                latestSensorValues[data.type] = data.estimatedPowerMw

                Log.d("SENSOR_DEBUG", "Tipo: ${data.type} Valor: ${data.estimatedPowerMw}")
            }
        }
    }

    /**
     * Punto de entrada del servicio cuando es iniciado mediante un Intent.
     *
     * - Recupera los parámetros de duración y frecuencia de muestreo.
     * - Promueve el servicio a Foreground Service para garantizar su ejecución,
     *   continua incluso en segundo plano.
     * - Inicia el proceso de captura de datos con duración y frecuencia especificadas.
     *
     * @return START_NOT_STICKY indica que el sistema no recreará el servicio automáticamente
     * si es finalizado.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val duration = intent?.getIntExtra("EXTRA_DURATION", 1) ?: 1
        val frequency = intent?.getIntExtra("EXTRA_FREQUENCY", 3) ?: 3

        // El servicio se promociona a Foreground inmediatamente para cumplir con Android 14+
        startForeground(1, createNotification("Iniciando captura de datos..."))
        startDataCapture(duration, frequency)

        return START_NOT_STICKY
    }

    /**
     * Inicia el proceso de captura de datos durante un intervalo definido.
     *
     * - Cancela cualquier captura previa y reinicia estructuras de almacenamiento asociadas.
     * - Lanza una corrutina principal que coordina dos tareas concurrentes:
     * 1) Cronómetro: actualiza el tiempo restante y la notificación cada segundo.
     * 2) Muestreo: registra snapshots de los sensores según la frecuencia definida.
     *
     * Ambas tareas se ejecutan en paralelo para desacoplar la precisión temporal del
     * procesamiento de muestreo.
     *
     * @param durationMinutes Tiempo total de la sesión de captura en minutos.
     * @param samplingFrequency Intervalo de muestreo en segundos.
     */
    private fun startDataCapture(durationMinutes: Int, samplingFrequency: Int) {
        captureJob?.cancel()
        capturedLines.clear()
        sensorEnergyMap.clear()

        val totalSeconds = durationMinutes * 60

        captureJob = serviceScope.launch {
            // Esperar un momento para asegurar que el startContinuousMonitoring() ha poblado los datos iniciales
            delay(500)

            CaptureServiceManager.updateState(
                CaptureServiceManager.captureState.value.copy(
                    isCapturing = true,
                    samplingFrequency = samplingFrequency
                )
            )

            // 1. Lanzar Cronómetro para actualización de UI y Notificación
            launch(Dispatchers.Default) {
                for (secondsLeft in (durationMinutes * 60) downTo 0) {
                    CaptureServiceManager.updateState(
                        CaptureServiceManager.captureState.value.copy(
                            remainingSeconds = secondsLeft,
                            isCapturing = true
                        )
                    )
                    updateNotification("Tiempo restante: ${secondsLeft / 60}m ${secondsLeft % 60}s")

                    if (secondsLeft == 0) {
                        finalizeCapture(durationMinutes, samplingFrequency)
                    }
                    delay(1000) // 1 iteración = 1 segundo real
                }
            }

            // 2. Lanzar muestreo de datos (Snapshot) independiente del reloj de la UI
            launch(Dispatchers.IO) {
                for (elapsed in 0 until totalSeconds) {
                    val remaining = totalSeconds - elapsed

                    // Procesar cada N segundo según frecuencia
                    if (remaining % samplingFrequency == 0) {
                        processSnapshot(samplingFrequency)
                    }
                    delay(1000)
                }
            }
        }
    }

    /**
     * Genera un registro (Snapshot) de las mediciones actuales de los sensores y lo almacena en memoria.
     *
     * Implementa retención de último valor conocido: si un sensor no ha emitido
     * un nuevo evento, se utiliza el último valor conocido de [latestSensorValues] para
     * mantener continuidad en la señal cuando un sensor no emite nuevos eventos.
     *
     * @param frequency Intervalo de muestreo en segundos utilizado como Δt para calculo de energía.
     */
    private suspend fun processSnapshot(frequency: Int) {
        val timeTag = java.time.Instant.now().toString()

        // 1. Construcción de la fila CSV con formato localizado para asegurar puntos decimales
        val row = String.format(
            java.util.Locale.US,
            "%s,%d,%.7f,%.7f,%.7f,%.7f,%.7f",
            timeTag,
            frequency,
            latestSensorValues[Sensor.TYPE_LIGHT] ?: 0f,
            latestSensorValues[Sensor.TYPE_PROXIMITY] ?: 0f,
            latestSensorValues[Sensor.TYPE_ACCELEROMETER] ?: 0f,
            latestSensorValues[Sensor.TYPE_MAGNETIC_FIELD] ?: 0f,
            latestSensorValues[Sensor.TYPE_GYROSCOPE] ?: 0f
        )

        capturedLines.add(row)

        // 2. Integración discreta para el cálculo de energía: E = P * Δt
        // Se acumula energía incremental usando el último valor de potencia disponible.
        latestSensorValues.forEach { (type, power) ->
            val currentEnergy = sensorEnergyMap[type] ?: 0f
            val energyIncrement = calculateEnergyUseCase(power, frequency)
            sensorEnergyMap[type] = currentEnergy + energyIncrement
        }
    }

    /**
     * Coordina el cierre de la sesión de captura de datos, la persistencia de los datos
     * en CSV (mediante un caso de uso) y base de datos, y la liberación de los recursos
     * del Foreground Service.
     */
    private suspend fun finalizeCapture(durationMinutes: Int, samplingFrequency: Int) {
        // Formato de nombre de archivo basado en la fecha/hora actual
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))

        val sensorResults = prepareFinalResults()

        try {
            // Persistencia delegada al Caso de Uso (Capa de Datos)
            saveCaptureUseCase(
                timestamp = timestamp,
                durationMinutes = durationMinutes,
                samplingFrequency = samplingFrequency,
                allMeasurements = capturedLines,
                sensorResults = sensorResults
            )
        } catch (e: Exception) {
            Log.e("SensorService", "Error crítico al guardar CSV: ${e.message}")
        } finally {
            // Limpieza absoluta de estados para permitir futuras capturas
            CaptureServiceManager.resetState()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    /**
     * Consolida los datos de energía acumulada por sensor en objetos [SensorResult]
     *
     * Cada resultado representa el consumo total de energía registrado durante
     * toda la sesión de captura, listo para persistencia o visualización.
     */
    private fun prepareFinalResults(): List<SensorResult> {
        val currentTimestamp = java.time.Instant.now().toString()
        val displayNames = mapOf(
            Sensor.TYPE_ACCELEROMETER to "Acelerómetro",
            Sensor.TYPE_GYROSCOPE to "Giroscopio",
            Sensor.TYPE_LIGHT to "Luminosidad",
            Sensor.TYPE_MAGNETIC_FIELD to "Magnetómetro",
            Sensor.TYPE_PROXIMITY to "Proximidad"
        )

        return sensorEnergyMap.map { (type, totalJoules) ->
            SensorResult(
                sensorType = type,
                displayName = displayNames[type] ?: "Sensor",
                estimatedPowerMw = 0f, // No relevante en el resumen final
                totalEnergyJ = totalJoules,
                totalEnergymJ = totalJoules * 1000f,
                timestamp = currentTimestamp
            )
        }
    }

    // Gestión de Interfaz y Notificaciones

    /**
     * Método_ requerido por la clase Service.
     *
     * Este servicio no soporta binding, ya que funciona como un Foreground Service
     * autónomo encargado exclusivamente de la captura de datos en segundo plano.
     *
     * @return null porque no se expone un canal de comunicación directa.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Crea el canal de notificación requerido para ejecutar el Foreground Service
     * en dispositivos con Android 8.0 (API 26) o superior.
     *
     * - IMPORTANCE_DEFAULT se utiliza porque la notificación solo informa
     * el estado del servicio sin requerir alta prioridad visual o sonora.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Verificar versión actual >= API 26 (Android 8.0)
            val channel = NotificationChannel(
                "sensor_capture_channel",
                "Monitoreo de Sensores",
                NotificationManager.IMPORTANCE_LOW
            )

            // Servicio del sistema que controla notificaciones y crear canal de notificaciones
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Construye la notificación persistente utilizada por el Foreground Service.
     *
     * Esta notificación es obligatoria para mantener el servicio activo en segundo plano
     * y mostrar al usuario que la aplicación está ejecutando una tarea continua.
     *
     * @param content Texto dinámico que refleja el estado actual de la captura.
     * @return Notificación configurada para el servicio en primer plano.
     */
    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "sensor_capture_channel")
            .setContentTitle("SensorTV 2.0 en ejecución")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true) // persistente, no deslizable
            .build()
    }

    /**
     * Actualiza la notificación activa del Foreground Service.
     *
     * Utiliza el mismo ID de notificación para reemplazar el contenido existente
     * sin generar múltiples notificaciones en el sistema.
     *
     * @param content Texto actualizado que refleja el estado actual del servicio.
     */
    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    /**
     * Libera todos los recursos asociados al Foreground Service.
     *
     * - Cancela las corrutinas activas relacionadas con el monitoreo y captura.
     * - Detiene el scope principal del servicio para evitar ejecución en segundo plano.
     * - Resetea el estado compartido con la UI para evitar inconsistencias visuales.
     */
    override fun onDestroy() {
        monitoringJob?.cancel()
        serviceScope.cancel()
        CaptureServiceManager.resetState()
        super.onDestroy()
    }
}