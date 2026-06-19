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

    private val _selectedMonth = MutableStateFlow<String?>(null)
    val selectedMonth: StateFlow<String?> = _selectedMonth

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre

    val genres = listOf("Мюзикл", "Оперетта", "Комедия", "Сказка", "Драма")

    private val monthMap = mapOf(
        "ЯНВАРЯ" to "Январь",
        "ФЕВРАЛЯ" to "Февраль",
        "МАРТА" to "Март",
        "АПРЕЛЯ" to "Апрель",
        "МАЯ" to "Май",
        "ИЮНЯ" to "Июнь",
        "ИЮЛЯ" to "Июль",
        "АВГУСТА" to "Август",
        "СЕНТЯБРЯ" to "Сентябрь",
        "ОКТЯБРЯ" to "Октябрь",
        "НОЯБРЯ" to "Ноябрь",
        "ДЕКАБРЯ" to "Декабрь"
    )

    val availableMonths: StateFlow<List<String>> = _posters.map { posters ->
        posters.mapNotNull { item ->
            val parts = item.date.split(" ")
            if (parts.size > 1) {
                val genitiveMonth = parts[1].replace(",", "").uppercase()
                monthMap[genitiveMonth] ?: genitiveMonth
            } else null
        }.distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPosters: StateFlow<List<AppItem>> = combine(
        _posters, _searchQuery, _selectedMonth, _selectedGenre
    ) { posters, query, month, genre ->
        var result = posters
        
        // 1. Поиск (по заголовку и описанию)
        if (query.isNotEmpty()) {
            result = result.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true)
            }
        }
        
        // 2. Фильтр по месяцу (учитываем падежи)
        if (month != null) {
            // Находим исходный родительный падеж для поиска в строке даты
            val genitiveSearch = monthMap.entries.find { it.value == month }?.key ?: month.uppercase()
            result = result.filter { it.date.uppercase().contains(genitiveSearch) }
        }
        
        // 3. Фильтр по жанру (максимально гибкий поиск)
        if (genre != null) {
            val searchGenre = genre.lowercase().trim()
            result = result.filter { 
                val title = it.title.lowercase()
                val desc = it.description.lowercase()
                
                title.contains(searchGenre) || 
                desc.contains(searchGenre) ||
                // Дополнительные проверки для сложных слов
                (searchGenre == "комедия" && (title.contains("комическ") || desc.contains("комическ"))) ||
                (searchGenre == "мюзикл" && (title.contains("musical") || desc.contains("musical")))
            }
        }
        
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPosters()
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleMonth(month: String) {
        _selectedMonth.value = if (_selectedMonth.value == month) null else month
    }

    fun toggleGenre(genre: String) {
        _selectedGenre.value = if (_selectedGenre.value == genre) null else genre
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
