package com.sensortv.app.ui.model

// Representa un registro guardado de captura (Cambiar o Verificar)
data class Record(
    val id: Int,
    val fileName: String,
    val date: String,
)