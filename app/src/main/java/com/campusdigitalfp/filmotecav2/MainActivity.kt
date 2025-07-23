package com.campusdigitalfp.filmotecav2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusdigitalfp.filmotecav2.navigation.Navigation
import com.campusdigitalfp.filmotecav2.ui.theme.FilmotecaV2Theme
import com.campusdigitalfp.filmotecav2.viewModel.AuthViewModel
import com.campusdigitalfp.filmotecav2.viewModel.FilmViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //instancio cloud firestore ej3 2ev
        val db = FirebaseFirestore.getInstance() // Inicializa Firestore
        //ej 3 2ev
        // Configura Firestore sin almacenamiento en caché persistente.
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                MemoryCacheSettings.newBuilder().build() // Usa solo memoria RAM, sin guardar datos en disco.
            )
            .build()

        db.firestoreSettings = settings // Aplica la configuración a Firestore.
        enableEdgeToEdge()
        setContent {
            FilmotecaV2Theme {
                //ej3 2ev sequita el navigation() y se añade este codigo
                // Crea una instancia de `HabitViewModel` usando `viewModel()`.
                val viewModel: FilmViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel ()

                // Inicia la navegación en la aplicación y pasa el `viewModel`.
                Navigation(viewModel, authViewModel)//sale error al principio porque hay que cambiar la clase navigation
            }
        }
    }
}
