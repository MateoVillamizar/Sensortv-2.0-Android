package com.sensortv.app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sensortv.app.data.datasource.AndroidBatteryDataSource
import com.sensortv.app.data.datasource.AndroidSensorDataSource
import com.sensortv.app.data.repository.BatteryRepositoryImpl
import com.sensortv.app.data.repository.SensorRepository
import com.sensortv.app.data.repository.SensorRepositoryImpl

/**
 * Fábrica para crear instancias de [SensorViewModel].
 * Es necesaria porque [SensorViewModel] requiere un [SensorRepository] en su constructor,
 * y el sistema de Android por defecto no sabe cómo proveer dependencias externas.
 *
 * @param context El contexto de la aplicación para inicializar [AndroidSensorDataSource].
 */
class SensorViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {

            // Construcción manual de dependencias
            val sensorDataSource = AndroidSensorDataSource(context)
            val batteryDataSource = AndroidBatteryDataSource(context)

            val sensorRepo = SensorRepositoryImpl(sensorDataSource, batteryDataSource)
            val batteryRepo = BatteryRepositoryImpl(batteryDataSource)

            return SensorViewModel(sensorRepo, batteryRepo) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}