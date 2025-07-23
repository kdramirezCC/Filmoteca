//clase creada para el ejercicio 4 2ev
package com.campusdigitalfp.filmotecav2.viewModel

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    //metodo para registrar usuarios ej4/3.2-2ev
    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        // Inicia el proceso de creación de un usuario con email y contraseña en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task -> // Se añade un listener para detectar cuando el proceso ha finalizado
                if (task.isSuccessful) {
                    // Si el registro es exitoso, se llama a la función de resultado con 'true' y sin mensaje de error
                    onResult(true, null)
                } else {
                    // Si ocurre un error, se traduce el mensaje de error de Firebase a un formato más comprensible
                    val errorMessage = translateErrorFirebase(task.exception)
                    // Se llama a la función de resultado con 'false' y se pasa el mensaje de error
                    onResult(false, errorMessage)
                }
            }
    }



        //función de inicio de sesión ej4/3.3-2ev
    fun loginUser(email: String?, password: String?, onResult: (Boolean, String?) -> Unit) {
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            onResult(false, "El correo y la contraseña no pueden estar vacíos.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = translateErrorFirebase(task.exception)
                    onResult(false, errorMessage)
                }
            }
    }

    //inicio de sesión anonima ej4/3.4-2ev
    fun signInAnonymously(onResult: (Boolean, String?) -> Unit) {
        // Inicia sesión de manera anónima en Firebase Authentication
        auth.signInAnonymously()
            .addOnCompleteListener { task -> // Se añade un listener para manejar el resultado de la operación
                if (task.isSuccessful) {
                    // Si el inicio de sesión es exitoso, se llama al callback con 'true' y sin mensaje de error
                    onResult(true, null)
                } else {
                    // Si ocurre un error, se obtiene un mensaje más comprensible usando translateErrorFirebase
                    val errorMessage = translateErrorFirebase(task.exception)
                    // Se llama al callback con 'false' y se pasa el mensaje de error
                    onResult(false, errorMessage)
                }
            }
    }
    //función para cerrar sesión
    fun logout() {
        auth.signOut()
    }

    // Configuración del cliente de Google Sign-In
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(
        application,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("161664252137-th3l4nkcsclfbrou01rravto05ltjsk5.apps.googleusercontent.com") // Reemplaza con tu Client ID de Firebase
            .requestEmail() // Solicita acceso al correo del usuario para identificarlo
            .build()
    )

    //Inicia el flujo de autenticación con Google. es la pantalla para elegir la cuenta
    fun signInWithGoogle(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent // Obtiene el intent para iniciar sesión
        launcher.launch(signInIntent) // Lanza el intent de autenticación con Google
    }

    //Maneja el resultado del inicio de sesión con Google y lo autentica con Firebase.
    suspend fun handleGoogleSignInResult(data: Intent?): Boolean {
        return try {
            // Obtener la cuenta de Google desde el Intent de respuesta
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java) // Obtiene la cuenta autenticada

            val googleIdToken = account?.idToken // Obtiene el ID Token de la cuenta
            if (googleIdToken != null) {
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null) // Crea la credencial de Firebase

                // Autenticación en Firebase con la credencial de Google
                auth.signInWithCredential(credential).await()
                true // Retorna `true` si la autenticación fue exitosa
            } else {
                false // No se obtuvo token, retorno `false`
            }
        } catch (e: ApiException) {
            false // Captura cualquier error en la autenticación con Google y retorna `false`
        }
    }


    private fun translateErrorFirebase(exception: Exception?): String {
        // Se verifica si la excepción es de tipo FirebaseAuthException y se obtiene su código de error
        return when ((exception as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo electrónico no tiene un formato válido."
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta o el usuario no tiene contraseña."
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo."
            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada."
            "ERROR_TOO_MANY_REQUESTS" -> "Has realizado demasiados intentos, intenta más tarde."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo ya está registrado en otra cuenta."
            "ERROR_NETWORK_REQUEST_FAILED" -> "No se pudo conectar a la red. Verifica tu conexión."
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil. Usa una más segura."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Este correo ya está registrado con otro método de inicio de sesión."
            else -> "Ocurrió un error desconocido. Intenta nuevamente."
        }
    }



}