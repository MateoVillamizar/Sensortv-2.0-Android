package com.sensortv.app.data.datasource

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Implementación de [CsvDataSource] que utiliza la API de archivos de Android.
 *
 * @param context Contexto necesario para obtener el servicio FILE_SERVICE.
 */
class AndroidCsvDataSource(
    private val context: Context
): CsvDataSource {

    /**
     * Escribe una lista de strings en un archivo físico en el almacenamiento local del dispositivo.
     *
     * @param fileName Nombre del archivo con extensión (ej: "mediciones.csv").
     * @param content Lista de líneas de texto (String) a escribir.
     * @return El objeto [File] que representa el archivo creado.
     */
    // Dispatchers.IO es hilo especial diseñado para tareas de I/O (Input/Output)
    override suspend fun writeCsv(fileName: String, content: List<String>): File = withContext(
        Dispatchers.IO) {
        // Ruta de carpeta propia: /Android/data/com.sensortv.app/files/captures
        // context.getExternalFilesDir(null) apunta a: /Android/data/com.sensortv.app/files/
        val directory = File(context.getExternalFilesDir(null), "captures")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        //Crear la referencia al archivo dentro de la carpeta
        val file = File(directory, fileName)

        // Escribir el contenido usando un bufferedWriter que abre un canal de escritura eficiente
        file.bufferedWriter().use { writer ->
            content.forEach { line ->
                writer.write(line)
                writer.newLine()
            }
        }
        file
    }
}