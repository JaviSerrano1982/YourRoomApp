package com.example.yourroom.model



data class PhotoResponse(
    val id: Long,
    val spaceId: Long,
    val url: String,
    val primary: Boolean
)
