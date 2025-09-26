package com.example.yourroom.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.model.PhotoRequest
import com.example.yourroom.model.SpaceBasicsRequest
import com.example.yourroom.model.SpaceDetailsRequest
import com.example.yourroom.model.SpaceResponse
import com.example.yourroom.repository.PhotoRepository
import com.example.yourroom.repository.SpaceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ------------------------------
// VIEWMODEL: PublishSpaceViewModel
// ------------------------------

/**
 * ViewModel para la pantalla de publicación de sala (datos básicos).
 *
 * - Gestiona el estado de los campos de la UI.
 * - Valida la información antes de enviar.
 * - Crea o actualiza la sala en el backend.
 * - Maneja la foto principal de la sala subiéndola a Firebase y registrándola en el backend.
 */
@HiltViewModel
class PublishSpaceViewModel @Inject constructor(
    private val repo: SpaceRepository,
    private val photoRepo: PhotoRepository,
) : ViewModel() {

    /**
     * Estado de la UI durante la publicación de la sala.
     *
     * @param isLoading Indica si se está procesando una acción.
     * @param error     Mensaje de error actual.
     * @param spaceId   Identificador de la sala en backend.
     * @param title     Título de la sala.
     * @param location  Ciudad/ubicación.
     * @param address   Dirección exacta.
     * @param capacity  Capacidad máxima (texto numérico).
     * @param price     Precio por hora (texto numérico).
     * @param photoUri  Foto principal seleccionada.
     */
    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val spaceId: Long? = null,
        val title: String = "",
        val location: String = "",
        val address: String = "",
        val capacity: String = "",
        val price: String = "",
        val photoUri: Uri? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    // ------------------------------
    // Setters de los campos de la UI
    // ------------------------------

    fun onTitleChange(v: String)      { _ui.update { it.copy(title = v) } }
    fun onLocationChange(v: String)   { _ui.update { it.copy(location = v) } }
    fun onAddressChange(v: String)    { _ui.update { it.copy(address = v) } }
    fun onCapacityChange(v: String)   { _ui.update { it.copy(capacity = v) } }
    fun onPriceChange(v: String)      { _ui.update { it.copy(price = v) } }
    fun setPhotoUri(uri: Uri?)        { _ui.update { it.copy(photoUri = uri) } }

    // ------------------------------
    // Validación de campos
    // ------------------------------

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

    // ------------------------------
    // Creación y actualización de sala
    // ------------------------------

    /**
     * Envía los datos básicos al backend.
     * Si no hay id crea una nueva sala, si ya existe la actualiza.
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

    /**
     * Cancela la publicación eliminando el borrador de sala.
     */
    fun cancelAndDelete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val id = _ui.value.spaceId ?: run {
                onSuccess() // si no hay borrador creado aún
                return@launch
            }

            try {
                _ui.update { it.copy(isLoading = true, error = null) }
                val resp = repo.deleteSpace(id)
                if (resp.isSuccessful) {
                    _ui.update { it.copy(isLoading = false, spaceId = null) }
                    onSuccess()
                } else {
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al borrar (HTTP ${resp.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(isLoading = false, error = e.message ?: "Error al borrar")
                }
            }
        }
    }

    /**
     * Asegura que exista un id de sala. Si no lo hay, crea un borrador.
     */
    private suspend fun ensureSpaceId(): Long {
        _ui.value.spaceId?.let { return it }
        return try {
            val draft = repo.createDraft()
            _ui.update { it.copy(spaceId = draft.id) }
            draft.id
        } catch (e: retrofit2.HttpException) {
            _ui.update { it.copy(error = "Error creando borrador (HTTP ${e.code()})") }
            -1L
        } catch (e: Exception) {
            _ui.update { it.copy(error = e.message ?: "Error inesperado") }
            -1L
        }
    }

    // ------------------------------
    // Foto principal
    // ------------------------------

    fun onAddMainPhotoClicked(openPicker: () -> Unit) {
        viewModelScope.launch {
            try {
                _ui.update { it.copy(isLoading = true, error = null) }
                ensureSpaceId() // crea draft si no existe
                openPicker()
            } finally {
                _ui.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onMainPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                _ui.update { it.copy(isLoading = true, photoUri = uri) }
                val spaceId = ensureSpaceId()
                val downloadUrl = uploadToFirebase(spaceId, uri)
                photoRepo.add(spaceId, PhotoRequest(url = downloadUrl, primary = true))
            } finally {
                _ui.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun uploadToFirebase(spaceId: Long, uri: Uri): String {
        ensureFirebaseSession()
        val ref = FirebaseStorage.getInstance()
            .reference.child("spaces/$spaceId/main.jpg")

        ref.putFile(uri).await()
        val raw = ref.downloadUrl.await().toString()
        return if (raw.contains("?")) "$raw&ts=${System.currentTimeMillis()}"
        else "$raw?ts=${System.currentTimeMillis()}"
    }

    private suspend fun ensureFirebaseSession() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    // ------------------------------
    // Helpers de texto
    // ------------------------------

    private fun cleanSpaces(s: String) = s.replace(Regex("\\s+"), " ").trim()

    fun onLocationTyping(v: String) {
        _ui.update { it.copy(location = cleanSpaces(v)) }
    }

    fun onLocationPicked(label: String) {
        _ui.update { it.copy(location = label) }
    }
}

// ------------------------------
// VIEWMODEL: PublishDetailsViewModel
// ------------------------------

/**
 * ViewModel para la pantalla de publicación de sala (detalles).
 *
 * - Gestiona campos de tamaño, disponibilidad, servicios y descripción.
 * - Construye el request para actualizar detalles.
 * - Permite cancelar la publicación eliminando la sala borrador.
 */
@HiltViewModel
class PublishDetailsViewModel @Inject constructor(
    private val repo: SpaceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val spaceId: Long = checkNotNull(savedStateHandle["spaceId"])

    /**
     * Estado de la UI durante la edición de detalles.
     */
    data class UiState(
        val sizeM2Text: String = "",
        val availability: String = "",
        val services: String = "",
        val description: String = "",
        val isSaving: Boolean = false,
        val error: String? = null
    )
    var uiState by mutableStateOf(UiState())
        private set

    // ------------------------------
    // Setters de campos de detalles
    // ------------------------------

    fun onSize(v: String) { uiState = uiState.copy(sizeM2Text = v.filter { it.isDigit() }) }
    fun onAvailability(v: String) { uiState = uiState.copy(availability = v) }
    fun onServices(v: String) { uiState = uiState.copy(services = v) }
    fun onDescription(v: String) { uiState = uiState.copy(description = v) }

    // ------------------------------
    // Construcción del request
    // ------------------------------

    private fun buildRequest(): SpaceDetailsRequest {
        val size = uiState.sizeM2Text.takeIf { it.isNotBlank() }?.toInt()
        return SpaceDetailsRequest(
            sizeM2 = size,
            availability = uiState.availability.trim().ifBlank { null },
            services = uiState.services.trim().ifBlank { null },
            description = uiState.description.trim().ifBlank { null }
        )
    }

    // ------------------------------
    // Guardado de detalles
    // ------------------------------

    /**
     * Envía los detalles de la sala al backend.
     *
     * @return true si se guardó con éxito, false si hubo error.
     */
    suspend fun saveDetailsAwait(): Boolean {
        val req = buildRequest()
        uiState = uiState.copy(isSaving = true, error = null)
        return try {
            repo.updateDetails(spaceId, req)
            uiState = uiState.copy(isSaving = false)
            true
        } catch (e: Exception) {
            uiState = uiState.copy(isSaving = false, error = e.message ?: "Error al guardar")
            false
        }
    }

    // ------------------------------
    // Cancelar publicación
    // ------------------------------

    fun cancelAndDelete(onSuccess: () -> Unit) {
        uiState = uiState.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                val resp = repo.deleteSpace(spaceId)
                if (resp.isSuccessful) {
                    uiState = uiState.copy(isSaving = false)
                    onSuccess()
                } else {
                    uiState = uiState.copy(
                        isSaving = false,
                        error = "Error al borrar (HTTP ${resp.code()})"
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, error = e.message ?: "Error al borrar")
            }
        }
    }
}
