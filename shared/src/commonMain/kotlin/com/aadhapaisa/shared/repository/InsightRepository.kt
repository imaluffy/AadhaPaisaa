package com.aadhapaisa.shared.repository

import com.aadhapaisa.shared.models.Insight
import com.aadhapaisa.shared.models.InsightCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface InsightRepository {
    fun getInsights(): Flow<List<Insight>>
    fun getInsightsByCategory(category: InsightCategory): Flow<List<Insight>>
}

class MockInsightRepository : InsightRepository {
    private val mockInsights = listOf<Insight>()

    override fun getInsights(): Flow<List<Insight>> = flow {
        emit(mockInsights)
    }

    override fun getInsightsByCategory(category: InsightCategory): Flow<List<Insight>> = flow {
        val filtered = mockInsights.filter { it.category == category }
        emit(filtered)
    }
}
