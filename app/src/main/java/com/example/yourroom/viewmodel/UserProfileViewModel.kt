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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    fun initProfile(context: Context) {
        viewModelScope.launch {
            val prefs = UserPreferences(context)
            val storedId = prefs.userIdFlow.first()
            _userId.value = storedId
            Log.d("Perfil", "✅ userId cargado al entrar: $storedId")

            if (storedId > 0) {
                loadProfile(storedId)
            } else {
                Log.w("Perfil", "⚠️ userId inválido al cargar perfil")
            }
        }
    }

    fun setLocalImage(uri: Uri?) {
        _localImageUri.value = uri
        uri?.let {
            updateField { copy(photoUrl = it.toString()) }
            _isImageChanged.value = true
        }
    }

    fun clearImageChange() {
        _isImageChanged.value = false
    }

    fun loadProfile(userId: Long) {
        viewModelScope.launch {
            if (userId <= 0) return@launch
            try {
                val profile = repository.getProfile(userId)
                _profile.value = profile
                _hasChanges.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                e.printStackTrace()
            }
        }
    }

    fun updateProfile(userId: Long) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val safeProfile = _profile.value
                val result = repository.updateProfile(userId, safeProfile)
                _profile.value = result
                _hasChanges.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateField(update: UserProfileDto.() -> UserProfileDto) {
        _profile.value = _profile.value.update()
        _hasChanges.value = true
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
