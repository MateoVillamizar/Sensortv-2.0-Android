package com.sensortv.app.data.datasource

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Implementación de [CsvDataSource] que utiliza la API de archivos de Android.
 *
 * @param context Contexto necesario para acceder al almacenamiento de archivos de la aplicación.
 */
class AndroidCsvDataSource(
    private val context: Context
): CsvDataSource {

    /**
     * Escribe una lista de strings en un archivo físico en el almacenamiento local del dispositivo.
     * Esta operación se ejecuta en un hilo de I/O utilizando [Dispatchers.IO] para evitar
     * bloquear el hilo principal.
     *
     * - El contenido se normaliza eliminando acentos y convirtiendo a mayúsculas.
     *
     * @param fileName Nombre del archivo con extensión (ej: "mediciones.csv").
     * @param content Lista de líneas de texto (String) a escribir.
     * @return El objeto [File] que representa el archivo creado.
     */
    override suspend fun writeCsv(fileName: String, content: List<String>): File = withContext(
        Dispatchers.IO) {
        // context.getExternalFilesDir(null) apunta a: /Android/storage/emulated/0/Android/data/com.sensortv.app/files/
        val directory = File(context.getExternalFilesDir(null), "captures")

        if (!directory.exists()) directory.mkdirs()

        //Crear la referencia al archivo dentro de la carpeta
        val file = File(directory, fileName)

        Log.e("CSV_PATH", "Archivo guardado en: ${file.absolutePath}")

        /**
         * Escribe el contenido en el archivo usando UTF-8 (Charsets.UTF_8 explícitamente).
         * Cada línea se normaliza (sin acentos y convirtiendo a mayúsculas).
         * El bloque `use` asegura que el writer se cierre automáticamente.
         */
        file.outputStream().bufferedWriter(Charsets.UTF_8).use { writer ->
            content.forEach { line ->
                val normalizedLine = java.text.Normalizer
                    .normalize(line, java.text.Normalizer.Form.NFD) // descompone caracteres acentuados de línea
                    .replace("\\p{Mn}+".toRegex(), "")  // \p{Mn} ->  marcas diacríticas (acentos, tildes, etc.)
                    .uppercase()

                writer.write(normalizedLine)
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