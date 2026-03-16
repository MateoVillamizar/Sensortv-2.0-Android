package com.sensortv.app.model

data class SensorMonitorInfo(
    val name: String,
    val hardware: String,
    val baseCurrentMa: Float,
    val currentPowerMw: Float
)