package com.aadhapaisa.shared.viewmodel

import com.aadhapaisa.shared.models.Insight
import com.aadhapaisa.shared.models.NewsItem
import com.aadhapaisa.shared.repository.InsightRepository
import com.aadhapaisa.shared.repository.NewsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InsightsViewModel(
    private val insightRepository: InsightRepository,
    private val newsRepository: NewsRepository
) {
    private val _insights = MutableStateFlow<List<Insight>>(emptyList())
    val insights: StateFlow<List<Insight>> = _insights.asStateFlow()

    private val _news = MutableStateFlow<List<NewsItem>>(emptyList())
    val news: StateFlow<List<NewsItem>> = _news.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Load insights
                insightRepository.getInsights().collect { insights ->
                    _insights.value = insights
                }

                // Load news
                newsRepository.getNews().collect { news ->
                    _news.value = news
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshNews() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                _isRefreshing.value = true
                _error.value = null

                newsRepository.refreshNews().collect { news ->
                    _news.value = news
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to refresh news"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

