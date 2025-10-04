// PublishPhotosViewModel.kt
package com.example.yourroom.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.model.PhotoRequest
import com.example.yourroom.repository.PhotoRepository
import com.example.yourroom.repository.SpaceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ------------------------------
// VIEWMODEL: PublishPhotosViewModel
// ------------------------------

/**
 * ViewModel encargado de gestionar la selección,
 * subida y persistencia de fotos en una sala durante
 * el proceso de publicación.
 *
 * - Gestiona el estado de la UI con las fotos seleccionadas.
 * - Sube imágenes a Firebase Storage.
 * - Crea registros de fotos en el backend a través de PhotoRepository.
 * - Permite cancelar la publicación eliminando el borrador de la sala.
 */
@HiltViewModel
class PublishPhotosViewModel @Inject constructor(
    private val photoRepo: PhotoRepository,
    private val spaceRepo: SpaceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Id de la sala actual, recibido desde navegación (SafeArgs)
    val spaceId: Long = checkNotNull(savedStateHandle["spaceId"])

    /**
     * Estado interno de la pantalla de gestión de fotos.
     *
     * @param selected  Lista de URIs de fotos seleccionadas en la UI.
     * @param isSaving  Indica si se está procesando una acción (guardar/borrar).
     * @param error     Mensaje de error en caso de fallo.
     */
    data class UiState(
        val selected: List<Uri> = emptyList(),
        val isSaving: Boolean = false,
        val error: String? = null
    )
    var ui by mutableStateOf(UiState())
        private set

    // ------------------------------
    // Gestión de selección de fotos
    // ------------------------------

    /**
     * Añade nuevas fotos seleccionadas.
     * Máximo 10 elementos, evitando duplicados.
     */
    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val current = ui.selected.toMutableList()
        for (u in uris) {
            if (current.size >= 10) break
            if (current.none { it == u }) current.add(u)
        }
        ui = ui.copy(selected = current)
    }

    /**
     * Elimina la foto en el índice indicado.
     */
    fun removeAt(index: Int) {
        if (index !in ui.selected.indices) return
        val list = ui.selected.toMutableList()
        list.removeAt(index)
        ui = ui.copy(selected = list)
    }

    /**
     * Reemplaza la foto en el índice indicado con otra URI.
     */
    fun replaceAt(index: Int, uri: Uri) {
        if (index !in ui.selected.indices) return
        val list = ui.selected.toMutableList()
        list[index] = uri
        ui = ui.copy(selected = list)
    }

    // ------------------------------
    // Guardado y subida de fotos
    // ------------------------------

    /**
     * Sube todas las fotos seleccionadas a Firebase y
     * registra cada una en el backend (primary = false).
     *
     * @return true si todo fue correcto, false si hubo error.
     */
    suspend fun saveAllAwait(): Boolean {
        if (ui.selected.isEmpty()) return true
        ui = ui.copy(isSaving = true, error = null)
        return try {
            ensureFirebaseSession()
            ui.selected.forEachIndexed { i, uri ->
                val url = uploadExtraToFirebase(spaceId, uri, i)
                photoRepo.add(spaceId, PhotoRequest(url = url, primary = false))
            }
            ui = ui.copy(isSaving = false)
            true
        } catch (e: Exception) {
            ui = ui.copy(isSaving = false, error = e.message ?: "Error al guardar fotos")
            false
        }
    }
    suspend fun saveAllAwaitAndPublish(): Boolean {
        val ok = saveAllAwait()
        if (!ok) return false
        // Publicar ahora
        spaceRepo.publish(spaceId)
        return true
    }

    // ------------------------------
    // Cancelar publicación
    // ------------------------------

    /**
     * Cancela la publicación de la sala borrador,
     * eliminándola completamente del backend.
     */
    fun cancelAndDelete(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                ui = ui.copy(isSaving = true, error = null)
                spaceRepo.deleteSpace(spaceId)
                ui = ui.copy(isSaving = false)
                onDone()
            } catch (e: Exception) {
                ui = ui.copy(isSaving = false, error = e.message ?: "Error al cancelar")
            }
        }
    }

    // ------------------------------
    // Firebase helpers
    // ------------------------------

    /**
     * Asegura que exista una sesión de Firebase.
     * Si no hay usuario actual, inicia sesión anónima.
     */
    private suspend fun ensureFirebaseSession() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    /**
     * Sube un archivo a Firebase Storage y devuelve la URL pública.
     * Se añade un timestamp para evitar caché.
     */
    private suspend fun uploadExtraToFirebase(spaceId: Long, uri: Uri, index: Int): String {
        val ref = FirebaseStorage.getInstance()
            .reference.child("spaces/$spaceId/photos/photo_${System.currentTimeMillis()}_$index.jpg")
        ref.putFile(uri).await()
        val raw = ref.downloadUrl.await().toString()
        return if (raw.contains("?")) "$raw&ts=${System.currentTimeMillis()}"
        else "$raw?ts=${System.currentTimeMillis()}"
    }
}
