package com.campusdigitalfp.filmotecav2.repository

import android.util.Log
import com.campusdigitalfp.filmotecav2.model.Film
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
//ejercicio3 2ev


/**
 * FilmRepository maneja todas las operaciones relacionadas con las películas en Firestore.
 * Este repositorio actúa como una capa intermedia entre la base de datos y el ViewModel.
 */
class FilmRepository {

    // Instancia de Firestore y autenticación
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Obtiene la referencia a la colección de películas del usuario actual
    private fun getUserFilmsCollection() =
        auth.currentUser?.let { user ->
            db.collection("usuarios").document(user.uid).collection("films")
        }

    suspend fun addFilm(film: Film): String? {
        val collection = getUserFilmsCollection() ?: return null
        val newDocRef = collection.document()

        return try {
            val filmWithId = film.copy(id = newDocRef.id)
            newDocRef.set(filmWithId).await()
            newDocRef.id
        } catch (e: Exception) {
            null
        }
    }
    /**
     * Recupera todas las películas almacenadas en Firestore y los devuelve como una lista.
     * Utiliza await() para esperar a que Firestore devuelva los datos antes de continuar.
     * Si ocurre un error, devuelve una lista vacía en lugar de lanzar una excepción.
     */
    suspend fun getFilms(): List<Film> {
        return try {
            getUserFilmsCollection()?.get()?.await()?.documents?.mapNotNull {// Obtiene los documentos de Firestore
                it.toObject(Film::class.java)?.copy(id = it.id)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()// En caso de error, devuelve una lista vacía para evitar fallos en la UI
        }
    }

    suspend fun updateFilm(film: Film) {
        getUserFilmsCollection()?.document(film.id.toString())?.set(film)?.await()
    }

    suspend fun deleteFilm(filmId: String) {
        getUserFilmsCollection()?.document(filmId)?.delete()?.await()
    }

    fun listenToFilmsUpdates(onUpdate: (List<Film>) -> Unit) {
        getUserFilmsCollection()?.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("FS_error", "Error al obtener películas: ${exception.message}")
                return@addSnapshotListener
            }

            val films = snapshot?.documents?.mapNotNull {
                it.toObject(Film::class.java)?.copy(id = it.id)
            } ?: emptyList()
            onUpdate(films)
        }
    }

    suspend fun addMultipleFilms(films: List<Film>) {
        val batch = db.batch()
        getUserFilmsCollection()?.let { collection ->
            films.forEach { film ->
                val newDocRef = collection.document()
                batch.set(newDocRef, film.copy(id = newDocRef.id))
            }
        }
        batch.commit().await()
    }

    suspend fun deleteMultipleFilms(films: List<Film>) {
        val batch = db.batch()
        getUserFilmsCollection()?.let { collection ->
            films.forEach { film ->
                batch.delete(collection.document(film.id.toString()))
            }
        }
        try {
            batch.commit().await() // Ejecuta la eliminación de todos las peliculas en una sola operación
            Log.i("HS_info", "Películas eliminadas correctamente de Firestore")
        } catch (e: Exception) {
            Log.e("HS_error", "Error al eliminar peliculas: ${e.message}")
        }
    }
}

