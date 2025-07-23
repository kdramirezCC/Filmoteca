package com.campusdigitalfp.filmotecav2.navigation


import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.filmotecav2.screens.AboutScreen
import com.campusdigitalfp.filmotecav2.screens.FilmDataScreen
import com.campusdigitalfp.filmotecav2.screens.FilmEditScreen
import com.campusdigitalfp.filmotecav2.screens.FilmListScreen
import com.campusdigitalfp.filmotecav2.screens.LoginScreen
import com.campusdigitalfp.filmotecav2.screens.RegisterScreen
import com.campusdigitalfp.filmotecav2.viewModel.AuthViewModel
import com.campusdigitalfp.filmotecav2.viewModel.FilmViewModel
import com.google.firebase.auth.FirebaseAuth

// Función que verifica si hay un usuario autenticado en Firebase Authentication.
// Retorna true si hay un usuario autenticado, false en caso contrario.
fun comprobarUsuario(): Boolean {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    return currentUser != null
}

// Función que gestiona la navegación dentro de la aplicación usando Jetpack Compose.
@Composable
fun Navigation(filmViewModel: FilmViewModel, authViewModel: AuthViewModel) {//meto el FilmViewModel ej3 2ev++++le pasamos el AuthviewModel ej42ev
    val navController = rememberNavController() // Controlador de navegación para gestionar cambios de pantalla

    // Define la pantalla de inicio dependiendo de si el usuario está autenticado o no.
    val startDestination =
        if (comprobarUsuario())
            "list"  // Si está autenticado, ir a la lista de hábitos
        else
            "login" // Si no está autenticado, ir a la pantalla de login

    // Configuración del NavHost para definir las rutas y sus pantallas correspondientes.
    NavHost(navController = navController, startDestination = startDestination) {

        // Ruta para la pantalla de login
        composable("login") {
            if (comprobarUsuario())
                FilmListScreen(
                    navController,
                    viewModel = filmViewModel,
                    authViewModel//ej42ev
                ) // Si el usuario está autenticado, redirigir a la lista de hábitos
            else
                LoginScreen(navController, authViewModel) // Si no, mostrar la pantalla de inicio de sesión
        }

        // Ruta para la pantalla de registro
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }

        // Ruta para la lista de peliculas (pantalla principal)
        composable("list") {
            if (comprobarUsuario())
                FilmListScreen(navController, filmViewModel, authViewModel)
            else
                LoginScreen(navController, authViewModel)
        }

        // Ruta para la pantalla de "Acerca de"
        composable("about") {
            if (comprobarUsuario())
                AboutScreen(navController, filmViewModel, authViewModel)
            else
                LoginScreen(navController, authViewModel)
        }

        //para la pantalla de filmData
        composable("data/{filmIndex}") { backStackEntry ->
        val filmIndex = backStackEntry.arguments?.getString("filmIndex") ?.toIntOrNull()
          filmIndex?.let {
                if (comprobarUsuario())
                    FilmDataScreen(navController, filmViewModel,authViewModel,it)
                else
                    LoginScreen(navController, authViewModel)
            }
        }

        // Ruta para editar una pelicula con un ID específico
        composable("edit/{filmIndex}") { backStackEntry ->
            val filmIndex = backStackEntry.arguments?.getString("filmIndex")?.toIntOrNull()

            filmIndex?.let {
                if (comprobarUsuario()) {
                    FilmEditScreen(navController, filmViewModel,authViewModel,it) // Permite editar si está autenticado
                } else {
                    LoginScreen(navController, authViewModel) // Si no, redirige al login
                }
            }
        }

    }
}



