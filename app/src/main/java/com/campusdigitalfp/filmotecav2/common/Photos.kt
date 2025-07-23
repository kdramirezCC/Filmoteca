package com.campusdigitalfp.filmotecav2.common

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Guarda una imagen en la carpeta interna de la aplicación.
 * EJ5EV2 - Función para manejar imágenes capturadas desde la cámara.
 */
fun saveImageToAppFolder(context: Context, imageUri: Uri): Uri? {
    //genera un rimeStamp para darle un nombre único a la imagen
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val directory = File(context.filesDir, "FilmotecaImages")//tengo un directorio    y un directorio hijo donde se van a guardar las fotos

    //verificamos si el direeeectorio existe y sino lo crea
    if (!directory.exists() && !directory.mkdirs()) {
        Log.e("saveImageToAppFolder", "No se pudo crear el directorio")
        return null
    }

    val file = File(directory, "IMG_$timeStamp.jpg")

    return try {
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        Uri.fromFile(file)
    } catch (e: IOException) {
        Log.e("saveImageToAppFolder", "Error al guardar la imagen", e)
        null
    }
}

