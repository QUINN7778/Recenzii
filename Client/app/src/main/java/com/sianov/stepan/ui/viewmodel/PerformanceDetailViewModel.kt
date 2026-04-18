package com.sianov.stepan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sianov.stepan.data.model.PerformanceDetail
import com.sianov.stepan.data.model.ReviewRequest
import com.sianov.stepan.data.model.ReviewResponse
import com.sianov.stepan.data.repository.AppRepository
import com.sianov.stepan.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerformanceDetailViewModel @Inject constructor(
    val repository: AppRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _detail = MutableStateFlow<PerformanceDetail?>(null)
    val detail: StateFlow<PerformanceDetail?> = _detail

    private val _reviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val reviews: StateFlow<List<ReviewResponse>> = _reviews

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun loadDetail(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 1. Сначала получаем детали (из кэша или с сайта). 
            // Благодаря кэшу это теперь должно быть мгновенно.
            val performanceDetail = repository.getPerformanceDetail(url)
            _detail.value = performanceDetail
            
            // Сразу выключаем индикатор загрузки, как только получили детали!
            // Не ждем серверных дел.
            _isLoading.value = false
            
            // 2. А отзывы и синхронизацию запускаем параллельно в фоне, 
            // чтобы они не тормозили показ основной инфы.
            if (performanceDetail != null) {
                launch { syncWithServer(performanceDetail) }
                launch { loadReviews(url) }
            }
        }
    }

    private suspend fun syncWithServer(performance: PerformanceDetail) {
        val token = authRepository.authToken.firstOrNull() ?: return
        try {
            repository.apiService.syncPerformance(
                token = "Token $token",
                data = mapOf(
                    "url" to performance.detailUrl,
                    "title" to performance.title,
                    "image_url" to performance.imageUrl,
                    "description" to performance.description
                )
            )
        } catch (e: Exception) {
            // Ошибка может быть, если сервер недоступен или токен протух
        }
    }

    fun loadReviews(url: String) {
        viewModelScope.launch {
            try {
                val response = repository.apiService.getReviews(url)
                _reviews.value = response
            } catch (e: Exception) {
                // Игнорируем ошибки загрузки отзывов
            }
        }
    }

    fun addReview(url: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val token = authRepository.authToken.firstOrNull()
            if (token.isNullOrEmpty()) {
                _message.value = "Ошибка: вы не авторизованы"
                return@launch
            }
            
            // На всякий случай пробуем синхронизировать перед отзывом
            _detail.value?.let { syncWithServer(it) }
            
            try {
                val response = repository.apiService.addReview(
                    token = "Token $token",
                    request = ReviewRequest(
                        url = url,
                        rating = rating,
                        comment = comment
                    )
                )
                
                if (response.isSuccessful) {
                    _message.value = "Отзыв опубликован!"
                    loadReviews(url)
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    if (errorBody.contains("not synced")) {
                        _message.value = "Ошибка: спектакль еще не в базе сервера."
                    } else {
                        _message.value = "Сервер вернул ошибку: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _message.value = "Сетевая ошибка: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
