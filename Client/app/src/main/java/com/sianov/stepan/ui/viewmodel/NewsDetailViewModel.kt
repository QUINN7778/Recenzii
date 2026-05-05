package com.sianov.stepan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sianov.stepan.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    val repository: AppRepository
) : ViewModel() {

    private val _content = MutableStateFlow<String?>(null)
    val content: StateFlow<String?> = _content

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadNewsDetail(url: String) {
        if (!_content.value.isNullOrEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            _content.value = repository.getNewsDetail(url)
            _isLoading.value = false
        }
    }
}
