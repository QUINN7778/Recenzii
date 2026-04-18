package com.sianov.stepan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sianov.stepan.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val user = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    val favorites = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
        
    val reminders = repository.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
        
    val visited = repository.visited
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun login(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val success = repository.login(username, email, password)
            if (!success) {
                _error.value = "Ошибка входа. Проверьте логин/пароль или интернет."
            }
            _isLoading.value = false
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val success = repository.register(username, email, password)
            if (!success) {
                _error.value = "Ошибка регистрации. Возможно, логин уже занят."
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
    
    fun toggleFavorite(url: String) {
        viewModelScope.launch {
            repository.toggleFavorite(url)
        }
    }
    
    fun toggleReminder(url: String) {
        viewModelScope.launch {
            repository.toggleReminder(url)
        }
    }
    
    fun toggleVisited(url: String) {
        viewModelScope.launch {
            repository.toggleVisited(url)
        }
    }
}
