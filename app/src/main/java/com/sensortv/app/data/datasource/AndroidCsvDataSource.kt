package com.sensortv.app.data.datasource

import android.content.Context
import android.util.Log
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
     * Esta operación se ejecuta en un hilo de I/O utilizando [Dispatchers.IO] para evitar
     * bloquear el hilo principal.
     *
     * @param fileName Nombre del archivo con extensión (ej: "mediciones.csv").
     * @param content Lista de líneas de texto (String) a escribir.
     * @return El objeto [File] que representa el archivo creado.
     */
    override suspend fun writeCsv(fileName: String, content: List<String>): File = withContext(
        Dispatchers.IO) {
        // Ruta de carpeta propia: /Android/storage/emulated/0/Android/data/com.sensortv.app/files/captures/
        // context.getExternalFilesDir(null) apunta a: /Android/storage/emulated/0/Android/data/com.sensortv.app/files/
        val directory = File(context.getExternalFilesDir(null), "captures")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        //Crear la referencia al archivo dentro de la carpeta
        val file = File(directory, fileName)

        Log.e("CSV_PATH", "Archivo guardado en: ${file.absolutePath}")

        // Escribir el contenido usando un bufferedWriter que abre un canal de escritura eficiente
        file.bufferedWriter().use { writer ->
            content.forEach { line ->
                writer.write(line)
                writer.newLine()
            }
        }
        file
    }

    /**
     * Elimina un archivo CSV previamente almacenado en el sistema de archivos del dispositivo.
     * Verifica la existencia del archivo antes de intentar eliminarlo.
     *
     * @param filePath Ruta absoluta del archivo a eliminar (ej: /storage/emulated/0/.../archivo.csv).
     * @return `true` si el archivo fue eliminado correctamente, `false` si el archivo no existe
     * o no pudo ser eliminado.
     */
    override suspend fun deleteCsvFile(filePath: String): Boolean = withContext(Dispatchers.IO){
        val file = File(filePath)
        if (file.exists()) file.delete()
        else false
    }
}