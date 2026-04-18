package com.sianov.stepan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sianov.stepan.data.model.AppItem
import com.sianov.stepan.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PosterViewModel @Inject constructor(
    val repository: AppRepository
) : ViewModel() {

    private val _posters = MutableStateFlow<List<AppItem>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredPosters: StateFlow<List<AppItem>> = combine(_posters, _searchQuery) { posters, query ->
        if (query.isEmpty()) {
            posters
        } else {
            posters.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPosters()
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun loadPosters() {
        viewModelScope.launch {
            _isLoading.value = true
            val posters = repository.getPosters()
            _posters.value = posters
            _isLoading.value = false

            // ФОНОВАЯ ПРЕДЗАГРУЗКА:
            // Как только получили список, начинаем качать детали каждого спектакля в кэш.
            // Пользователь этого не видит, но карточки потом откроются мгновенно.
            posters.forEach { item ->
                if (item.detailUrl.isNotEmpty()) {
                    launch { repository.getPerformanceDetail(item.detailUrl) }
                }
            }
        }
    }
}
