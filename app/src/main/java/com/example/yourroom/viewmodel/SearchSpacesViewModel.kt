package com.example.yourroom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.repository.SpaceRepository
import com.example.yourroom.ui.screens.home.SearchSpacesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchSpacesViewModel @Inject constructor(
    private val spaceRepo: SpaceRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(SearchSpacesUiState())
    val ui: StateFlow<SearchSpacesUiState> = _ui

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    private var searchJob: Job? = null

    init {
        // carga inicial: muestra “lo último publicado” con q = ""
        runSearch("")
    }

    fun onQueryChange(newQuery: String) {
        _ui.value = _ui.value.copy(query = newQuery, errorMessage = null)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            runSearch(newQuery.trim())
        }
    }

    private fun runSearch(q: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, errorMessage = null)
            try {
                val results = spaceRepo.searchSpaces(q)
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    allSpaces = results,
                    filteredSpaces = results
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al buscar salas"
                )
            }
        }
    }
    fun toggleFavorite(spaceId: Long) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(spaceId)) current.remove(spaceId) else current.add(spaceId)
        _favoriteIds.value = current
    }
}
