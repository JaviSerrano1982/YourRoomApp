package com.example.yourroom.model

// ------------------------------
// MODELO: PhotoRequest
// ------------------------------

/**
 * Modelo de datos para representar una foto en una sala.
 *
 * @param url     Dirección de la imagen.
 * @param primary Indica si la foto es la principal de la sala.
 */
data class PhotoRequest(
    val url: String,
    val primary: Boolean
)
