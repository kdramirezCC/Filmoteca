package com.campusdigitalfp.filmotecav2.model
//carpeta y clase creadas de antes, es la plantilla para que los datos de la bd y la app tengan la misma estructura siempre
data class Film(
    var id: String? = "",//va a ser el identificador en firestore
    var imageResId: Int = 0,
    var imageUrl: String? = null, // ej5ev2 URL de la imagen subida desde la cámara
    var title: String? = null,
    var director: String? = null,
    var year: Int = 0,
    var genre: Int = 0,
    var format: Int = 0,
    var imdbUrl: String? = null,
    var comments: String? = null
) {
    override fun toString(): String {
        return title ?: "<Sin título>"
    }

    companion object {
        const val FORMAT_DVD = 0
        const val FORMAT_BLURAY = 1
        const val FORMAT_DIGITAL = 2
        const val GENRE_ACTION = 0
        const val GENRE_COMEDY = 1
        const val GENRE_DRAMA = 2
        const val GENRE_SCIFI = 3
        const val GENRE_HORROR = 4
        const val GENRE_ANIMATION = 5
    }
}