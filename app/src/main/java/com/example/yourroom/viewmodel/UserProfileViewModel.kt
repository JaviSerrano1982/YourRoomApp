package com.example.yourroom.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.model.UserProfileDto
import com.example.yourroom.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _profile = MutableStateFlow(UserProfileDto())
    val profile: StateFlow<UserProfileDto> = _profile

    fun loadProfile(userId: Long) {
        viewModelScope.launch {
            if (userId <= 0) {
                Log.w("Perfil", "⛔ userId inválido, no se carga perfil")
                return@launch
            }
            try {
                val profile = repository.getProfile(userId)
                _profile.value = profile
            } catch (e: Exception) {
                Log.e("Perfil", "❌ Error al cargar perfil: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    fun updateProfile(userId: Long) {
        viewModelScope.launch {
            try {
                println("🚀 Lanzando updateProfile con userId = $userId")

                val safeProfile = _profile.value.copy(
                    photoUrl = _profile.value.photoUrl.ifBlank {
                        "https://via.placeholder.com/150"
                    }
                )

                // 🔍 Imprimir el JSON como cadena para copiar y probar
                println("📤 JSON enviado: $safeProfile")

                val result = repository.updateProfile(userId, safeProfile)
                println("✅ Perfil actualizado: $result")

                _profile.value = result
            } catch (e: Exception) {
                println("❌ Error al actualizar perfil: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    fun updateField(update: UserProfileDto.() -> UserProfileDto) {
        _profile.value = _profile.value.update()
    }
}
