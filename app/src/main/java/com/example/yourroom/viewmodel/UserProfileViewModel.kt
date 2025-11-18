package com.example.yourroom.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.datastore.UserPreferences
import com.example.yourroom.model.UserProfileDto
import com.example.yourroom.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Estructura de errores por campo para el formulario de perfil.
 * Cada flag indica si el campo está en error (true = hay error).
 */
data class FieldErrors(
    val firstName: Boolean = false,
    val lastName: Boolean = false,
    val birthDate: Boolean = false,
    val gender: Boolean = false,
    val email: Boolean = false,
    val phone: Boolean = false,
    val location: Boolean = false
)

/**
 * ViewModel responsable de:
 * - Cargar/guardar el perfil del usuario mediante el repositorio.
 * - Validar campos y exponer errores individuales y generales.
 * - Gestionar el ciclo de vida del “estado sucio” (ediciones pendientes).
 * - Subir imagen a Firebase Storage y persistir la URL en backend.
 * - Iniciar sesión en Firebase (token personalizado).
 *
 * Exposición de estado mediante StateFlow para que la UI (Compose) reaccione
 * a cambios de forma declarativa.
 */
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    // ---------------------------------------------------------------------
    // ESTADO DE UI (StateFlow) — leído por las pantallas Compose
    // ---------------------------------------------------------------------

    /** Perfil en edición/visualización. */
    private val _profile = MutableStateFlow(UserProfileDto())
    val profile: StateFlow<UserProfileDto> = _profile

    /** Spinner/botón deshabilitado durante guardado. */
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    /**
     * Flag interno: hay ediciones pendientes (dirty). Se pone a true ante
     * cualquier cambio de campo o imagen, y se limpia tras un guardado exitoso.
     */
    private val _hasUnsavedEdits = MutableStateFlow(false)

    /**
     * Estado compuesto que la UI usa para habilitar acciones (p.e. botón “Guardar”):
     * hay cambios si existe diff con el estado inicial O hay imagen cambiada
     * O hay ediciones pendientes marcadas.
     */
    private val _hasChanges = MutableStateFlow(false)
    val hasChanges: StateFlow<Boolean> = _hasChanges

    /** Mensaje de error general (para diálogos/toasts en UI). */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /** userId del backend (no confundir con uid de Firebase). */
    private val _userId = MutableStateFlow(0L)
    val userId: StateFlow<Long> = _userId

    /** URI local de imagen seleccionada (antes/después de subir). */
    private val _localImageUri = MutableStateFlow<Uri?>(null)
    val localImageUri: StateFlow<Uri?> = _localImageUri

    /** True si el usuario ha cambiado la imagen (para habilitar guardado). */
    private val _isImageChanged = MutableStateFlow(false)
    val isImageChanged: StateFlow<Boolean> = _isImageChanged

    /** Errores de validación por campo (para resaltar inputs). */
    private val _fieldErrors = MutableStateFlow(FieldErrors())
    val fieldErrors: StateFlow<FieldErrors> = _fieldErrors

    /** Mensaje específico de error para email (formato/campo vacío). */
    private val _emailErrorMessage = MutableStateFlow<String?>(null)
    val emailErrorMessage: StateFlow<String?> = _emailErrorMessage

    /** True cuando un guardado ha terminado con éxito (para mostrar feedback). */
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    /** True mientras se está subiendo la foto a Firebase Storage. */
    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto

    // ---------------------------------------------------------------------
    // ESTADO INTERNO — no expuesto directamente a la UI
    // ---------------------------------------------------------------------

    /** Snapshot del perfil cargado inicialmente para detectar cambios. */
    private var initialProfile: UserProfileDto? = null

    /** Controla si debemos “enseñar” errores en caliente. */
    private var showErrors: Boolean = false



    val canPublish: StateFlow<Boolean> =
        profile
            .map { p -> isFormComplete(p) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)


    // ---------------------------------------------------------------------
    // UTILIDADES DE VALIDACIÓN
    // ---------------------------------------------------------------------

    /** Comprueba que todos los campos obligatorios estén completos (no vacíos). */
    private fun isFormComplete(p: UserProfileDto): Boolean {
        return !p.firstName.isNullOrBlank() &&
                !p.lastName.isNullOrBlank() &&
                !p.birthDate.isNullOrBlank() &&
                !p.gender.isNullOrBlank() &&
                !p.email.isNullOrBlank() &&
                !p.phone.isNullOrBlank() &&
                !p.location.isNullOrBlank()
    }

    /** Valida el formulario y, si `showErrors = true`, actualiza errores por campo. */
    fun validateFields(showErrors: Boolean = false): Boolean {
        val p = _profile.value
        val valid = isFormComplete(p)
        if (showErrors) {
            _fieldErrors.value = computeErrors(p)
        }
        return valid
    }

    /** Valida formato de email. */
    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    /** Mensaje específico para el campo email (obligatorio + formato). */
    // antes
// fun emailErrorMessage(email: String): String?

// después
    fun emailErrorMessage(email: String?): String? {
        if (email.isNullOrBlank()) return "Campo obligatorio"
        val ok = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return if (!ok) "Email no válido" else null
    }


    /** Normaliza teléfono dejando solo dígitos. */
    private fun cleanPhone(phone: String) = phone.filter(Char::isDigit)

    /** Regla actual: un teléfono válido debe tener 9 dígitos. */
    private fun isValidPhone(phone: String): Boolean = cleanPhone(phone).length == 9

    /** Construye el objeto de errores por campo a partir del perfil actual. */
    private fun computeErrors(p: UserProfileDto): FieldErrors {
        return FieldErrors(
            firstName = p.firstName.isNullOrBlank(),
            lastName = p.lastName.isNullOrBlank(),
            birthDate = p.birthDate.isNullOrBlank(),
            gender = p.gender.isNullOrBlank(),
            email = p.email.isNullOrBlank() || !isValidEmail(p.email),
            phone = p.phone.isNullOrBlank() || !isValidPhone(p.phone),
            location = p.location.isNullOrBlank()
        )
    }

    /** Devuelve true si existe al menos un error de campo. */
    private fun hasAnyError(fe: FieldErrors) =
        fe.firstName || fe.lastName || fe.birthDate || fe.gender || fe.email || fe.phone || fe.location

    // ---------------------------------------------------------------------
    // GESTIÓN DE ESTADO INICIAL Y CAMBIOS
    // ---------------------------------------------------------------------

    /**
     * Fija el perfil “inicial”, limpia errores y marca el estado como no editado.
     * También sincroniza la URI local de imagen (si existe photoUrl).
     */
    private fun markInitial(p: UserProfileDto) {
        _profile.value = p
        initialProfile = p.copy()
        _isImageChanged.value = false
        _localImageUri.value = null
        _fieldErrors.value = FieldErrors()
        _errorMessage.value = null
        showErrors = false
        _hasUnsavedEdits.value = false
        recomputeHasChanges()
    }

    /** Recalcula el flag `hasChanges` combinando “dirty”, cambio de imagen y diff real. */
    private fun recomputeHasChanges() {
        val current = _profile.value
        val initial = initialProfile
        val fieldsChanged = initial != null && current != initial
        _hasChanges.value = _hasUnsavedEdits.value || _isImageChanged.value || fieldsChanged
    }

    // ---------------------------------------------------------------------
    // CICLO DE VIDA / CARGA INICIAL
    // ---------------------------------------------------------------------

    /**
     * Carga el userId desde DataStore y, si es válido (>0), solicita el perfil al backend.
     * Debe llamarse al entrar en la pantalla de perfil.
     */
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

    /**
     * Solicita el perfil al backend. Si el servidor devuelve 404 (perfil inexistente),
     * inicializa el estado con un perfil vacío para que el usuario pueda completarlo.
     */
    fun loadProfile(userId: Long) {
        viewModelScope.launch {
            if (userId <= 0) return@launch

            _userId.value = userId

            _isSaving.value = true
            try {
                val dto = runCatching { repository.getProfile(userId) }
                    .fold(
                        onSuccess = { it },                      // puede ser null según tu repo
                        onFailure = { e ->
                            // 404 => perfil inexistente -> tratar como vacío
                            val http = e as? retrofit2.HttpException
                            if (http?.code() == 404 || e.message?.contains("404") == true) null
                            else throw e
                        }
                    )

                // Perfil vacío OK (no es error)
                markInitial(dto ?: UserProfileDto())
                _fieldErrors.value = FieldErrors()
                _emailErrorMessage.value = null
                _hasChanges.value = false
                _errorMessage.value = null                 // <- clave: NO mostrar diálogo

            } catch (e: retrofit2.HttpException) {
                // Otros códigos != 404: si quieres, muestra snackbar, pero NO el diálogo
                _errorMessage.value = null
            } catch (_: java.io.IOException) {
                // Sin conexión: no muestres el diálogo de "Datos incompletos"
                _errorMessage.value = null
            } catch (_: Exception) {
                _errorMessage.value = null
            } finally {
                _isSaving.value = false
            }
        }
    }



    // ---------------------------------------------------------------------
    // ACCIONES DE USUARIO: CAMPOS E IMAGEN
    // ---------------------------------------------------------------------

    /**
     * Actualiza la imagen local seleccionada (galería/cámara).
     * Marca edición pendiente y recalcula errores si corresponde.
     */
    fun setLocalImage(uri: Uri?) {
        _localImageUri.value = uri
        // La foto solo se marca como cambiada si existe URI local.
        _isImageChanged.value = uri != null

        _hasUnsavedEdits.value = true

        if (showErrors) {
            _fieldErrors.value = computeErrors(_profile.value)
        }
        recomputeHasChanges()
    }

    /** Permite limpiar el “cambio de imagen” (útil tras cancelar o revertir). */
    fun clearImageChange() {
        _isImageChanged.value = false
        recomputeHasChanges()
    }

    /**
     * Actualiza uno o varios campos del perfil.
     * Ejemplo de uso desde UI:
     *   viewModel.updateField { copy(firstName = nuevoNombre) }
     */
    fun updateField(update: UserProfileDto.() -> UserProfileDto) {
        _profile.value = _profile.value.update()
        _hasUnsavedEdits.value = true

        // Mantenemos el feedback específico de email actualizado
        _emailErrorMessage.value = emailErrorMessage(_profile.value.email)

        if (showErrors) {
            val fe = computeErrors(_profile.value)
            _fieldErrors.value = fe
            if (!hasAnyError(fe)) {
                _errorMessage.value = null // cierra diálogo si todo ya está OK
            }
        }
        recomputeHasChanges()
    }

    // ---------------------------------------------------------------------
    // GUARDADO DE PERFIL (BACKEND)
    // ---------------------------------------------------------------------

    /**
     * Valida y guarda el perfil en el backend.
     * - Si hay errores, se actualizan los errores por campo y se expone un error general.
     * - Si todo va bien, actualiza el estado inicial y marca el guardado como exitoso.
     */
    fun updateProfile(userId: Long) {
        if (_isSaving.value) return
        viewModelScope.launch {
            val errors = computeErrors(_profile.value)
            _emailErrorMessage.value = emailErrorMessage(_profile.value.email)

            if (hasAnyError(errors)) {
                _fieldErrors.value = errors
                _errorMessage.value = "Por favor, corrige los campos marcados"
                showErrors = true
                return@launch
            }

            _isSaving.value = true
            try {
                // 1) Sube imagen si hay selección local
                val maybeUrl = persistImageIfNeededAndGetUrlOrNull(userId)

                // 2) Arma el perfil definitivo con la URL (si la hubo)
                val safeProfile = if (maybeUrl != null) {
                    _profile.value.copy(photoUrl = maybeUrl)
                } else {
                    _profile.value
                }

                // 3) Persiste perfil (con o sin foto nueva)
                val result = repository.updateProfile(userId, safeProfile)

                // 4) Sincroniza estado local con servidor y limpia flags
                _profile.value = result
                initialProfile = result.copy()

                _localImageUri.value = null
                _isImageChanged.value = false
                _hasUnsavedEdits.value = false

                _saveSuccess.value = true
                _emailErrorMessage.value = null
                _fieldErrors.value = FieldErrors()
                _errorMessage.value = null
                showErrors = false

                recomputeHasChanges()
            } catch (e: Exception) {
                _errorMessage.value = e.message
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }

    /** Permite a la UI “consumir” el evento de guardado exitoso. */
    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }

    // ---------------------------------------------------------------------
    // SUBIDA DE IMAGEN (FIREBASE STORAGE) + PERSISTENCIA DE URL EN BACKEND
    // ---------------------------------------------------------------------

    /**
     * Sube la imagen seleccionada a Firebase Storage y actualiza `photoUrl` con la URL pública.
     * Posteriormente persiste la URL en tu backend mediante `repository.updateProfile`.
     *
     * Nota: requiere que haya sesión en Firebase. Si no hay, hace sign-in anónimo.
     */
    fun uploadProfileImage(uri: Uri?) {
        if (uri == null) return

        Log.d("UploadDebug", "uploadProfileImage() llamado con uri = $uri")
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("UploadDebug", "UID Firebase actual: ${currentUser?.uid}")

        if (currentUser == null) {
            _errorMessage.value = "Usuario no logueado en Firebase. No se puede subir la imagen."
            return
        }

        viewModelScope.launch {
            try {
                _isUploadingPhoto.value = true
                _errorMessage.value = null

                // Asegura sesión activa (si entró aquí sin tenerla)
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                }

                // Ruta en Firebase Storage (organizado por userId del backend)
                val userId = _userId.value
                val ref = FirebaseStorage.getInstance()
                    .reference.child("users/$userId/profile.jpg")

                // Sube imagen
                ref.putFile(uri).await()

                // Obtiene URL pública y le añade un query param para bust de caché
                val downloadUrl = ref.downloadUrl.await().toString()
                val finalUrl = if (downloadUrl.contains("?")) {
                    "$downloadUrl&ts=${System.currentTimeMillis()}"
                } else {
                    "$downloadUrl?ts=${System.currentTimeMillis()}"
                }

                // Intenta persistir la URL en el backend
                try {
                    val updated = repository.updateProfile(
                        userId = userId,
                        _profile.value.copy(photoUrl = finalUrl)
                    )
                    // Estado persistido = actualizado por el servidor
                    _profile.value = updated
                    initialProfile = updated.copy()

                    // La foto ya está guardada en backend: NO hay cambios pendientes por la imagen
                    _localImageUri.value = null
                    _isImageChanged.value = false
                    _hasUnsavedEdits.value = false
                    recomputeHasChanges()
                } catch (e: Exception) {
                    _errorMessage.value =
                        "Subida OK pero no se pudo guardar la URL en el servidor: ${e.message}"
                    // Actualiza estado local (aunque haya fallado el guardado en backend)
                    _profile.value = _profile.value.copy(photoUrl = finalUrl)
                    _localImageUri.value = uri
                    _isImageChanged.value = true
                    _hasUnsavedEdits.value = true
                    recomputeHasChanges()
                }


            } catch (e: Exception) {
                _errorMessage.value = "No se pudo subir la imagen: ${e.message}"
            } finally {
                _isUploadingPhoto.value = false
            }
        }
    }

    // ---------------------------------------------------------------------
    // AUTENTICACIÓN EN FIREBASE (TOKEN PERSONALIZADO)
    // ---------------------------------------------------------------------

    /**
     * Cierra cualquier sesión previa y entra a Firebase con un token personalizado
     * que típicamente te provee tu backend (Custom Auth).
     */
    fun loginToFirebaseWithCustomToken(token: String) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
                FirebaseAuth.getInstance().signInWithCustomToken(token).await()
                Log.d("FirebaseAuth", "✅ UID en Firebase: ${FirebaseAuth.getInstance().currentUser?.uid}")
            } catch (e: Exception) {
                Log.e("FirebaseAuth", "❌ Error al loguear en Firebase: ${e.message}")
                _errorMessage.value = "Error al conectar con Firebase: ${e.message}"
            }
        }
    }

    // ---------------------------------------------------------------------
    // UTILIDADES DE UI
    // ---------------------------------------------------------------------

    /** Limpia el mensaje de error general (útil tras cerrar diálogos/snackbars). */
    fun clearError() {
        _errorMessage.value = null
    }
    // ---------------------------------------------------------------------
    // SESIÓN: limpiar estado al hacer logout o cambiar de usuario
    // ---------------------------------------------------------------------
    fun clearSessionState() {
        _userId.value = 0L
        _profile.value = UserProfileDto()
        initialProfile = null
        _localImageUri.value = null
        _isImageChanged.value = false
        _hasUnsavedEdits.value = false
        _hasChanges.value = false
        _fieldErrors.value = FieldErrors()
        _emailErrorMessage.value = null
        _errorMessage.value = null
    }
    /** El usuario ha elegido una imagen: solo la guardamos como selección local. */
    fun onPickLocalImage(uri: Uri?) {
        _localImageUri.value = uri
        _isImageChanged.value = (uri != null)
        _hasUnsavedEdits.value = (uri != null)
        recomputeHasChanges()
    }

    /** Descarta ediciones locales (para salir sin guardar). */
    fun discardEdits() {
        // Vuelve al snapshot inicial
        _profile.value = initialProfile?.copy() ?: UserProfileDto()
        _localImageUri.value = null
        _isImageChanged.value = false
        _hasUnsavedEdits.value = false
        _errorMessage.value = null
        _fieldErrors.value = FieldErrors()
        recomputeHasChanges()
    }
    fun removeSelectedImage() {
        _localImageUri.value = null
        _profile.value = _profile.value.copy(photoUrl = null)
        _isImageChanged.value = true
        _hasUnsavedEdits.value = true
        recomputeHasChanges()
    }


    /**
     * Sube la imagen local si existe y devuelve la URL final (con bust de caché), o null si no hay imagen nueva.
     * NO toca el estado de UI más allá del spinner; deja que updateProfile sincronice initialProfile y limpie flags.
     */
    private suspend fun persistImageIfNeededAndGetUrlOrNull(userId: Long): String? {
        val uri = _localImageUri.value ?: return null

        _isUploadingPhoto.value = true
        try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) auth.signInAnonymously().await()

            val ref = FirebaseStorage.getInstance()
                .reference.child("users/$userId/profile.jpg")

            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            return if (downloadUrl.contains("?")) {
                "$downloadUrl&ts=${System.currentTimeMillis()}"
            } else {
                "$downloadUrl?ts=${System.currentTimeMillis()}"
            }
        } finally {
            _isUploadingPhoto.value = false
        }
    }



}
