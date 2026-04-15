package com.sensortv.app.data.datasource

import java.io.File

/**
 * Interfaz que define el contrato para la compresión de archivos.
 */
interface FileCompressor {

    /**
     * Comprime una lista de archivos en un archivo ZIP.
     *
     * @param files Lista de objetos [File] físicos que se desean agrupar.
     * @param zipName El nombre que tendrá el paquete final ZIP.
     * @return El objeto [File] que representa al archivo ZIP ya creado.
     */
    suspend fun zipFiles(files: List<File>, zipName: String): File
}