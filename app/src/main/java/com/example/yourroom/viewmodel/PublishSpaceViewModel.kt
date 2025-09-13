package com.example.yourroom.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.model.SpaceBasicsRequest
import com.example.yourroom.model.SpaceResponse
import com.example.yourroom.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublishSpaceViewModel @Inject constructor(
    private val repo: SpaceRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val spaceId: Long? = null,
        // Campos de tu UI
        val title: String = "",
        val location: String = "",
        val address: String = "",
        val capacity: String = "",
        val price: String = "",
        val photoUri: Uri? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    // Setters (con los nombres de tu pantalla)
    fun onTitleChange(v: String)      { _ui.update { it.copy(title = v) } }
    fun onLocationChange(v: String)   { _ui.update { it.copy(location = v) } }
    fun onAddressChange(v: String)    { _ui.update { it.copy(address = v) } }
    fun onCapacityChange(v: String)   { _ui.update { it.copy(capacity = v) } }
    fun onPriceChange(v: String)      { _ui.update { it.copy(price = v) } }
    fun setPhotoUri(uri: Uri?)        { _ui.update { it.copy(photoUri = uri) } }

    private fun validateBasics(): String? {
        val s = _ui.value
        if (s.title.trim().length < 3) return "El título debe tener al menos 3 caracteres"
        val cap = s.capacity.toIntOrNull()
        if (cap == null || cap <= 0) return "La capacidad debe ser un número > 0"
        val price = s.price.replace(',', '.').toDoubleOrNull()
        if (price == null || price <= 0.0) return "El precio/hora no es válido"
        if (s.location.isBlank()) return "La ubicación es obligatoria"
        if (s.address.isBlank()) return "La dirección es obligatoria"
        if (s.photoUri == null) return "Selecciona una foto para continuar"
        return null
    }

    /**
     * Crea o actualiza los básicos.
     * Al terminar, entrega el id al callback para navegar.
     */
    fun submitBasics(onSuccess: (Long) -> Unit) {
        val validation = validateBasics()
        if (validation != null) {
            _ui.update { it.copy(error = validation) }
            return
        }

        val s = _ui.value
        viewModelScope.launch {
            try {
                _ui.update { it.copy(isLoading = true, error = null) }

                val body = SpaceBasicsRequest(
                    // ownerId: si tu backend ya lo infiere del JWT, quítalo del DTO
                    title = s.title.trim(),
                    location = s.location.trim(),
                    addressLine = s.address.trim(),
                    capacity = s.capacity.toIntOrNull(),
                    hourlyPrice = s.price.replace(',', '.').toDoubleOrNull()
                )

                val response: SpaceResponse =
                    if (s.spaceId == null) repo.createSpace(body)
                    else repo.updateBasics(s.spaceId, body)

                _ui.update { it.copy(isLoading = false, spaceId = response.id) }
                onSuccess(response.id)
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message ?: "Error inesperado") }
            }
        }
    }
}
