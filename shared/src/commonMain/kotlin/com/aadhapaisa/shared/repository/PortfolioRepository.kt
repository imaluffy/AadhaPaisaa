package com.aadhapaisa.shared.repository

import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.models.PortfolioSummary
import com.aadhapaisa.shared.models.ConsolidatedHolding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PortfolioRepository {
    fun getHoldings(): Flow<List<Holding>>
    fun getConsolidatedHoldings(): Flow<List<ConsolidatedHolding>>
    fun getPortfolioSummary(): Flow<PortfolioSummary>
    fun getRecentPurchases(limit: Int = 5): Flow<List<Holding>>
    fun getPositiveHoldings(): Flow<List<Holding>>
    fun getNegativeHoldings(): Flow<List<Holding>>
    suspend fun addHolding(holding: Holding)
    suspend fun updateHolding(holding: Holding)
    suspend fun removeHolding(stockSymbol: String)
    fun clearAllData()
}

// DatabasePortfolioRepository removed - using PersistentPortfolioRepository instead
