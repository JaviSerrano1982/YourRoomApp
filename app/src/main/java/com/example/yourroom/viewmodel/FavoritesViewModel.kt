package com.example.yourroom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.repository.FavoriteRepository
import com.example.yourroom.ui.screens.favorites.FavoritesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepo: FavoriteRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(FavoritesUiState())
    val ui: StateFlow<FavoritesUiState> = _ui

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, errorMessage = null)

            try {
                val favorites = favoriteRepo.getFavourites()
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    favorites = favorites
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al cargar favoritos"
                )
            }
        }
    }

    fun removeFavorite(spaceId: Long) {
        viewModelScope.launch {
            try {
                favoriteRepo.removeFavourite(spaceId)
                _ui.value = _ui.value.copy(
                    favorites = _ui.value.favorites.filterNot { it.id == spaceId }
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    errorMessage = e.message ?: "No se pudo eliminar el favorito"
                )
            }
        }
    }
}