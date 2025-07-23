
package com.campusdigitalfp.filmotecav2.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.campusdigitalfp.filmotecav2.model.Film
import com.campusdigitalfp.filmotecav2.R
import com.campusdigitalfp.filmotecav2.common.FilmTopAppBar
import com.campusdigitalfp.filmotecav2.viewModel.AuthViewModel
import com.campusdigitalfp.filmotecav2.viewModel.FilmViewModel

@Composable
fun FilmListScreen(navController: NavHostController, viewModel: FilmViewModel,authViewModel: AuthViewModel) {//ej3 ev2 le paso tambien el view model/ ej42ev y el authView
    var isActionMode by remember { mutableStateOf(false) }
    val selectedFilms = remember { mutableStateListOf<Film>() }
    //ej32ev
    val films by viewModel.films.collectAsState() // ej3 ev2 Estado de los hábitos observados desde el ViewModel.

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        FilmTopAppBar(navController,
            viewModel,//ej3 2ev
            authViewModel,//ej42ev
            principal = true,
            editar = false,
            selectedFilms = selectedFilms,
            isActionMode = isActionMode,


        ) {
            isActionMode = it
        }
    }) { innerPadding ->
        FilmListContent(
           films,//ej3ev2ya no necesito esto porque le paso la lista de films directamente films = films,
            selectedFilms = selectedFilms,
            isActionMode = isActionMode,
            navController = navController,
            innerPadding = innerPadding
        ) { isActionMode = it }
    }
}

@Composable
fun FilmListContent(
    films: List<Film>,
    selectedFilms: MutableList<Film>,
    isActionMode: Boolean,
    navController: NavHostController,
    innerPadding: PaddingValues,
    onActionModeChange: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        itemsIndexed(films) { index, film ->
            VistaFilm(film = film, onClick = {
                if (isActionMode) {
                    if (selectedFilms.contains(film)) {
                        selectedFilms.remove(film)
                        if (selectedFilms.isEmpty()) {
                            onActionModeChange(false)
                        }
                    } else {
                        selectedFilms.add(film)
                    }
                } else {
                    navController.navigate("data/$index")
                }
            }, onLongClick = {
                selectedFilms.add(film)
                onActionModeChange(true)
            }, isSelected = selectedFilms.contains(film)
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VistaFilm(film: Film, onClick: () -> Unit, onLongClick: () -> Unit, isSelected: Boolean) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
           // ANTESdelej52ev painter = painterResource(if (isSelected) R.drawable.selected else film.imageResId),
            model = film.imageUrl ?: R.drawable.placeholder, // ej52ev Cargar la imagen desde la URL o mostrar un placeholder

            contentDescription = "Icono",
            modifier = Modifier
                .padding(if (isSelected) 15.dp else 0.dp)
                .size(if (isSelected) 50.dp else 80.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = film.title.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = film.director.toString(), style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}