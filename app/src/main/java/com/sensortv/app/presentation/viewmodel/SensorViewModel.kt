package com.sensortv.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sensortv.app.data.repository.BatteryRepository
import com.sensortv.app.data.repository.SensorRepository
import com.sensortv.app.data.model.BatteryData
import com.sensortv.app.ui.model.SensorChartData
import com.sensortv.app.ui.model.SensorChartPoint
import com.sensortv.app.data.model.SensorData
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar y centralizar el estado de los sensores del dispositivo.
 * Transforma el flujo de datos crudos del repositorio en una lista de estado único
 * que la interfaz de usuario puede observar fácilmente.
 *
 * @property  sensorRepository Repositorio que proporciona el flujo de datos crudos.
 * @property  batteryRepository Repositorio que proporciona el flujo de datos de la batería.
 */
class SensorViewModel(
    private val sensorRepository: SensorRepository,
    private val batteryRepository: BatteryRepository
): ViewModel() {

    private val _sensorList = MutableStateFlow<List<SensorData>>(emptyList())
    val sensorList: StateFlow<List<SensorData>> = _sensorList

    private val _batteryState = MutableStateFlow<BatteryData?>(null)
    val batteryState: StateFlow<BatteryData?> = _batteryState

    // Datos en el tiempo
    private val _sensorChartData = MutableStateFlow<List<SensorChartData>>(emptyList())
    val sensorChartData: StateFlow<List<SensorChartData>> = _sensorChartData
    private var lastChartUpdate: Long = 0

    private val startTime = System.currentTimeMillis()

    init {
        observeSensors()
        observeBattery()
    }

    /**
     * Inicia la recolección de datos del repositorio dentro del alcance del ViewModel ([viewModelScope]).
     * La lógica actualiza un sensor existente en la lista si coincide el tipo,
     * o agrega el nuevo sensor si la lista está vacía o no lo contiene.
     */
    @OptIn(FlowPreview::class)
    private fun observeSensors() {
        viewModelScope.launch {
            sensorRepository.observeSensors().collect { newData ->
                updateSensorList(newData)

                // Solo se ejecuta si han pasado x milisegundos desde la última actualización
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastChartUpdate >= 3000) {
                    updateChartData(_sensorList.value)
                    lastChartUpdate = currentTime
                }
            }
        }
    }

    private fun observeBattery() {
        viewModelScope.launch {
            batteryRepository.observeBattery().collect { batteryData ->
                _batteryState.value = batteryData
            }
        }
    }

    private fun updateSensorList(newData: SensorData) {
        // Se toma el estado actual de la lista de forma segura
        _sensorList.update { currentList ->

            // Verificar si el sensor ya existe en la lista actual
            val sensorExists = currentList.any { it.type == newData.type }

            if (sensorExists) {
                //  Si existe, recorremos la lista y reemplazamos solo el sensor que cambió
                currentList.map { sensor ->
                    if (sensor.type == newData.type) {
                        sensor.copy(
                            values = newData.values,
                            frequencyHz = newData.frequencyHz,
                            available = true,
                            estimatedPowerMw = newData.estimatedPowerMw
                        )
                    } else sensor // Los demás sensores no cambian
                }
            } else {
                // Si es nuevo, se agrega al final de la lista
                currentList + newData
            }
        }
    }

    private fun updateChartData(sensors: List<SensorData>) {

        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000f

        _sensorChartData.update { currentCharts ->

            val updatedCharts = currentCharts.toMutableList()

            sensors.forEach { sensor ->

                val newPoint = SensorChartPoint(
                    timeStamp = elapsedSeconds,
                    powerMw = sensor.estimatedPowerMw
                )

                val index = updatedCharts.indexOfFirst { it.sensorType == sensor.type }

                if (index >= 0) {
                    val chart = updatedCharts[index]

                    val updatedPoints =
                        (chart.points + newPoint).takeLast(30)

                    updatedCharts[index] = chart.copy(points = updatedPoints)

                } else {
                    updatedCharts.add(
                        SensorChartData(
                            sensorType = sensor.type,
                            displayName = sensor.displayName,
                            points = listOf(newPoint)
                        )
                    )
                }
            }
            updatedCharts
        }
    }
}