package com.example.yourroom.repository

import com.example.yourroom.model.UserProfileDto
import com.example.yourroom.network.UserApiService
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApiService
) {
    suspend fun getProfile(userId: Long): UserProfileDto {
        return api.getUserProfile(userId)
    }

    suspend fun updateProfile(userId: Long, profile: UserProfileDto): UserProfileDto {
        return api.updateUserProfile(userId, profile)
    }

}

