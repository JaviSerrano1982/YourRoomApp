
package com.example.yourroom.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.model.PhotoRequest
import com.example.yourroom.model.PhotoResponse // asume que existe con id, url, primary
import com.example.yourroom.repository.PhotoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

val EditSecondaryPhotosViewModel.PhotoCell.thumbnailModel: Any
    get() = when (this) {
        is EditSecondaryPhotosViewModel.PhotoCell.Existing -> url
        is EditSecondaryPhotosViewModel.PhotoCell.New -> uri
    }

@HiltViewModel
class EditSecondaryPhotosViewModel @Inject constructor(
    private val photoRepo: PhotoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var spaceId: Long = savedStateHandle.get<Long>("spaceId") ?: 0L

    sealed class PhotoCell {
        data class Existing(val id: Long, val url: String) : PhotoCell()
        data class New(val uri: Uri) : PhotoCell()


    }



    data class UiState(
        val cells: List<PhotoCell> = emptyList(),
        val isSaving: Boolean = false,
        val error: String? = null
    )
    var ui by mutableStateOf(UiState())
        private set

    private val toDelete = mutableSetOf<Long>()

    fun init(spaceId: Long) {
        if (this.spaceId == spaceId && ui.cells.isNotEmpty()) return
        this.spaceId = spaceId
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                ui = ui.copy(error = null)
                val photos = photoRepo.list(spaceId) // trae todas (primary y secundarias)
                val secondary = photos.filter { it.primary == false }
                ui = ui.copy(
                    cells = secondary.map { PhotoCell.Existing(id = it.id, url = it.url) }
                )
            } catch (e: Exception) {
                ui = ui.copy(error = e.message ?: "Error al cargar fotos")
            }
        }
    }

    // Helpers de grid
    val PhotoCell.thumbnailModel: Any
        get() = when (this) {
            is PhotoCell.Existing -> url
            is PhotoCell.New -> uri
        }

    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val max = 10
        val current = ui.cells.toMutableList()
        uris.forEach { u ->
            if (current.size >= max) return@forEach
            current.add(PhotoCell.New(u))
        }
        ui = ui.copy(cells = current)
    }

    fun removeAt(index: Int) {
        if (index !in ui.cells.indices) return
        val list = ui.cells.toMutableList()
        when (val cell = list.removeAt(index)) {
            is PhotoCell.Existing -> toDelete.add(cell.id)
            is PhotoCell.New -> Unit
        }
        ui = ui.copy(cells = list)
    }

    fun replaceAt(index: Int, uri: Uri) {
        if (index !in ui.cells.indices) return
        val list = ui.cells.toMutableList()
        when (val cell = list[index]) {
            is PhotoCell.Existing -> {
                // marcar la antigua para borrar y poner una nueva en su lugar
                toDelete.add(cell.id)
                list[index] = PhotoCell.New(uri)
            }
            is PhotoCell.New -> {
                list[index] = PhotoCell.New(uri)
            }
        }
        ui = ui.copy(cells = list)
    }

    suspend fun save(): Boolean {
        ui = ui.copy(isSaving = true, error = null)
        return try {
            ensureFirebaseSession()

            // 1) Borrar fotos existentes marcadas
            for (id in toDelete) {
                photoRepo.deleteOne(id)
            }
            toDelete.clear()

            // 2) Subir y registrar nuevas
            ui.cells.forEachIndexed { i, cell ->
                if (cell is PhotoCell.New) {
                    val url = uploadExtraToFirebase(spaceId, cell.uri, i)
                    photoRepo.add(spaceId, PhotoRequest(url = url, primary = false))
                }
            }

            // 3) Recargar estado coherente desde backend
            load()
            ui = ui.copy(isSaving = false)
            true
        } catch (e: Exception) {
            ui = ui.copy(isSaving = false, error = e.message ?: "Error al guardar")
            false
        }
    }

    private suspend fun ensureFirebaseSession() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) auth.signInAnonymously().await()
    }

    private suspend fun uploadExtraToFirebase(spaceId: Long, uri: Uri, index: Int): String {
        val ref = FirebaseStorage.getInstance()
            .reference
            .child("spaces/$spaceId/photos/photo_${System.currentTimeMillis()}_$index.jpg")
        ref.putFile(uri).await()
        val raw = ref.downloadUrl.await().toString()
        return if ('?' in raw) "$raw&ts=${System.currentTimeMillis()}" else "$raw?ts=${System.currentTimeMillis()}"
    }
}
