package com.example.yourroom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.model.SpaceResponse
import com.example.yourroom.repository.PhotoRepository
import com.example.yourroom.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyRoomItem(
    val space: SpaceResponse,
    val primaryPhotoUrl: String? // puede ser null si no hay foto principal
)

@HiltViewModel
class MyRoomsViewModel @Inject constructor(
    private val spaceRepo: SpaceRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val items: List<MyRoomItem> = emptyList(),
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    init {
        loadMyRooms()
    }

    fun loadMyRooms() {
        _ui.value = UiState(isLoading = true)
        viewModelScope.launch {
            try {
                val spaces = spaceRepo.getMine()
                // Para cada sala buscamos su foto principal (si la hay)
                val items = spaces.map { s ->
                    val photos = try {
                        photoRepo.list(s.id)
                    } catch (_: Exception) {
                        emptyList()
                    }
                    val primaryUrl = photos.firstOrNull { it.primary }?.url
                        ?: photos.firstOrNull()?.url
                    MyRoomItem(space = s, primaryPhotoUrl = primaryUrl)
                }
                _ui.value = UiState(isLoading = false, items = items)
            } catch (e: Exception) {
                _ui.value = UiState(isLoading = false, error = e.message ?: "Error al cargar tus salas")
            }
        }
    }
    fun deleteRoom(id: Long) {
        viewModelScope.launch {
            try {
                val resp = spaceRepo.deleteSpace(id)
                if (resp.isSuccessful) {
                    // Quitamos la sala eliminada de la lista actual
                    _ui.value = _ui.value.copy(
                        items = _ui.value.items.filterNot { it.space.id == id },
                        error = null
                    )
                } else {
                    _ui.value = _ui.value.copy(
                        error = "No se pudo borrar (HTTP ${resp.code()})"
                    )
                }
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    error = e.message ?: "Error al borrar la sala"
                )
            }
        }
    }

}
