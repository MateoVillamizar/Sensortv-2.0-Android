package com.sensortv.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.sensortv.app.data.datasource.AndroidBatteryDataSource
import com.sensortv.app.data.datasource.AndroidCsvDataSource
import com.sensortv.app.data.datasource.AndroidSensorDataSource
import com.sensortv.app.data.datasource.AppDatabase
import com.sensortv.app.data.repository.BatteryRepositoryImpl
import com.sensortv.app.data.repository.CaptureRepositoryImpl
import com.sensortv.app.data.repository.SensorRepositoryImpl
import com.sensortv.app.domain.CalculateEnergyUseCase
import com.sensortv.app.domain.ObserveSensorPowerUseCase
import com.sensortv.app.domain.SaveCaptureUseCase
import com.sensortv.app.domain.StartCaptureTimerUseCase

/**
 * Fábrica personalizada para la creación del [SensorViewModel].
 * Android no puede instanciar automáticamente ViewModels que requieran parámetros en su constructor.
 * 1. Inicializa los DataSources con el contexto del sistema.
 * 2. Construye los Repositorios inyectando sus respectivas fuentes de datos.
 * 3. Instancia los Casos de Uso de la capa Domain.
 *
 * @param context Contexto necesario para que los DataSources accedan a los servicios de hardware.
 * @return [SensorViewModelFactory] Instancia completa de [SensorViewModel] lista para uso en UI.
 */
class SensorViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    // Suprime advertencia de cast inseguro (as T); pero sabemos que es correcto por la lógica del factory.
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Se verifica que se esté solicitando el ViewModel (SensorViewModel) correcto
        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {

            // Construcción de dependencias
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "sensor_tv_2.0_db").build()
            val captureDao = db.captureDao()

            val sensorDataSource = AndroidSensorDataSource(context)
            val batteryDataSource = AndroidBatteryDataSource(context)
            val csvDataSource = AndroidCsvDataSource(context)

            val sensorRepo = SensorRepositoryImpl(sensorDataSource)
            val batteryRepo = BatteryRepositoryImpl(batteryDataSource)
            val captureRepo = CaptureRepositoryImpl(captureDao)

            val observeSensorPowerUseCase = ObserveSensorPowerUseCase(sensorRepo, batteryRepo)
            val startCaptureTimerUseCase = StartCaptureTimerUseCase()
            val calculateEnergyUseCase = CalculateEnergyUseCase()
            val saveCaptureUseCase = SaveCaptureUseCase(csvDataSource, captureRepo)

            return SensorViewModel(
                observeSensorPowerUseCase,
                startCaptureTimerUseCase,
                calculateEnergyUseCase,
                saveCaptureUseCase,
                batteryRepo
            ) as T
        }
        throw IllegalArgumentException("Error: Clase ViewModel no compatible con esta Factory")
    }
}