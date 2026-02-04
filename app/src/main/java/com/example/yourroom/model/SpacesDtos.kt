package com.example.yourroom.model

import java.math.BigDecimal

// ------------------------------
// MODELOS: SpaceRequests & SpaceResponse
// ------------------------------

/**
 * Petición con datos básicos de la sala.
 *
 * Coincide con lo esperado por el backend.
 *
 * @param ownerId      Id del propietario (MVP: se envía, aunque luego debería tomarse del token).
 * @param title        Título de la sala.
 * @param location     Ciudad o localidad de la sala.
 * @param addressLine  Dirección detallada.
 * @param capacity     Capacidad máxima de personas.
 * @param hourlyPrice  Precio por hora.
 */
data class SpaceBasicsRequest(
    val ownerId: Long? = null, // MVP: lo pasamos hasta que el backend lo tome del token
    val title: String,
    val location: String? = null,
    val addressLine: String? = null,
    val capacity: Int? = null,
    val hourlyPrice: Double? = null
)

/**
 * Petición con datos de detalle de la sala.
 *
 * @param sizeM2       Tamaño en metros cuadrados.
 * @param availability Disponibilidad horaria o calendario.
 * @param services     Servicios adicionales ofrecidos.
 * @param description  Descripción libre de la sala.
 */
data class SpaceDetailsRequest(
    val sizeM2: Int? = null,
    val availability: String? = null,
    val services: String? = null,
    val description: String? = null
)

/**
 * Respuesta común que devuelve el backend al consultar o crear una sala.
 *
 * @param id           Identificador único de la sala.
 * @param ownerId      Id del propietario de la sala.
 * @param status       Estado de la sala (ej: activa, pendiente, etc.).
 * @param title        Título de la sala.
 * @param location     Ciudad o localidad.
 * @param addressLine  Dirección detallada.
 * @param capacity     Capacidad máxima.
 * @param hourlyPrice  Precio por hora (formato BigDecimal).
 * @param sizeM2       Tamaño en m².
 * @param availability Disponibilidad horaria.
 * @param services     Servicios disponibles.
 * @param description  Descripción completa.
 */
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
    val description: String?,
    val primaryPhotoUrl: String? = null
)
