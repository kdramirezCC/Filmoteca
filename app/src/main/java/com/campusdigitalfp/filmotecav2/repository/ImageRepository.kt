package com.campusdigitalfp.filmotecav2.repository
import android.util.Log
import com.campusdigitalfp.filmotecav2.model.Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ImageRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Obtiene la referencia a la subcolección "imagenes" dentro del usuario autenticado
    private fun getUserImagesCollection() =
        auth.currentUser?.let { user ->
            db.collection("usuarios").document(user.uid).collection("imagenes")
        }

    /**
     * Agrega una imagen a la subcolección del usuario autenticado.
     */
    suspend fun addImagen(imagen: Image) {
        getUserImagesCollection()?.add(imagen)?.await()
    }

    /**
     * Obtiene todas las imágenes almacenadas en la subcolección del usuario autenticado.
     */
    suspend fun getImagenes(): List<Image> {
        return try {
            getUserImagesCollection()?.get()?.await()?.documents?.mapNotNull {
                it.toObject(Image::class.java)?.copy(id = it.id)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Elimina una imagen específica del usuario autenticado.
     */
    suspend fun deleteImagen(imagenId: String) {
        getUserImagesCollection()?.document(imagenId)?.delete()?.await()
    }

    /**
     * Elimina todas las imágenes relacionadas con un hábito específico en la colección del usuario.
     */
    suspend fun deleteImagenesFilm(filmId: String) {
        try {
            val snapshot = getUserImagesCollection()?.whereEqualTo("pelicula", filmId)?.get()?.await()
            snapshot?.documents?.forEach { document ->
                getUserImagesCollection()?.document(document.id)?.delete()?.await()
            }
            Log.i("FS_info", "Todas las imágenes de la película $filmId han sido eliminadas.")
        } catch (e: Exception) {
            Log.e("FS_error", "Error eliminando imágenes: ${e.message}")
        }
    }

    /**
     * Escucha cambios en la subcolección de imágenes del usuario autenticado en Firestore.
     */
    fun listenToImagenesUpdates(onUpdate: (List<Image>) -> Unit) {
        getUserImagesCollection()?.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("FS_error", "Error al obtener imágenes: ${exception.message}")
                return@addSnapshotListener
            }

            val imagenes = snapshot?.documents?.mapNotNull {
                it.toObject(Image::class.java)?.copy(id = it.id)
            } ?: emptyList()
            onUpdate(imagenes)
        }
    }
}