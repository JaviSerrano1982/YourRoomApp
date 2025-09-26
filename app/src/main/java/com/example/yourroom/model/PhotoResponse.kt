package com.example.yourroom.model

// ------------------------------
// MODELO: PhotoResponse
// ------------------------------

/**
 * Modelo de datos que representa la respuesta de una foto recibida desde el backend.
 *
 * @param id       Identificador único de la foto.
 * @param spaceId  Identificador de la sala a la que pertenece la foto.
 * @param url      Dirección de la imagen.
 * @param primary  Indica si la foto es la principal de la sala.
 */
data class PhotoResponse(
    val id: Long,
    val spaceId: Long,
    val url: String,
    val primary: Boolean
)
