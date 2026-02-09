package com.example.yourroom.ui.screens.home



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.yourroom.viewmodel.SearchSpacesViewModel
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.yourroom.navigation.BottomNavItem.Favorites.imageVector


@Composable
fun SearchSpacesScreen(
    navController: NavHostController,
    vm: SearchSpacesViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var isFavorite by remember { mutableStateOf(false) }


    Column(Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(80.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.85f)
                    .padding(end = 16.dp)
                    .focusRequester(focusRequester),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Buscar salas (ej: gimnasio)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        state.errorMessage?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(16.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.filteredSpaces) { space ->

                Card {
                    Row(Modifier.fillMaxWidth().padding(12.dp)) {

                        // Imagen (si hay)
                        if (!space.primaryPhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = space.primaryPhotoUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                        }

                        Column(Modifier.weight(1f)) {
                            Text(space.title ?: "Sin título", fontWeight = FontWeight.Bold)
                            Text(space.location ?: "", color = Color.DarkGray)
                            Text(
                                text = space.hourlyPrice?.toPlainString()?.let { "$it €/h" } ?: "",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { isFavorite = !isFavorite },
                            modifier = Modifier.offset(y = 10.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite)
                                    Icons.Filled.Favorite
                                else
                                    Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) Color.Red else Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }



                    }
                }
            }
        }
    }
}
