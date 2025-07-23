//Pnatalla creada en ej42ev

package com.campusdigitalfp.filmotecav2.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.campusdigitalfp.filmotecav2.viewModel.AuthViewModel
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.identity.Identity

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    // Variables de estado para almacenar el email, la contraseña y los mensajes de error
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }//circulo de que está cargando

    // Crear un alcance de corrutinas para manejar tareas asíncronas dentro del Composable
    val scope = rememberCoroutineScope()

// Definir el ActivityResultLauncher para manejar el resultado del inicio de sesión con Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult() // Define el contrato para recibir un resultado de una actividad
    ) { result ->
        // Ejecutar una corrutina para procesar el resultado de la autenticación
        scope.launch {
            // Llamar a la función del ViewModel que maneja la autenticación con Google y obtiene el resultado
            val success = viewModel.handleGoogleSignInResult(result.data)

            // Verificar si la autenticación fue exitosa
            if (success) {
                // Si la autenticación es exitosa, navegar a la pantalla principal (lista de hábitos)
                navController.navigate("list")
            } else {
                // Si hubo un error en la autenticación, mostrar un mensaje de error en la interfaz
                errorMessage = "Error al autenticar con Google"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título de la pantalla de inicio de sesión
        Text(text = "Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de entrada de email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de entrada de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para iniciar sesión con email y contraseña
        Button(
            onClick = {
                isLoading = true
                viewModel.loginUser(email, password) { success, error ->
                    isLoading = false
                    if (success) {
                        navController.navigate("list")
                    } else {
                        errorMessage = error
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Iniciar Sesión")
        }

        // Botón de inicio de sesión con Google (usando el nuevo método con cierre de sesión previo)
        Button(
            onClick = {
                isLoading = true // Indica que el proceso de autenticación está en curso

                // Cierra la sesión actual antes de iniciar el flujo de autenticación con Google
                viewModel.googleSignInClient.signOut().addOnCompleteListener {
                    // Una vez finalizado el cierre de sesión, lanza la pantalla de selección de cuenta de Google
                    googleSignInLauncher.launch(viewModel.googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth(), // Hace que el botón ocupe todo el ancho disponible
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary) // Define el color del botón
        ) {
            Text("Iniciar Sesión con Google") // Texto del botón
        }

        // Botón para iniciar sesión como invitado (anónimo)
        Button(
            onClick = {
                isLoading = true
                viewModel.signInAnonymously { success, error ->
                    isLoading = false
                    if (success) {
                        navController.navigate("list")
                    } else {
                        errorMessage = error
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
        ) {
            Text("Acceder como invitado")
        }

        // Indicador de carga mientras se realiza el proceso de autenticación
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        // Mostrar mensaje de error en caso de fallo en la autenticación
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para ir a la pantalla de registro
        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}
