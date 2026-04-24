package com.sensortv.app.data.datasource

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Implementación de [FileCompressor]. Utiliza los flujos de salida de Java (IO) para realizar
 * la compresión de archivos de forma eficiente en segundo plano.
 *
 * @param context Contexto necesario para acceder a las carpetas del sistema (cache).
 */
class AndroidFileCompressor(
    private val context: Context
) : FileCompressor {

    /**
     * Toma una lista de archivos existentes y los agrupa en un nuevo archivo .zip
     * almacenado en el directorio de caché interno (context.cacheDir).
     *
     * - Utiliza [Dispatchers.IO] para asegurar que la UI no se bloquee.
     * - Filtra automáticamente archivos que no existan.
     * - Utiliza [ZipOutputStream] como flujo de salida para construir el archivo ZIP.
     * - Utiliza [FileInputStream] como flujo de entrada para leer cada archivo origen (CSV).
     *
     * @param files Lista de objetos [File] físicos que se desean comprimir.
     * @param zipName El nombre que tendrá el paquete final ZIP.
     * @return El objeto [File] que representa al archivo ZIP ya creado.
     */
    override suspend fun zipFiles(files: List<File>, zipName: String): File = withContext(
        Dispatchers.IO) {

        val zipFile = File(context.cacheDir, zipName)

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos -> //ZipOutputStream

            files.forEach { file ->
                if (!file.exists()) return@forEach

                FileInputStream(file).use { fis -> // FileInputStream
                    val entry = ZipEntry(file.name)

                    zos.putNextEntry(entry) // Agrega entrada para nuevo ZIP
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }
        zipFile
    }
}