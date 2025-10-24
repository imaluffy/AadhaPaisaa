package com.aadhapaisa.shared.repository

import com.aadhapaisa.shared.models.Stock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface StockRepository {
    fun getAllStocks(): Flow<List<Stock>>
    fun getStockBySymbol(symbol: String): Flow<Stock?>
    fun searchStocks(query: String): Flow<List<Stock>>
}

class MockStockRepository : StockRepository {
    private val mockStocks = listOf(
        Stock("RELIANCE", "Reliance Industries", 2456.50, 2.35, "Energy", 1650000000000L, 2500000L),
        Stock("TCS", "Tata Consultancy Services", 3456.75, -1.25, "Technology", 1250000000000L, 1800000L),
        Stock("HDFC", "HDFC Bank", 1567.25, 0.85, "Banking", 950000000000L, 3200000L),
        Stock("INFY", "Infosys", 1456.80, 1.45, "Technology", 600000000000L, 2100000L),
        Stock("ICICIBANK", "ICICI Bank", 987.45, -0.65, "Banking", 650000000000L, 4500000L),
        Stock("BHARTIARTL", "Bharti Airtel", 856.30, 3.25, "Telecom", 480000000000L, 3800000L),
        Stock("ITC", "ITC Limited", 456.75, 0.95, "FMCG", 550000000000L, 5200000L),
        Stock("SBIN", "State Bank of India", 567.25, -1.15, "Banking", 500000000000L, 6800000L),
        Stock("WIPRO", "Wipro Limited", 456.80, 2.15, "Technology", 250000000000L, 1900000L),
        Stock("HINDUNILVR", "Hindustan Unilever", 2345.60, 1.25, "FMCG", 550000000000L, 1200000L)
    )

    override fun getAllStocks(): Flow<List<Stock>> = flow {
        emit(mockStocks)
    }

    override fun getStockBySymbol(symbol: String): Flow<Stock?> = flow {
        emit(mockStocks.find { it.symbol.equals(symbol, ignoreCase = true) })
    }

    override fun searchStocks(query: String): Flow<List<Stock>> = flow {
        val filtered = mockStocks.filter { 
            it.symbol.contains(query, ignoreCase = true) || 
            it.name.contains(query, ignoreCase = true) ||
            it.sector.contains(query, ignoreCase = true)
        }
        emit(filtered)
    }
}


