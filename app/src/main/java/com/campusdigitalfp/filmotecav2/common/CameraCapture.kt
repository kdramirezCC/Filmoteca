package com.campusdigitalfp.filmotecav2.common
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.campusdigitalfp.filmotecav2.model.Image
import com.campusdigitalfp.filmotecav2.viewModel.FilmViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*



@Composable
fun CameraCapture(viewModel: FilmViewModel, filmId: String) {
    val context = LocalContext.current

    // Variable para almacenar la URI de la imagen capturada desde la cámara
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Variable para almacenar la URI de la imagen guardada localmente en la app
    var savedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Verifica si el permiso de la cámara ha sido concedido al usuario
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Lanzador para solicitar permisos de cámara y grabación de audio en tiempo de ejecución
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Verifica si el permiso de la cámara ha sido concedido
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
    }

    // Lanzador para abrir la cámara y capturar una imagen
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            // Si la captura es exitosa, guardar la imagen en la carpeta de la app
            saveImageToAppFolder(context, imageUri!!)?.let { galleryUri ->
                savedImageUri = galleryUri

                // Iniciar una corrutina en el hilo de entrada/salida para subir la imagen al servidor XAMPP
               // CoroutineScope(Dispatchers.IO).launch {
                   // savedImageUri?.let {
                        //servidorval serverResponse = uploadImageToServer(it, context)
                       //servidor Log.d("ServerResponse", "Respuesta del servidor: $serverResponse")
                  //  }
                //}

                // Guardar la referencia de la imagen en el ViewModel para su posterior uso en la app (Firestore)
                val imagen = Image(id = "", filmId = filmId, url = galleryUri.toString())
                viewModel.addImagen(imagen)
            }
        }
    }

    // Función para crear un archivo de imagen temporal antes de iniciar la captura
    val createImageFile: () -> Uri? = {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: IOException) {
            Log.e("createImageFile", "Error al crear el archivo de imagen", e)
            null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para capturar una imagen desde la cámara
        Button(
            onClick = {
                if (!hasCameraPermission) {
                    // Si no se tienen los permisos de cámara, solicitarlos al usuario
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                    )
                } else {
                    // Si ya se tienen permisos, crear un archivo temporal y lanzar la cámara
                    createImageFile()?.let { uri ->
                        imageUri = uri
                        cameraLauncher.launch(uri)
                    } ?: Log.e("CameraCapture", "No se pudo crear el archivo de imagen")
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(text = "Hacer foto La pelicula")
        }
    }
}