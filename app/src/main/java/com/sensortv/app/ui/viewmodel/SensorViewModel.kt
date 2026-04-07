package com.sensortv.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sensortv.app.data.repository.BatteryRepository
import com.sensortv.app.data.repository.SensorRepository
import com.sensortv.app.data.model.BatteryData
import com.sensortv.app.ui.model.SensorChartData
import com.sensortv.app.ui.model.SensorChartPoint
import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar la lógica de presentación y estado de SensorTV 2.0.
 * - Se encarga de transformar los flujos de datos crudos (repositorio) en estados
 * observables por la UI.
 * - Controla el inicio/fin de la monitorización y los tiempos de captura de datos.
 *
 * @param  sensorRepository Repositorio de sensores y potencia.
 * @param  batteryRepository Repositorio de datos del estado de la batería.
 */
class SensorViewModel(
    private val sensorRepository: SensorRepository,
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

    // Variables de captura de datos
    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing

    private val _remainingTime = MutableStateFlow(0)
    val remainingTime: StateFlow<Int> = _remainingTime

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
     * - Sincroniza la actualización de la gráfica cada 3 segundos para optimizar el
     * rendimiento y reducir el consumo de recursos.
     */
    @OptIn(FlowPreview::class)
    private suspend fun observeSensors() {
        sensorRepository.observeSensors().collect { newData ->
            // Actualización inmediata de la lista de texto/valores
            updateSensorList(newData)

            // Lógica de control para la gráfica (optimización)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastChartUpdate >= 3000) {
                updateChartData(_sensorList.value)
                lastChartUpdate = currentTime
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
     * Cancela el [monitoringJob] para detener la corrutina de observación,
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
        lastChartUpdate = 0
        startTime = System.currentTimeMillis()

        monitoringJob = viewModelScope.launch {
            observeSensors()
        }
    }

    /**
     * Inicia el proceso de captura de datos por un tiempo determinado.
     * - Reinicia el monitoreo para sincronizar el inicio de los datos.
     * - Activa el estado de captura [_isCapturing] para la UI.
     * - Lanza un temporizador en segundo plano que resta los segundos restantes.
     *
     * @param durationMinutes Duración total de la captura en minutos.
     */
    fun startCapture(durationMinutes: Int) {
        restartMonitoring()

        val totalSeconds = durationMinutes * 60
        _remainingTime.value = totalSeconds
        _isCapturing.value = true

        captureJob?.cancel() // Control de seguridad que cancela cualquier captura previa

        captureJob = viewModelScope.launch {
            while (_remainingTime.value > 0) {
                delay(1000)
                _remainingTime.value -= 1
            }
            _isCapturing.value = false // Cerrar captura
        }
    }

    /**
     * Detiene manualmente el proceso de captura antes de que se agote el tiempo.
     * Cancela la tarea del temporizador y actualiza el estado de UI para
     * indicar que la captura ha finalizado.
     */
    fun stopCapture() {
        captureJob?.cancel()
        _isCapturing.value = false
    }
}