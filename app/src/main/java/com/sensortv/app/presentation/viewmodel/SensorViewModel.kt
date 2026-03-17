package com.sensortv.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sensortv.app.data.repository.BatteryRepository
import com.sensortv.app.data.repository.SensorRepository
import com.sensortv.app.model.BatteryData
import com.sensortv.app.model.SensorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar y centralizar el estado de los sensores del dispositivo.
 * Transforma el flujo de datos crudos del repositorio en una lista de estado único
 * que la interfaz de usuario puede observar fácilmente.
 *
 * @property repository Repositorio que provee acceso a la fuente de datos de los sensores.
 */
class SensorViewModel(
    private val sensorRepository: SensorRepository,
    private val batteryRepository: BatteryRepository
): ViewModel() {

    private val _sensorList = MutableStateFlow<List<SensorData>>(emptyList())
    val sensorList: StateFlow<List<SensorData>> = _sensorList

    private val _batteryState = MutableStateFlow<BatteryData?>(null)
    val batteryState: StateFlow<BatteryData?> = _batteryState

    init {
        observeSensors()
        observeBattery()
    }

    /**
     * Inicia la recolección de datos del repositorio dentro del alcance del ViewModel ([viewModelScope]).
     * La lógica actualiza un sensor existente en la lista si coincide el tipo,
     * o agrega el nuevo sensor si la lista está vacía o no lo contiene.
     */
    private fun observeSensors() {
        viewModelScope.launch {
            sensorRepository.observeSensors().collect { newData ->
                val currentList = _sensorList.value

                // Verificar si el sensor ya existe en la lista actual
                val sensorExists = currentList.any { it.type == newData.type }

                _sensorList.value = if (sensorExists) {
                    // Si existe, se usa map para encontrarlo y actualizar sus valores
                    currentList.map { sensor ->
                        if (sensor.type == newData.type) {
                            sensor.copy(
                                values = newData.values,
                                frequencyHz = newData.frequencyHz,
                                available = true
                            )
                        } else sensor
                    }
                } else {
                    // Si no existe, se añade a la lista actual
                    currentList + newData
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
}