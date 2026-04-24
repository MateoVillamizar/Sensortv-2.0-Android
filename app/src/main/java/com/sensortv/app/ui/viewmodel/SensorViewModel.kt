package com.sensortv.app.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sensortv.app.data.model.BatteryData
import com.sensortv.app.data.model.SensorData
import com.sensortv.app.data.repository.BatteryRepository
import com.sensortv.app.data.service.CaptureServiceManager
import com.sensortv.app.data.service.SensorCaptureService
import com.sensortv.app.domain.ObserveSensorPowerUseCase
import com.sensortv.app.ui.model.SensorChartData
import com.sensortv.app.ui.model.SensorChartPoint
import com.sensortv.app.ui.utils.UiEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar la lógica de presentación y estado de SensorTV 2.0.
 * - Se encarga de transformar los datos de los sensores en representaciones visuales
 * (listas y gráficas) y de actuar como intermediario entre la UI y el servicio de captura en segundo plano.
 **
 * @param observeSensorPowerUseCase Caso de uso para calcular la potencia en tiempo real (P = V * I).
 * @param batteryRepository Repositorio para observar cambios en el estado de la batería.
 * @param context Contexto de la aplicación necesario para iniciar/detener [SensorCaptureService].
 */
class SensorViewModel(
    private val observeSensorPowerUseCase: ObserveSensorPowerUseCase,
    private val batteryRepository: BatteryRepository,
    @SuppressLint("StaticFieldLeak") private val context: Context
): ViewModel() {

    // Estados de UI (Observables por Compose)

    /** Lista de sensores detectados con su potencia instantánea. */
    private val _sensorList = MutableStateFlow<List<SensorData>>(emptyList())
    val sensorList: StateFlow<List<SensorData>> = _sensorList

    /** Estado actual de la batería (voltaje y porcentaje -> nivel de carga). */
    private val _batteryState = MutableStateFlow<BatteryData?>(null)
    val batteryState: StateFlow<BatteryData?> = _batteryState

    /** Datos estructurados para la gráfica de potencia vs tiempo (Canvas). */
    private val _sensorChartData = MutableStateFlow<List<SensorChartData>>(emptyList())
    val sensorChartData: StateFlow<List<SensorChartData>> = _sensorChartData

    // Estados de Captura (Sincronizados con el Manager)

    /** Estado de captura sincronizado con el Servicio de segundo plano (SensorCaptureService)
     * Su valor es sincronizado con el estado global gestionado por el
     * SensorCaptureService a través de CaptureServiceManager.
     */
    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing

    private val _remainingTime = MutableStateFlow(0)
    val remainingTime: StateFlow<Int> = _remainingTime

    // Variables de Control Interno
    private var lastChartUpdate: Long = 0
    private var startTime: Long = 0
    private var isManualCancel = false

    /** Frecuencia de actualización de la gráfica. Se sincroniza con
     * la frecuencia de la captura activa.
     */
    private var userSamplingFrequency: Int = 3

    /** Tarea para la observación de sensores en tiempo real */
    private var monitoringJob: Job? = null

    // Eventos de UI (Notificaciones)

    /**
     * Canal privado para la emisión de eventos únicos de UI (Toasts, Navegación).
     * - [Channel.BUFFERED] permite que los eventos se mantengan en cola si la UI no está lista.
     */
    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()  // receiveAsFlow asegura que cada evento se consuma una sola vez

    init {
        startMonitoring()
        observeBattery()
        observeServiceState()
    }

    /**
     * Suscribe al ViewModel a los cambios de en el estado de la batería.
     *
     * - Observa el flujo proveniente del repositorio.
     * - Actualiza el estado observable [_batteryState] cada vez que el sistema
     * notifica una variación en el voltaje o nivel de carga.
     */
    private fun observeBattery() {
        viewModelScope.launch {
            batteryRepository.observeBattery().collect { batteryData ->
                _batteryState.value = batteryData
            }
        }
    }

    /**
     * Recolecta el flujo de potencia de los sensores y coordina las actualizaciones de UI.
     *
     * - Actualiza la lista de sensores en cada emisión para mantener valores instantáneos.
     * - La gráfica se actualiza según la frecuencia de muestreo para optimizar el consumo de recursos.
     *
     * Nota:
     * Este flujo es independiente del proceso de captura persistente, el cual es
     * gestionado por el SensorCaptureService.
     */
    @OptIn(FlowPreview::class)
    private suspend fun observeSensors() {
        observeSensorPowerUseCase().collect { newData ->
            // Actualización inmediata de la lista de texto/valores
            updateSensorList(newData)

            // Actualización controlada de la gráfica
            val currentTime = System.currentTimeMillis()
            val intervalMillis = userSamplingFrequency * 1000
            if (currentTime - lastChartUpdate >= intervalMillis) {
                updateChartData(_sensorList.value)
                lastChartUpdate = currentTime
            }
        }
    }

    /**
     * Actualiza la lista de sensores manteniendo inmutabilidad.
     *
     * 1. Identificar si el sensor entrante ya está registrado mediante una bandera (sensorExists).
     * 2. Si el sensor ya existe, se actualizan sus valores.
     * 3. Si el sensor es nuevo, se agrega a la lista.
     *
     * @param newData Información actualizada del sensor proveniente del repositorio.
     */
    private fun updateSensorList(newData: SensorData) {
        _sensorList.update { currentList ->

            var sensorExists = false

            val updatedList = currentList.map { sensor ->
                if (sensor.type == newData.type) {
                    sensorExists = true
                    sensor.copy(
                        values = newData.values,
                        frequencyHz = newData.frequencyHz,
                        isAvailable = true,
                        estimatedPowerMw = newData.estimatedPowerMw
                    )
                } else sensor
            }

            if (sensorExists) updatedList else updatedList + newData
        }
    }

    /**
     * Actualiza los datos de la gráfica (tiempo vs potencia) para cada sensor.
     *
     * - Convierte la lista actual en un mapa indexado por tipo de sensor para optimizar
     * el acceso y actualización de datos.
     * - Calcula el tiempo transcurrido (elapsedSeconds) desde el inicio y genera un
     * nuevo punto (P, t) para cada sensor activo.
     * - Limita el histórico a los últimos 25 puntos para mantener la fluidez en la UI.
     *
     * @param sensors Lista de sensores con las mediciones de potencia calculadas.
     */
    private fun updateChartData(sensors: List<SensorData>) {
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000f

        _sensorChartData.update { currentCharts ->

            val chartMap = currentCharts.associateBy { it.sensorType }.toMutableMap()

            sensors.forEach { sensor ->
                val newPoint = SensorChartPoint(
                    timeStamp = elapsedSeconds,
                    powerMw = sensor.estimatedPowerMw
                )

                val existingChart = chartMap[sensor.type]

                // Se actualiza la gráfica existente o se crea una nueva
                chartMap[sensor.type] =
                    if (existingChart != null) {
                        existingChart.copy(
                            points = (existingChart.points + newPoint).takeLast(25)
                        )
                    } else {
                        SensorChartData(
                            sensorType = sensor.type,
                            displayName = sensor.displayName,
                            points = listOf(newPoint)
                        )
                    }
            }
            chartMap.values.toList() // Se convierte de map a lista para que Compose pueda iterar sobre ella
        }
    }

    /**
     * Inicia el monitoreo de sensores del dispositivo para UI.
     *
     * - Activa la recolección del flujo de sensores dentro del [viewModelScope].
     * - Inicializa el tiempo base utilizado para la gráfica.
     * - Lanza una corrutina vinculada al ciclo de vida del ViewModel para observar los sensores.
     */
    fun startMonitoring() {
        if (monitoringJob != null) return
        startTime = System.currentTimeMillis()

        monitoringJob = viewModelScope.launch {
            observeSensors()
        }
    }

    /**
     * Detiene la observación de sensores y cancela la corrutina asociada [monitoringJob].
     *
     * - Reinicia el tiempo base utilizado en las gráficas.
     */
    fun stopMonitoring() {
        startTime = 0
        monitoringJob?.cancel()
        monitoringJob = null
    }

    /**
     * Reinicia el estado del monitoreo de sensores en la capa de UI.
     *
     * - Detiene procesos actuales, limpia las listas de sensores y datos de gráficas,
     * y vuelve a iniciar la recolección de datos.
     */
    fun restartMonitoring() {
        stopMonitoring()
        _sensorList.value = emptyList()
        _sensorChartData.value = emptyList()
        lastChartUpdate = 0
        startMonitoring()
    }

    /**
     * Inicia la captura de datos delegando la responsabilidad al [SensorCaptureService].
     *
     * - Sincroniza la frecuencia de actualización de la gráfica con la frecuencia de captura
     * definida por el usuario.
     *
     * @param durationMinutes Duración total de la sesión de captura.
     * @param samplingFrequency Frecuencia de muestreo, el intervalo entre registros.
     */
    fun startCapture(durationMinutes: Int, samplingFrequency: Int) {
        // Ajustar la frecuencia local para que la gráfica coincida con la captura
        this.userSamplingFrequency = samplingFrequency
        // Limpiar la gráfica antes de empezar para que sea una "sesión limpia"
        restartMonitoring()

        val intent = Intent(context, SensorCaptureService::class.java).apply {
            putExtra("EXTRA_DURATION", durationMinutes)
            putExtra("EXTRA_FREQUENCY", samplingFrequency)
        }

        // Iniciar el servicio de primer plano
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Detiene el servicio de captura de forma inmediata.
     * La persistencia de los datos y el reseteo del estado global
     * son responsabilidad del servicio.
     */
    fun stopCapture() {
        val intent = Intent(context, SensorCaptureService::class.java)
        context.stopService(intent)
        CaptureServiceManager.resetState()
    }

    /**
     * Cancela la captura de datos actual sin persistir información y
     * notifica al usuario.
     */
    fun cancelCapture() {
        if (_isCapturing.value) {
            isManualCancel = true
            stopCapture()
        }
    }

    /**
     * Observa el estado global del [CaptureServiceManager].
     * Garantiza que la UI permanezca consistente con el estado real del servicio,
     * incluso si el ViewModel fue recreado o la app estuvo en segundo plano.
     */
    private fun observeServiceState() {
        viewModelScope.launch {
            CaptureServiceManager.captureState.collect { state ->
                val wasCapturing = _isCapturing.value
                val isNowCapturing = state.isCapturing

                // Detectar inicio de captura para sincronizar la frecuencia visual
                if (!wasCapturing && isNowCapturing) {
                    userSamplingFrequency = state.samplingFrequency
                    restartMonitoring()
                }

                // Detectar fin de captura para notificar al usuario
                if (wasCapturing && !isNowCapturing) {
                    userSamplingFrequency = 3 // Volver a frecuencia base original
                    restartMonitoring()

                    if (isManualCancel) {
                        sendUiMessage("Captura cancelada y servicio detenido")
                    } else {
                        sendUiMessage("Captura finalizada y guardada")
                    }

                    isManualCancel = false
                }

                // Sincronización de estados reactivos
                _isCapturing.value = state.isCapturing
                _remainingTime.value = state.remainingSeconds
            }
        }
    }

    /**
     * Envía un evento único a la UI (mostrar un Toast).
     *
     * @param message Texto a mostrar en el Toast.
     */
    fun sendUiMessage(message: String) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowToast(message))
        }
    }
}