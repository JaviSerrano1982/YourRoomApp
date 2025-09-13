package com.example.yourroom.model

import java.math.BigDecimal


// Petición de básicos (coincide con backend)
data class SpaceBasicsRequest(
    val ownerId: Long? = null, // MVP: lo pasamos hasta que el backend lo tome del token
    val title: String,
    val location: String? = null,
    val addressLine: String? = null,
    val capacity: Int? = null,
    val hourlyPrice: Double? = null
)


// Petición de detalles
data class SpaceDetailsRequest(
    val sizeM2: Int? = null,
    val availability: String? = null,
    val services: String? = null,
    val description: String? = null
)


// Respuesta común del backend
data class SpaceResponse(
    val id: Long,
    val ownerId: Long,
    val status: String,
    val title: String?,
    val location: String?,
    val addressLine: String?,
    val capacity: Int?,
    val hourlyPrice: BigDecimal?,
    val sizeM2: Int?,
    val availability: String?,
    val services: String?,
    val description: String?
)