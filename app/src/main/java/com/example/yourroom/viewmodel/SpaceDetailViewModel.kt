package com.example.yourroom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.repository.PhotoRepository
import com.example.yourroom.repository.SpaceRepository
import com.example.yourroom.repository.UserRepository
import com.example.yourroom.ui.screens.spaceDetail.SpaceDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpaceDetailViewModel @Inject constructor(
    private val spaceRepo: SpaceRepository,
    private val photoRepo: PhotoRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(SpaceDetailUiState())
    val ui: StateFlow<SpaceDetailUiState> = _ui.asStateFlow()

    fun load(spaceId: Long) {
        if (_ui.value.space?.id == spaceId) return

        viewModelScope.launch {
            _ui.value = SpaceDetailUiState(isLoading = true)

            try {
                val space = spaceRepo.getOne(spaceId)

                val photosDeferred = async { photoRepo.list(spaceId) }

                val ownerProfileDeferred = async {
                    runCatching {
                        userRepo.getProfile(space.ownerId)
                    }.getOrNull()
                }

                val photos = photosDeferred.await()
                val ownerProfile = ownerProfileDeferred.await()

                _ui.value = SpaceDetailUiState(
                    isLoading = false,
                    space = space,
                    photos = photos,
                    ownerEmail = ownerProfile?.email,
                    ownerName = listOfNotNull(
                        ownerProfile?.firstName,
                        ownerProfile?.lastName
                    ).joinToString(" ").ifBlank { null },
                    ownerPhotoUrl = ownerProfile?.photoUrl
                )
            } catch (e: Exception) {
                _ui.value = SpaceDetailUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al cargar el detalle de la sala"
                )
            }
        }
    }
}

