package com.example.yourroom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourroom.datastore.FavoriteSpacesStore
import com.example.yourroom.repository.SpaceRepository
import com.example.yourroom.ui.screens.home.SearchSpacesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchSpacesViewModel @Inject constructor(
    private val spaceRepo: SpaceRepository,
    private val favoriteStore: FavoriteSpacesStore
) : ViewModel() {

    private val _ui = MutableStateFlow(SearchSpacesUiState())
    val ui: StateFlow<SearchSpacesUiState> = _ui

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    private var searchJob: Job? = null

    init {
        // 1) Cargar favoritos persistidos
        viewModelScope.launch {
            favoriteStore.favoriteIds.collectLatest { ids ->
                _favoriteIds.value = ids
            }
        }

        // 2) carga inicial
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
        viewModelScope.launch {
            favoriteStore.toggle(spaceId)

        }
        }
}
