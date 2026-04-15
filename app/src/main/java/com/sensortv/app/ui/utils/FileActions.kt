package com.sensortv.app.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/**
 * Lanza un selector de aplicaciones del sistema para exportar un archivo específico.
 *
 * - Válida la existencia del archivo y lo convierte en Uri segura mediante FileProvider.
 * - Lanza un Intent ACTION_SEND con el archivo adjunto (EXTRA_STREAM).
 * - Se utiliza un chooser para que el usuario seleccione la app preferida para exportar el archivo.
 *
 * @param context Contexto de la actividad desde donde se lanza el Intent.
 * @param filePath Ruta absoluta del archivo físico en el almacenamiento.
 */
fun shareCsvFile(context: Context, filePath: String) {
    val file = File(filePath)
    if (!file.exists()) {
        Toast.makeText(context, "El archivo no existe en el almacenamiento", Toast.LENGTH_SHORT).show()
        return
    }

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)   // Adjuntar el archivo al intent
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)     // Permiso temporal de lectura para la app de destino
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)             // Necesario si el contexto no es Activity
    }

    context.startActivity(Intent.createChooser(shareIntent, "exportar registro CSV mediante:"))
}

/**
 * Solicita al sistema abrir un archivo CSV con una aplicación compatible del dispositivo (ej. Excel, visores csv, etc).
 *
 * - Válida la existencia del archivo y lo convierte en Uri segura mediante FileProvider.
 * - Lanza un Intent ACTION_VIEW con el archivo como dato principal.
 * - Se utiliza un chooser para que el usuario seleccione la app preferida para abrir el archivo.
 *
 * @param context Contexto de la actividad desde donde se lanza el Intent.
 * @param filePath Ruta absoluta del archivo físico en el almacenamiento.
 */
fun viewCsvFile(context: Context, filePath: String) {
    val file = File(filePath)
    if (!file.exists()) return

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "text/csv")     // Archivo como dato principal del intent
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        val chooser = Intent.createChooser(viewIntent, "Abrir registro CSV con:")
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "No hay aplicaciones disponibles para abrir archivos CSV", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Lanza el selector de aplicaciones para exportar el paquete comprimido ZIP.
 * - Convierte el objeto File en una Uri segura a través del FileProvider.
 * - Lanza un Intent ACTION_SEND con el ZIP adjunto (EXTRA_STREAM).
 * - Otorga permisos de lectura temporales a la aplicación receptora.
 *
 * @param context Contexto necesario para obtener el FileProvider y lanzar la actividad.
 * @param file Objeto [File] que representa el archivo ZIP en la caché.
 */
fun shareZip(context: Context, file: File) {

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Exportar registros")
    )
}
