package com.campusdigitalfp.filmotecav2.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.filmotecav2.R
import com.campusdigitalfp.filmotecav2.model.Film
import com.campusdigitalfp.filmotecav2.model.Image
import com.campusdigitalfp.filmotecav2.repository.FilmRepository
import com.campusdigitalfp.filmotecav2.repository.ImageRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica de la aplicación relacionada con los hábitos.
 * Se comunica con el repositorio (`HabitRepository`) para realizar operaciones en Firestore.
 */
class FilmViewModel : ViewModel() {

    // Instancia del repositorio que maneja las operaciones de Firestore.
    private val repository = FilmRepository()
    private val imageRepository = ImageRepository()
    private val auth = FirebaseAuth.getInstance()

    // `_habits` almacena la lista de hábitos de forma interna y permite su modificación.
    private val _films = MutableStateFlow<List<Film>>(emptyList())

    // `habits` expone los hábitos a la UI sin permitir modificaciones directas.
    val films: StateFlow<List<Film>> get() = _films

    private val _imagenes = MutableStateFlow<List<Image>>(emptyList())
    val imagenes: StateFlow<List<Image>> get() = _imagenes

    init {
        listenToFilms()
        observeUserChanges()
        listenToImagenes()
        observeUserChangesImagenes()
    }

    private fun observeUserChanges() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                loadFilms()//Cargar hábitos del nuevo usuario
            } else {
                _films.value = emptyList() //Vaciar lista si no hay usuario autenticado
            }
        }
    }

    private fun observeUserChangesImagenes() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                loadImagenes()//Cargar hábitos del nuevo usuario
            } else {
                _imagenes.value = emptyList()//Vaciar lista si no hay usuario autenticado
            }
        }
    }

    private fun loadFilms() {
        viewModelScope.launch {
            _films.value = repository.getFilms()
        }
    }

    private fun loadImagenes() {
        viewModelScope.launch {
            _imagenes.value = imageRepository.getImagenes()
        }
    }
    /**
     * Escucha los cambios en Firestore en tiempo real.
     * Cuando se detecta un cambio en la colección de hábitos, la UI se actualiza automáticamente.
     */
    private fun listenToFilms() {
        repository.listenToFilmsUpdates { updatedFilms ->
            _films.value = updatedFilms
        }
    }

    /**
     * Escucha los cambios en Firestore en tiempo real.
     * Cuando se detecta un cambio en la colección de hábitos, la UI se actualiza automáticamente.
     */
    private fun listenToImagenes() {
        imageRepository.listenToImagenesUpdates { updatedImagenes ->
            _imagenes.value = updatedImagenes
        }
    }

    /**
     * Recupera la lista de hábitos desde Firestore y actualiza `_habits`.
     * Se ejecuta dentro de `viewModelScope.launch` para evitar bloquear la UI.
     */
    private fun fetchFilms() {
        viewModelScope.launch {
            _films.value = repository.getFilms()
        }
    }

    /**
     * Agrega un nuevo hábito a Firestore y actualiza la lista de hábitos.
     */
    fun addFilm(film: Film) {
        viewModelScope.launch {
            repository.addFilm(film)
            fetchFilms()// Recarga la lista después de añadir un nuevo hábito.
        }
    }

    /**
     * Actualiza un hábito en Firestore y recarga la lista de hábitos.
     */
    fun updateFilm(film: Film) {
        viewModelScope.launch {
            repository.updateFilm(film)
            fetchFilms()
        }
    }

    /**
     * Elimina un hábito de Firestore según su ID y actualiza la lista.
     */
    fun deleteFilm(filmId: String) {
        viewModelScope.launch {
            imageRepository.deleteImagenesFilm(filmId)
            repository.deleteFilm(filmId)
            fetchFilms()
        }
    }

    /**
     * Agrega una lista de 10 hábitos de ejemplo a Firestore.
     * Se usa para pruebas o para pre-cargar la base de datos con hábitos iniciales.
     */
    fun addExampleFilms() {
        val films = listOf(
            Film(title = "Harry Potter y la piedra filosofal", director = "Chris Columbus",
                imageResId = R.drawable.harry_potter_y_la_piedra_filosofal,
                comments = "Una aventura mágica en Hogwarts.",
                format = Film.FORMAT_DVD,
                genre = Film.GENRE_ACTION,
                imdbUrl = "http://www.imdb.com/title/tt0241527",
                year = 2001),
            Film(title = "Regreso al futuro", director = "Robert Zemeckis",
                imageResId = R.drawable.regreso_al_futuro,
                comments = "",
                format = Film.FORMAT_DIGITAL,
                genre = Film.GENRE_SCIFI,
                imdbUrl = "http://www.imdb.com/title/tt0088763",
                year = 1985),
            Film(title = "El rey león", director = "Roger Allers, Rob Minkoff",
                imageResId = R.drawable.el_rey_leon,
                comments = "Una historia de crecimiento y responsabilidad.",
                format = Film.FORMAT_BLURAY,
                genre = Film.GENRE_ACTION,
                imdbUrl = "http://www.imdb.com/title/tt0110357",
                year = 1994)
        )

        viewModelScope.launch {
            repository.addMultipleFilms(films)
        }
    }

    /**
     * Elimina múltiples hábitos seleccionados en una sola operación en Firestore.
     * Luego, actualiza la lista de hábitos en la UI.
     */
    fun deleteSelectedFilms(selectedFilms: List<Film>) {
        viewModelScope.launch {
            repository.deleteMultipleFilms(selectedFilms)
            fetchFilms()// Recarga la lista tras la eliminación.

        }
    }

    /**
     * Recupera la lista de hábitos desde Firestore y actualiza `_habits`.
     * Se ejecuta dentro de `viewModelScope.launch` para evitar bloquear la UI.
     */
    private fun fetchImagenes() {
        viewModelScope.launch {
            _imagenes.value = imageRepository.getImagenes()
        }
    }


    /**
     * Agrega un nuevo hábito a Firestore y actualiza la lista de hábitos.
     */
    fun addImagen(imagen: Image) {
        viewModelScope.launch {
            imageRepository.addImagen(imagen)
            fetchImagenes()// Recarga la lista después de añadir un nuevo hábito.
        }
    }

    /**
     * Elimina un hábito de Firestore según su ID y actualiza la lista.
     */
    fun deleteImagen(imagenId: String) {
        viewModelScope.launch {
            imageRepository.deleteImagen(imagenId)
            fetchImagenes()
        }
    }
}


/** antiguo codigo

 *
 * /**
 *  * ViewModel que gestiona la lógica de la aplicación relacionada con los hábitos.
 *  * Se comunica con el repositorio (`HabitRepository`) para realizar operaciones en Firestore.
 *  */
 * class FilmViewModel : ViewModel() {
 *
 *     // Instancia del repositorio que maneja las operaciones de Firestore.
 *     private val repository = FilmRepository()
 *
 *     // `_habits` almacena la lista de hábitos de forma interna y permite su modificación.
 *     private val _films = MutableStateFlow<List<Film>>(emptyList())
 *
 *     // `habits` expone los hábitos a la UI sin permitir modificaciones directas.
 *     val films: StateFlow<List<Film>> get() = _films
 *
 *     // Se ejecuta al inicializar el ViewModel y activa la escucha de cambios en Firestore.
 *     init {
 *         listenToFilms()
 *     }
 *
 *     /**
 *      * Escucha los cambios en Firestore en tiempo real.
 *      * Cuando se detecta un cambio en la colección de hábitos, la UI se actualiza automáticamente.
 *      */
 *     private fun listenToFilms() {
 *         repository.listenToFilmsUpdates { updatedFilms ->
 *             _films.value = updatedFilms
 *         }
 *     }
 *
 *     /**
 *      * Recupera la lista de hábitos desde Firestore y actualiza `_habits`.
 *      * Se ejecuta dentro de `viewModelScope.launch` para evitar bloquear la UI.
 *      */
 *     private fun fetchFilms() {
 *         viewModelScope.launch {
 *             _films.value = repository.getFilms()
 *         }
 *     }
 *
 *     /**
 *      * Agrega un nuevo hábito a Firestore y actualiza la lista de hábitos.
 *      */
 *     fun addFilm(film: Film) {
 *         viewModelScope.launch {
 *             repository.addFilm(film)
 *             fetchFilms() // Recarga la lista después de añadir un nuevo hábito.
 *         }
 *     }
 *
 *     /**
 *      * Actualiza un hábito en Firestore y recarga la lista de hábitos.
 *      */
 *     fun updateFilm(film: Film) {
 *         viewModelScope.launch {
 *             repository.updateFilm(film)
 *             fetchFilms()
 *         }
 *     }
 *
 *     /**
 *      * Elimina un hábito de Firestore según su ID y actualiza la lista.
 *      */
 *     fun deleteFilm(filmId: String) {
 *         viewModelScope.launch {
 *             repository.deleteFilm(filmId)
 *             fetchFilms()
 *         }
 *     }
 *
 *     /**
 *      * Agrega una lista de 10 hábitos de ejemplo a Firestore.
 *      * Se usa para pruebas o para pre-cargar la base de datos con hábitos iniciales.
 *      */
 *     fun addExampleFilms() {
 *         val films = listOf(
 *            Film( title = "Harry Potter y la piedra filosofal", director = "Chris Columbus"
 *                , imageResId = R.drawable.harry_potter_y_la_piedra_filosofal
 *             ,comments = "Una aventura mágica en Hogwarts."
 *             ,format = Film.FORMAT_DVD
 *             ,genre = Film.GENRE_ACTION // Cambia según corresponda
 *             ,imdbUrl = "http://www.imdb.com/title/tt0241527"
 *             ,year = 2001),
 *              Film(title = "Regreso al futuro"
 *                    ,director = "Robert Zemeckis"
 *                     ,imageResId = R.drawable.regreso_al_futuro
 *                     ,comments = ""
 *                     ,format = Film.FORMAT_DIGITAL
 *                     ,genre = Film.GENRE_SCIFI
 *                     ,imdbUrl = "http://www.imdb.com/title/tt0088763"
 *                     ,year = 1985),
 *             Film(title = "El rey león"
 *                     ,director = "Roger Allers, Rob Minkoff"
 *                     ,imageResId = R.drawable.el_rey_leon
 *                     ,comments = "Una historia de crecimiento y responsabilidad."
 *                     ,format = Film.FORMAT_BLURAY
 *                     ,genre = Film.GENRE_ACTION // Cambia según corresponda
 *                     ,imdbUrl = "http://www.imdb.com/title/tt0110357"
 *                     ,year = 1994)
 *
 *
 *         )
 *
 *         viewModelScope.launch {
 *             repository.addMultipleFilms(films) // Inserta los hábitos en una sola operación.
 *         }
 *     }
 *
 *     /**
 *      * Elimina múltiples hábitos seleccionados en una sola operación en Firestore.
 *      * Luego, actualiza la lista de hábitos en la UI.
 *      */
 *     fun deleteSelectedFilms(selectedFilms: List<Film>) {
 *         viewModelScope.launch {
 *             repository.deleteMultipleFilms(selectedFilms)
 *             fetchFilms() // Recarga la lista tras la eliminación.
 *         }
 *     }
 * }
 * */