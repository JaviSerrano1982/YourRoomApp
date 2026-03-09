package com.example.yourroom.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.yourroom.ui.components.GradientTopBar
import com.example.yourroom.ui.components.SpaceCard
import com.example.yourroom.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    navController: NavController,
    vm: FavoritesViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    val topBarHeight = 60.dp

    Box(Modifier.fillMaxSize()) {

        GradientTopBar(
            title = "Favoritos",
            onBackClick = { navController.popBackStack() }
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeight),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "Error",
                        color = Color.Red
                    )
                }
            }

            state.favorites.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Todavía no tienes salas favoritas")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeight)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(state.favorites, key = { it.id }) { space ->
                        SpaceCard(
                            space = space,
                            isFavorite = true,
                            onFavoriteClick = {
                                vm.removeFavorite(space.id)
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("refreshFavoritesSearch", true)
                            },
                            onClick = { navController.navigate("space_detail/${space.id}") }
                        )
                    }
                }
            }
        }
    }
}