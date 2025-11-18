package com.example.yourroom.ui.screens.home

import com.example.yourroom.model.SpaceResponse

/**
 * Representa el estado de la pantalla de búsqueda de salas.
 * Se usa en el ViewModel para que la UI pueda observar cambios
 * (texto de búsqueda, resultados, carga, errores, etc.).
 */
data class SearchSpacesUiState(
    val query: String = "",//Texto introducido en buscar
    val isLoading: Boolean = false, // Indica si se están cargando los datos desde el backend.
    val allSpaces: List<SpaceResponse> = emptyList(),// Lista completa de salas recibidas desde el servidor.
    val filteredSpaces: List<SpaceResponse> = emptyList(),// Lista de salas filtradas según el texto introducido en `query`.
    val errorMessage: String? = null
)
