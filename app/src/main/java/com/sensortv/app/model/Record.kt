package com.sensortv.app.model

// Representa un registro guardado de captura (Cambiar o Verificar)
data class Record(
    val id: Int,
    val fileName: String,
    val date: String,
)