package com.example.yourroom.viewmodel

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
            _profile.value = repository.getProfile(userId)
        }
    }

    fun updateProfile(userId: Long) {
        viewModelScope.launch {
            repository.updateProfile(userId, _profile.value)
        }
    }

    fun updateField(update: UserProfileDto.() -> UserProfileDto) {
        _profile.value = _profile.value.update()
    }
}
