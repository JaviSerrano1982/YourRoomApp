package com.example.yourroom.viewmodel

import android.net.Uri
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.collections.forEachIndexed

data class EditRoomUi(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val space: SpaceResponse? = null,
    // Campos del formulario (todos en una pantalla)
    val title: String = "",
    val location: String = "",
    val addressLine: String = "",
    val capacity: String = "",
    val hourlyPrice: String = "",
    val sizeM2: String = "",
    val availability: String = "",
    val services: String = "",
    val description: String = "",
    val isSaveEnabled: Boolean = false,
    val mainPhotoLocal: Uri? = null,
    val pendingSecondaryCount: Int = 0

)

@HiltViewModel
class EditRoomViewModel @Inject constructor(
    private val repo: SpaceRepository,
    private val photoRepo: PhotoRepository

) : ViewModel() {

    private val _ui = MutableStateFlow(EditRoomUi())
    val ui = _ui.asStateFlow()

    // Cambios de fotos pendientes hasta pulsar "Guardar cambios"
    private var pendingPrimaryUri: Uri? = null
    private var pendingSecondaryUris: List<Uri> = emptyList()

    private var pendingSecondaryDeletes: List<Long> = emptyList()


    private var spaceId: Long = 0L

    fun init(spaceId: Long) {
        if (this.spaceId == spaceId && _ui.value.space != null) return
        this.spaceId = spaceId
        load()
    }

    private fun load() {
        _ui.value = _ui.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val sp = repo.getSpace(spaceId)
                _ui.value = _ui.value.copy(
                    loading = false,
                    space = sp,
                    title = sp.title.orEmpty(),
                    location = sp.location.orEmpty(),
                    addressLine = sp.addressLine.orEmpty(),
                    capacity = sp.capacity?.toString().orEmpty(),
                    hourlyPrice = sp.hourlyPrice?.toPlainString().orEmpty(),
                    sizeM2 = sp.sizeM2?.toString().orEmpty(),
                    availability = sp.availability.orEmpty(),
                    services = sp.services.orEmpty(),
                    description = sp.description.orEmpty(),
                ).recomputeEnabled()
            } catch (e: Exception) {
                _ui.value =
                    _ui.value.copy(loading = false, error = e.message ?: "Error al cargar la sala")
            }
        }
    }

    fun onChange(
        title: String? = null,
        location: String? = null,
        addressLine: String? = null,
        capacity: String? = null,
        hourlyPrice: String? = null,
        sizeM2: String? = null,
        availability: String? = null,
        services: String? = null,
        description: String? = null
    ) {
        _ui.value = _ui.value.copy(
            title = title ?: _ui.value.title,
            location = location ?: _ui.value.location,
            addressLine = addressLine ?: _ui.value.addressLine,
            capacity = capacity ?: _ui.value.capacity,
            hourlyPrice = hourlyPrice ?: _ui.value.hourlyPrice,
            sizeM2 = sizeM2 ?: _ui.value.sizeM2,
            availability = availability ?: _ui.value.availability,
            services = services ?: _ui.value.services,
            description = description ?: _ui.value.description
        ).recomputeEnabled()
    }

    private fun EditRoomUi.recomputeEnabled(): EditRoomUi {
        val photosPending = (pendingPrimaryUri != null) ||
                pendingSecondaryUris.isNotEmpty()||
                pendingSecondaryDeletes.isNotEmpty()

        val ok =
            title.isNotBlank() &&
                    location.isNotBlank() &&
                    addressLine.isNotBlank() &&
                    capacity.toIntOrNull()?.let { it > 0 } == true &&
                    hourlyPrice.toBigDecimalOrNull()
                        ?.let { it > java.math.BigDecimal.ZERO } == true &&
                    sizeM2.toIntOrNull()?.let { it > 0 } == true &&
                    availability.isNotBlank() &&
                    services.isNotBlank() &&
                    description.isNotBlank() &&
                    // Habilitar si hay cambios de campos O hay fotos pendientes
                    (isDirty() || photosPending)

        return copy(isSaveEnabled = ok)
    }


    private fun EditRoomUi.isDirty(): Boolean {
        val sp = space ?: return false
        return title != sp.title.orEmpty() ||
                location != sp.location.orEmpty() ||
                addressLine != sp.addressLine.orEmpty() ||
                capacity != sp.capacity?.toString().orEmpty() ||
                hourlyPrice != sp.hourlyPrice?.toPlainString().orEmpty() ||
                sizeM2 != sp.sizeM2?.toString().orEmpty() ||
                availability != sp.availability.orEmpty() ||
                services != sp.services.orEmpty() ||
                description != sp.description.orEmpty()
    }


    fun save(onSuccess: () -> Unit) {
        val s = _ui.value
        if (!s.isSaveEnabled || s.saving) return

        // Toma siempre el ID real de la sala cargada en la UI
        val effectiveSpaceId = s.space?.id
        if (effectiveSpaceId == null) {
            _ui.value = s.copy(error = "No se encontró el ID de la sala para guardar")
            return
        }



        _ui.value = s.copy(saving = true, error = null)

        viewModelScope.launch {
            try {
                // 1) Básicos
                repo.updateBasics(
                    effectiveSpaceId,
                    SpaceBasicsRequest(
                        ownerId = s.space?.ownerId ?: 0L,
                        title = s.title,
                        location = s.location,
                        addressLine = s.addressLine,
                        capacity = s.capacity.toInt(),
                        hourlyPrice = s.hourlyPrice.toDouble()
                    )
                )

                // 2) Detalles
                repo.updateDetails(
                    effectiveSpaceId,
                    SpaceDetailsRequest(
                        sizeM2 = s.sizeM2.toInt(),
                        availability = s.availability.ifBlank { null },
                        services = s.services.ifBlank { null },
                        description = s.description.ifBlank { null }
                    )
                )

                // 3) FOTOS (subir solo si hay pendientes)
                if (pendingPrimaryUri != null || pendingSecondaryUris.isNotEmpty() ||
                    pendingSecondaryDeletes.isNotEmpty()) {

                    ensureFirebaseSession()

                    // 3.0) Borrados primero
                    if (pendingSecondaryDeletes.isNotEmpty()) {
                        pendingSecondaryDeletes.forEach { id -> photoRepo.deleteOne(id) }
                    }

                    // 3.1) Principal
                    pendingPrimaryUri?.let { uri ->
                        val url = uploadPrimaryToFirebase(effectiveSpaceId, uri)
                        photoRepo.add(effectiveSpaceId, PhotoRequest(url = url, primary = true))

                    }

                    // 3.2) Secundarias
                    if (pendingSecondaryUris.isNotEmpty()) {
                        pendingSecondaryUris.forEachIndexed { index, uri ->
                            val url = uploadExtraToFirebase(effectiveSpaceId, uri, index)
                            photoRepo.add(effectiveSpaceId, PhotoRequest(url = url, primary = false))
                        }
                    }

                    // Limpiar pendientes
                    pendingPrimaryUri = null
                    pendingSecondaryUris = emptyList()
                    pendingSecondaryDeletes = emptyList()
                }

                _ui.value = _ui.value.copy(
                    saving = false,
                    mainPhotoLocal = null,           // limpiamos preview local
                    pendingSecondaryCount = 0        // limpiamos secundarias pendientes
                ).recomputeEnabled()

                onSuccess()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(saving = false, error = e.message ?: "Error al guardar")
            }
        }
    }

    private suspend fun uploadPrimaryToFirebase(spaceId: Long, uri: Uri): String {
        val ref = FirebaseStorage.getInstance()
            .reference
            .child("spaces/$spaceId/main.jpg")
        ref.putFile(uri).await()
        val raw = ref.downloadUrl.await().toString()
        return if ('?' in raw) "$raw&ts=${System.currentTimeMillis()}" else "$raw?ts=${System.currentTimeMillis()}"
    }

    private suspend fun uploadExtraToFirebase(spaceId: Long, uri: Uri, index: Int): String {
        val ref = FirebaseStorage.getInstance()
            .reference
            .child("spaces/$spaceId/photos/photo_extra_${index}_${System.currentTimeMillis()}.jpg")
        ref.putFile(uri).await()
        val raw = ref.downloadUrl.await().toString()
        return if ('?' in raw) "$raw&ts=${System.currentTimeMillis()}" else "$raw?ts=${System.currentTimeMillis()}"
    }

    private suspend fun ensureFirebaseSession() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) auth.signInAnonymously().await()
    }
    // 1) Igual que en PublishBasics: guarda la Uri y activa el botón de guardar
    fun onPrimaryPhotoSelected(uri: Uri) {
        pendingPrimaryUri = uri
        _ui.value = _ui.value.copy(
            mainPhotoLocal = uri,
            error = null
        ).recomputeEnabled()
    }

    // 2) Marca secundarias pendientes (si aún no quieres secundarias, puedes omitir su uso)
    fun addSecondaryPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        pendingSecondaryUris = uris
        _ui.value = _ui.value.copy(
            pendingSecondaryCount = uris.size,
            error = null
        ).recomputeEnabled()
    }
    fun applySecondaryDelta(toDeleteIds: List<Long>, newUris: List<Uri>) {
        pendingSecondaryDeletes = toDeleteIds
        pendingSecondaryUris = newUris
        _ui.value = _ui.value.copy(
            pendingSecondaryCount = newUris.size,
            error = null
        ).recomputeEnabled()
    }


}
