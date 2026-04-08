package com.sensortv.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sensortv.app.data.model.BatteryData
import com.sensortv.app.data.model.SensorData
import com.sensortv.app.data.model.SensorResult
import com.sensortv.app.data.repository.BatteryRepository
import com.sensortv.app.domain.CalculateEnergyUseCase
import com.sensortv.app.domain.ObserveSensorPowerUseCase
import com.sensortv.app.domain.SaveCaptureUseCase
import com.sensortv.app.domain.StartCaptureTimerUseCase
import com.sensortv.app.ui.model.SensorChartData
import com.sensortv.app.ui.model.SensorChartPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar la lógica de presentación y estado de SensorTV 2.0.
 * - Transforma flujos de datos de dominio en estados observables por Jetpack Compose.
 * - Coordina la visualización de gráficas y el estado de la captura de datos.
 *
 * @param observeSensorPowerUseCase Caso de uso para calcular la potencia en tiempo real (P = V * I).
 * @param startCaptureTimerUseCase Caso de uso que gestiona la cuenta regresiva de captura.
 * @param calculateEnergyUseCase Caso de uso para calcular el incremento de energía por un sensor.
 * @param batteryRepository Repositorio para observar cambios en el estado de la batería.
 */
class SensorViewModel(
    private val observeSensorPowerUseCase: ObserveSensorPowerUseCase,
    private val startCaptureTimerUseCase: StartCaptureTimerUseCase,
    private val calculateEnergyUseCase: CalculateEnergyUseCase,
    private val saveCaptureUseCase: SaveCaptureUseCase,
    private val batteryRepository: BatteryRepository
): ViewModel() {

    // Estado de la UI (Observables)

    /** Lista actual de sensores activos */
    // StateFlow es para lectura desde UI. MutableStateFlow para actualización dentro del ViewModel
    private val _sensorList = MutableStateFlow<List<SensorData>>(emptyList())
    val sensorList: StateFlow<List<SensorData>> = _sensorList

    /** Estado actual de la batería (porcentaje y voltaje). */
    private val _batteryState = MutableStateFlow<BatteryData?>(null)
    val batteryState: StateFlow<BatteryData?> = _batteryState

    /** Datos estructurados para la visualización en gráficas de potencia en el tiempo. */
    private val _sensorChartData = MutableStateFlow<List<SensorChartData>>(emptyList())
    val sensorChartData: StateFlow<List<SensorChartData>> = _sensorChartData

    // Variables de Control de captura y tiempos
    private var lastChartUpdate: Long = 0

    private var startTime: Long = 0
    private var lastEnergyUpdate: Long = 0

    // Variables de captura de datos
    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing

    private val _remainingTime = MutableStateFlow(0)
    val remainingTime: StateFlow<Int> = _remainingTime
    private var userSamplingFrequency: Int = 3
    private val sensorEnergyMap = mutableMapOf<Int, Float>() // sensorType, Joules acumulados

    // Referencias a las tareas en ejecución (Corrutinas)
    private var captureJob: Job? = null
    private var monitoringJob: Job? = null

    init {
        startMonitoring()
        observeBattery()
    }

    /**
     * Suscribe al ViewModel a los cambios de en el estado de la batería.
     * Actualiza el estado observable [_batteryState] cada vez que el sistema
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
     * Inicia la recolección del flujo de datos de los sensores y coordina las actualizaciones de UI.

     * - Actualiza la lista de sensores en cada emisión para mantener valores instantáneos.
     * - Sincroniza la actualización de la gráfica para optimizar el rendimiento
     */
    @OptIn(FlowPreview::class)
    private suspend fun observeSensors() {
        observeSensorPowerUseCase().collect { newData ->
            // Actualización inmediata de la lista de texto/valores
            updateSensorList(newData)

            // Lógica de control para la gráfica (optimización)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastChartUpdate >= (userSamplingFrequency * 1000)) {
                updateChartData(_sensorList.value)
                lastChartUpdate = currentTime
            }

            //Lógica para el cálculo de energía en Joules
            // Si se está capturando y se pasó el intervalo elegido
            if (_isCapturing.value && (currentTime - lastEnergyUpdate >= (userSamplingFrequency * 1000))) {
                updateSensorEnergy(
                    sensorType = newData.type,
                    estimatedPowerMw = newData.estimatedPowerMw,
                    samplingFrequency = userSamplingFrequency
                )
                lastEnergyUpdate = currentTime
            }
        }
    }

    /**
     * Actualiza la lista de sensores de forma reactiva y eficiente.
     * Realiza un recorrido sobre la lista actual para:
     * 1. Identificar si el sensor entrante ya está registrado mediante una bandera (sensorExists).
     * 2. Actualizar los valores del sensor existente preservando la inmutabilidad.
     * 3. Si el sensor es nuevo (no se activó la bandera), se concatena al final de la lista.
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
            // Si el sensor aparece por primera vez, se agrega a la lista
            if (sensorExists) updatedList else updatedList + newData
        }
    }

    /**
     * Actualiza los datos de la gráfica utilizando un Mapa para optimizar las búsquedas.
     * 1. Convierte la lista actual en un Mapa indexado por tipo de sensor para acceso rápido.
     * 2. Calcula el tiempo transcurrido (elapsedSeconds) y genera un nuevo punto (P, t) para cada sensor activo.
     * 3. Actualiza o crea la serie de datos correspondiente, limitando el histórico a 25 puntos
     * para optimizar el uso de memoria y la fluidez de la interfaz.
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

                chartMap[sensor.type] =
                    if (existingChart != null) {
                        // Se actualiza la gráfica existente con el nuevo punto
                        existingChart.copy(
                            points = (existingChart.points + newPoint).takeLast(25)
                        )
                    } else {
                        // Crea una nueva gráfica desde cero
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
     * Calcula y acumula la energía consumida por un sensor específico durante la captura.
     * Utiliza la relación física E = P * t, donde la potencia se integra sobre el intervalo
     * de muestreo seleccionado por el usuario.
     *
     * @param sensorType Identificador del sensor (ej. Sensor.TYPE_ACCELEROMETER).
     * @param estimatedPowerMw Potencia actual calculada en miliwatts (mW).
     * @param samplingFrequency Intervalo de tiempo en segundos (1s, 3s, o 5s).
     */
    private fun updateSensorEnergy(sensorType: Int, estimatedPowerMw: Float, samplingFrequency: Int) {
        // Solo se acumula si el usuario ha iniciado una sesión de captura
        if(_isCapturing.value) {
            val currentEnergy = sensorEnergyMap[sensorType] ?: 0f
            val energyIncrement = calculateEnergyUseCase(estimatedPowerMw, samplingFrequency)

            sensorEnergyMap[sensorType] = currentEnergy + energyIncrement
        }
    }

    /**
     * Transforma el estado actual de los sensores y la energía acumulada en una lista
     * de resultados lista para ser persistida.
     *
     * @return Lista de [SensorResult] con los datos consolidados de la sesión.
     */
    private fun prepareSensorResults(): List<SensorResult> {
        val currentTimestamp = java.time.Instant.now().toString()

        return _sensorList.value.map { sensor ->
            SensorResult(
                sensorType = sensor.type,
                displayName = sensor.displayName,
                estimatedPowerMw = sensor.estimatedPowerMw,
                totalEnergyJ = sensorEnergyMap[sensor.type] ?: 0f,
                timestamp = currentTimestamp
            )
        }
    }

    /**
     * Inicia el monitoreo de sensores del dispositivo. Activa el flujo de recolección de
     * datos de los sensores utilizando corrutinas dentro del [viewModelScope].
     *
     * - Verifica si ya existe un proceso activo para evitar duplicidad.
     * - Inicializa el cronómetro de referencia para las gráficas.
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
     * Detiene inmediatamente el monitoreo de sensores y libera los recursos.
     * Cancela [monitoringJob] para detener la corrutina de observación,
     * reseteando el estado y liberando recursos para permitir un nuevo inicio.
     */
    fun stopMonitoring() {
        startTime = 0
        monitoringJob?.cancel()
        monitoringJob = null
    }

    /**
     * Reinicia el estado del monitoreo de sensores desde cero.
     * Detiene procesos actuales, limpia las listas de sensores y datos de gráficas,
     * y vuelve a iniciar la recolección de datos
     */
    fun restartMonitoring() {
        stopMonitoring()

        _sensorList.value = emptyList()
        _sensorChartData.value = emptyList()
        sensorEnergyMap.clear()
        lastChartUpdate = 0
        lastEnergyUpdate = 0
        startTime = System.currentTimeMillis()

        monitoringJob = viewModelScope.launch {
            observeSensors()
        }
    }

    /**
     * Inicia el proceso de captura de datos utilizando el caso de uso de dominio.
     * - Reinicia el monitoreo para sincronizar datos.
     * - Activa el estado de captura [_isCapturing] para la UI.
     * - Delega la gestión del tiempo al [startCaptureTimerUseCase].
     *
     * @param durationMinutes Duración total de la captura en minutos.
     * @param samplingFrequency Frecuencia de muestreo seleccionada por el usuario (1s, 3s, 5s).
     */
    fun startCapture(durationMinutes: Int, samplingFrequency: Int) {
        restartMonitoring()
        this.userSamplingFrequency = samplingFrequency
        _isCapturing.value = true

        captureJob?.cancel() // Control de seguridad que cancela cualquier captura previa

        // Recolectar el flujo del temporizador desde el dominio
        captureJob = viewModelScope.launch {
            startCaptureTimerUseCase(durationMinutes).collect { remainingSeconds ->
                _remainingTime.value = remainingSeconds

                if (remainingSeconds == 0) stopCapture()
            }
        }
    }

    /**
     * Detiene el proceso de captura y Cancela la suscripción al temporizador.
     *
     * Persiste los datos recolectados, genera un timestamp único para la sesión
     * y lanza la lógica de guardado en dominio.
     */
    fun stopCapture() {
        if(!_isCapturing.value) return // Evitar ejecuciones duplicadas
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))

        val results = prepareSensorResults()
        val duration = (_remainingTime.value / 60) // Duración original

        viewModelScope.launch {
            try {
                saveCaptureUseCase(
                    timestamp = timestamp,
                    durationMinutes = duration,
                    samplingFrequency = userSamplingFrequency,
                    sensorResults = results
                )
            } catch (e: Exception) {
                // Loguear error si algo falla en el guardado
                Log.e("CaptureViewModel", "Error al guardar captura", e)
            } finally {
                // Limpieza de estados tras el guardado
                _isCapturing.value = false
                _remainingTime.value = 0
                sensorEnergyMap.clear()
                captureJob?.cancel()

                // Reiniciar monitoreo normal
                userSamplingFrequency = 3
                restartMonitoring()
            }
        }
    }
}