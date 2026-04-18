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
class NewsViewModel @Inject constructor(
    val repository: AppRepository
) : ViewModel() {

    private val _news = MutableStateFlow<List<AppItem>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredNews: StateFlow<List<AppItem>> = combine(_news, _searchQuery) { news, query ->
        if (query.isEmpty()) {
            news
        } else {
            news.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadNews()
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun loadNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _news.value = repository.getNews()
            _isLoading.value = false
        }
    }
}
