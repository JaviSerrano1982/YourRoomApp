package com.example.yourroom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.model.SpaceBasicsRequest
import com.example.yourroom.model.SpaceDetailsRequest
import com.example.yourroom.model.SpaceResponse
import com.example.yourroom.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

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
    val isSaveEnabled: Boolean = false
)

@HiltViewModel
class EditRoomViewModel @Inject constructor(
    private val repo: SpaceRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(EditRoomUi())
    val ui = _ui.asStateFlow()

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
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Error al cargar la sala")
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
        val ok = title.isNotBlank() &&
                location.isNotBlank() &&
                capacity.toIntOrNull()?.let { it > 0 } == true &&
                hourlyPrice.toBigDecimalOrNull()?.let { it >= BigDecimal.ZERO } == true
        return copy(isSaveEnabled = ok)
    }

    fun save(onSuccess: () -> Unit) {
        val s = _ui.value
        if (!s.isSaveEnabled || s.saving) return

        _ui.value = s.copy(saving = true, error = null)

        viewModelScope.launch {
            try {
                // 1) Básicos
                repo.updateBasics(
                    spaceId,
                    SpaceBasicsRequest(
                        ownerId = s.space?.ownerId ?: 0L, // o desde token si lo manejas así
                        title = s.title,
                        location = s.location,
                        addressLine = s.addressLine,
                        capacity = s.capacity.toInt(),
                        hourlyPrice = s.hourlyPrice.toDoubleOrNull()

                    )
                )
                // 2) Detalles
                repo.updateDetails(
                    spaceId,
                    SpaceDetailsRequest(
                        sizeM2 = s.sizeM2.toIntOrNull(),
                        availability = s.availability.ifBlank { null },
                        services = s.services.ifBlank { null },
                        description = s.description.ifBlank { null }
                    )
                )
                _ui.value = _ui.value.copy(saving = false)
                onSuccess()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(saving = false, error = e.message ?: "Error al guardar")
            }
        }
    }
}
