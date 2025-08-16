package com.example.yourroom.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.datastore.UserPreferences
import com.example.yourroom.model.UserProfileDto
import com.example.yourroom.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FieldErrors(
    val firstName: Boolean = false,
    val lastName: Boolean = false,
    val birthDate: Boolean = false,
    val gender: Boolean = false,
    val email: Boolean = false,
    val phone: Boolean = false,
    val location: Boolean = false
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _profile = MutableStateFlow(UserProfileDto())
    val profile: StateFlow<UserProfileDto> = _profile

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _hasChanges = MutableStateFlow(false)
    val hasChanges: StateFlow<Boolean> = _hasChanges

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _userId = MutableStateFlow(0L)
    val userId: StateFlow<Long> = _userId

    private val _localImageUri = MutableStateFlow<Uri?>(null)
    val localImageUri: StateFlow<Uri?> = _localImageUri

    private val _isImageChanged = MutableStateFlow(false)
    val isImageChanged: StateFlow<Boolean> = _isImageChanged

    private val _fieldErrors = MutableStateFlow(FieldErrors())
    val fieldErrors: StateFlow<FieldErrors> = _fieldErrors

    private var initialProfile: UserProfileDto? = null

    private var showErrors: Boolean = false

    private val _emailErrorMessage = MutableStateFlow<String?>(null)
    val emailErrorMessage: StateFlow<String?> = _emailErrorMessage

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }



    /** ===== VALIDACIÓN ===== */
    private fun isFormComplete(p: UserProfileDto): Boolean {
        return p.firstName.isNotBlank() &&
                p.lastName.isNotBlank() &&
                p.birthDate.isNotBlank() &&
                p.gender.isNotBlank() &&
                p.email.isNotBlank() &&
                p.phone.isNotBlank() &&
                p.location.isNotBlank()
    }

    fun validateFields(showErrors: Boolean = false): Boolean {
        val p = _profile.value
        val valid = isFormComplete(p)

        if (showErrors) {
            _fieldErrors.value = FieldErrors(
                firstName = p.firstName.isBlank(),
                lastName = p.lastName.isBlank(),
                birthDate = p.birthDate.isBlank(),
                gender = p.gender.isBlank(),
                email = p.email.isBlank(),
                phone = p.phone.isBlank() || !isValidPhone(p.phone),
                location = p.location.isBlank()
            )
        }
        return valid
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun emailErrorMessage(email: String): String? =
        when {
            email.isBlank() -> "Campo obligatorio"
            !isValidEmail(email) -> "Email inválido"
            else -> null
        }
    private fun cleanPhone(phone: String) = phone.filter(Char::isDigit)
    private fun isValidPhone(phone: String): Boolean = cleanPhone(phone).length == 9

    private fun computeErrors(p: UserProfileDto): FieldErrors {
        return FieldErrors(
            firstName = p.firstName.isBlank(),
            lastName = p.lastName.isBlank(),
            birthDate = p.birthDate.isBlank(),
            gender = p.gender.isBlank(),
            email = p.email.isBlank() || !isValidEmail(p.email),
            phone = p.phone.isBlank() || !isValidPhone(p.phone),
            location = p.location.isBlank()
        )
    }


    private fun hasAnyError(fe: FieldErrors) =
        fe.firstName || fe.lastName || fe.birthDate || fe.gender || fe.email || fe.phone || fe.location

    private fun markInitial(p: UserProfileDto) {
        _profile.value = p
        initialProfile = p.copy()
        _isImageChanged.value = false
        _localImageUri.value = null
        _fieldErrors.value = FieldErrors()   // si usas errores por campo
        _errorMessage.value = null           // no mostrar diálogo al entrar
        showErrors = false                   // si usas el flag de “mostrar errores”
        recomputeHasChanges()
    }


    private fun recomputeHasChanges() {
        val current = _profile.value
        val initial = initialProfile
        val fieldsChanged = initial != null && current != initial

        // ✅ Botón activo si hay cambios en campos o si cambió la imagen
        _hasChanges.value = _isImageChanged.value || fieldsChanged
    }


    /** ===== CICLO DE VIDA / CARGA ===== */
    fun initProfile(context: Context) {
        viewModelScope.launch {
            val prefs = UserPreferences(context)
            val storedId = prefs.userIdFlow.first()
            _userId.value = storedId
            Log.d("Perfil", "✅ userId cargado al entrar: $storedId")

            if (storedId > 0) {
                loadProfile(storedId)
            }
        }
    }

    fun loadProfile(userId: Long) {
        viewModelScope.launch {
            if (userId <= 0) return@launch
            try {
                val p = repository.getProfile(userId)
                markInitial(p)                         //  perfil existente
            } catch (e: Exception) {
                val msg = e.message ?: ""
                // si el backend devuelve 404 al no existir perfil, arrancamos vacío
                if (msg.contains("404")) {
                    markInitial(UserProfileDto())      // perfil nuevo -> estado vacío
                } else {
                    // otros errores sí los mostramos
                    _errorMessage.value = "No se pudo cargar el perfil"
                }
            }
        }
    }


    /** ===== ACCIONES DE USUARIO ===== */
    fun setLocalImage(uri: Uri?) {
        _localImageUri.value = uri
        uri?.let {
            _isImageChanged.value = true
            _profile.value = _profile.value.copy(photoUrl = it.toString())
        } ?: run { _isImageChanged.value = false }

        if (showErrors) {
            val fe = computeErrors(_profile.value)
            _fieldErrors.value = fe
            _hasChanges.value = !hasAnyError(fe) || _isImageChanged.value
        } else {
            recomputeHasChanges()
        }
    }



    fun clearImageChange() {
        _isImageChanged.value = false
        recomputeHasChanges()
    }

    fun updateField(update: UserProfileDto.() -> UserProfileDto) {
        _profile.value = _profile.value.update()

        // Mensaje específico de email (se actualiza siempre)
        _emailErrorMessage.value = emailErrorMessage(_profile.value.email)

        val current = _profile.value
        val base = initialProfile ?: current.also { initialProfile = it.copy() }
        val fieldsChanged = current != base

        if (showErrors) {
            val fe = computeErrors(current)
            _fieldErrors.value = fe

            // ✅ Mantén el botón activo si:
            // - hay cambios respecto al snapshot, o
            // - cambió la imagen, o
            // - ya no quedan errores (para permitir guardar aunque coincida con el snapshot)
            _hasChanges.value = fieldsChanged || _isImageChanged.value || !hasAnyError(fe)

            if (!hasAnyError(fe)) {
                _errorMessage.value = null // cierra el diálogo si todo ya está OK
            }
        } else {
            // modo normal: botón activo si hay cambios vs snapshot o si cambió la imagen
            _hasChanges.value = fieldsChanged || _isImageChanged.value
        }
    }




    fun updateProfile(userId: Long) {
        if (_isSaving.value) return
        viewModelScope.launch {
            // 🔍 Validación completa (incluye formato de email)
            val errors = computeErrors(_profile.value)
            _emailErrorMessage.value = emailErrorMessage(_profile.value.email)
            if (hasAnyError(errors)) {
                _fieldErrors.value = errors
                _errorMessage.value = "Por favor, corrige los campos marcados"
                showErrors = true
                return@launch
            }

            // ✅ Si todo está bien, guardar
            _isSaving.value = true
            try {
                val safeProfile = _profile.value
                val result = repository.updateProfile(userId, safeProfile)
                _profile.value = result
                initialProfile = result.copy()
                _isImageChanged.value = false
                _saveSuccess.value = true

                //Reseteamos errore y mensajes
                _emailErrorMessage.value = null
                showErrors = false
                _fieldErrors.value = FieldErrors() // limpia errores
                _errorMessage.value = null

                recomputeHasChanges()
            } catch (e: Exception) {
                _errorMessage.value = e.message
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }



    fun clearError() {
        _errorMessage.value = null
    }
}
