package com.campusdigitalfp.filmotecav2.model

/**
 * Modelo de datos para almacenar información de imágenes en Firestore.
 * EJ5EV2 - Se añade para gestionar imágenes de películas.
 */
data class Image(
    var id: String = "", // Identificador único en Firestore
    var filmId: String = "", // ID de la película asociada
    var url: String = "" // URL de la imagen subida
)
