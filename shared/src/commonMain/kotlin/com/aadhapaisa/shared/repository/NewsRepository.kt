package com.aadhapaisa.shared.repository

import com.aadhapaisa.shared.models.NewsItem
import com.aadhapaisa.shared.models.NewsSentiment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface NewsRepository {
    fun getNews(): Flow<List<NewsItem>>
    fun getNewsForStock(symbol: String): Flow<List<NewsItem>>
    fun refreshNews(): Flow<List<NewsItem>>
}

class MockNewsRepository : NewsRepository {
    private val mockNews = listOf<NewsItem>()

    override fun getNews(): Flow<List<NewsItem>> = flow {
        emit(mockNews.sortedByDescending { it.publishedAt })
    }

    override fun getNewsForStock(symbol: String): Flow<List<NewsItem>> = flow {
        val filtered = mockNews.filter { it.relatedStocks.contains(symbol) }
        emit(filtered.sortedByDescending { it.publishedAt })
    }

    override fun refreshNews(): Flow<List<NewsItem>> = flow {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)
        emit(mockNews.sortedByDescending { it.publishedAt })
    }
}
