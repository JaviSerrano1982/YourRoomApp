package com.example.yourroom.model

data class UserProfileDto(
    val firstName: String = "",
    val lastName: String = "",
    val location: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String = ""
)
