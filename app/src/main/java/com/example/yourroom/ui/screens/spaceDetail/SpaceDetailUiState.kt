package com.example.yourroom.ui.screens.spaceDetail

import com.example.yourroom.model.PhotoResponse
import com.example.yourroom.model.SpaceResponse

data class SpaceDetailUiState(
    val isLoading: Boolean = true,
    val space: SpaceResponse? = null,
    val photos: List<PhotoResponse> = emptyList(),
    val ownerEmail: String? = null,
    val ownerName: String? = null,
    val ownerPhotoUrl: String? = null,
    val errorMessage: String? = null
)
