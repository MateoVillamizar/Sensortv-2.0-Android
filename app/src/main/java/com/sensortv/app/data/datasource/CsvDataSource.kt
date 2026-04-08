package com.sensortv.app.data.datasource

import java.io.File

/**
 * Interfaz que define las operaciones de escritura de archivos CSV.
 */
interface CsvDataSource {
    /**
     * Crea un archivo CSV a partir de una lista de líneas de texto.
     * @param fileName Nombre del archivo (incluyendo extensión).
     * @param content Lista de strings, donde cada string es una fila del CSV.
     * @return [File] el objeto del archivo creado.
     */
    suspend fun writeCsv(fileName: String, content: List<String>): File
}