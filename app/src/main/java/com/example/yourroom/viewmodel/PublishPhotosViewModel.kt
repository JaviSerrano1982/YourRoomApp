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

@HiltViewModel
class PublishPhotosViewModel @Inject constructor(
    private val photoRepo: PhotoRepository,
    private val spaceRepo: SpaceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val spaceId: Long = checkNotNull(savedStateHandle["spaceId"])

    data class UiState(
        val selected: List<Uri> = emptyList(),
        val isSaving: Boolean = false,
        val error: String? = null
    )
    var ui by mutableStateOf(UiState())
        private set

    // Añadir (máx. 10, evitando duplicados)
    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val current = ui.selected.toMutableList()
        for (u in uris) {
            if (current.size >= 10) break
            if (current.none { it == u }) current.add(u)
        }
        ui = ui.copy(selected = current)
    }

    fun removeAt(index: Int) {
        if (index !in ui.selected.indices) return
        val list = ui.selected.toMutableList()
        list.removeAt(index)
        ui = ui.copy(selected = list)
    }

    fun replaceAt(index: Int, uri: Uri) {
        if (index !in ui.selected.indices) return
        val list = ui.selected.toMutableList()
        list[index] = uri
        ui = ui.copy(selected = list)
    }

    // Sube todas las fotos seleccionadas y crea los registros (primary=false)
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

    // Cancelar publicación (borra el borrador completo)
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

    private suspend fun ensureFirebaseSession() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    private suspend fun uploadExtraToFirebase(spaceId: Long, uri: Uri, index: Int): String {
        val ref = FirebaseStorage.getInstance()
            .reference.child("spaces/$spaceId/photos/photo_${System.currentTimeMillis()}_$index.jpg")
        ref.putFile(uri).await()
        val raw = ref.downloadUrl.await().toString()
        return if (raw.contains("?")) "$raw&ts=${System.currentTimeMillis()}"
        else "$raw?ts=${System.currentTimeMillis()}"
    }
}
