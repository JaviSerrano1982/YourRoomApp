package com.example.yourroom.ui.screens.favorites

import com.example.yourroom.model.SpaceResponse

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val favorites: List<SpaceResponse> = emptyList(),
    val errorMessage: String? = null
)