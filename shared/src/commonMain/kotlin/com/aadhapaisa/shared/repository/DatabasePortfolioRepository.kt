package com.aadhapaisa.shared.repository

import com.aadhapaisa.shared.database.DatabaseDriverFactory
import com.aadhapaisa.shared.database.PortfolioDatabase
import com.aadhapaisa.shared.database.Holding as DatabaseHolding
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.models.PortfolioSummary
import com.aadhapaisa.shared.models.ConsolidatedHolding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Instant
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers 

class DatabasePortfolioRepository(
    private val driverFactory: DatabaseDriverFactory
) : PortfolioRepository {
    private val database = PortfolioDatabase(driverFactory.createDriver())
    private val _isInitialized = MutableStateFlow(false)
    private val _holdings = MutableStateFlow<List<Holding>>(emptyList())
    
    fun initialize() {
        if (_isInitialized.value) return
        
        println("🔧 DatabasePortfolioRepository: Initializing database...")
        // Create tables if they don't exist
        database.portfolioDatabaseQueries.selectAll()
        _isInitialized.value = true
        
        // Load initial data
        refreshHoldings()
        
        println("✅ DatabasePortfolioRepository: Database initialized successfully")
    }
    
    private fun refreshHoldings() {
        try {
            val dbHoldings = database.portfolioDatabaseQueries.selectAll().executeAsList()
            val holdings = dbHoldings.map { it.toHolding() }
            
            println("📊 DatabasePortfolioRepository: Refreshed holdings from database:")
            holdings.forEach { holding ->
                println("  - ${holding.stockSymbol}: ₹${holding.currentPrice}")
            }
            
            // Update the StateFlow to trigger UI refresh
            _holdings.value = holdings
            println("🔄 DatabasePortfolioRepository: StateFlow updated with ${holdings.size} holdings")
            
        } catch (e: Exception) {
            println("❌ DatabasePortfolioRepository: Error refreshing holdings: ${e.message}")
        }
    }
    
    override fun getHoldings(): Flow<List<Holding>> = _holdings.asStateFlow()
    
    override fun getConsolidatedHoldings(): Flow<List<ConsolidatedHolding>> {
        return getHoldings().map { holdings ->
            // Group holdings by stock symbol and consolidate them
            holdings.groupBy { it.stockSymbol }
                .map { (symbol, stockHoldings) ->
                    val firstHolding = stockHoldings.first()
                    val totalQuantity = stockHoldings.sumOf { it.quantity }
                    val totalInvestedValue = stockHoldings.sumOf { it.investedValue }
                    val avgBuyPrice = stockHoldings.sumOf { it.buyPrice * it.quantity } / totalQuantity
                    val firstPurchaseDate = stockHoldings.minByOrNull { it.purchaseDate }?.purchaseDate ?: firstHolding.purchaseDate
                    val lastPurchaseDate = stockHoldings.maxByOrNull { it.purchaseDate }?.purchaseDate ?: firstHolding.purchaseDate
                    val currentPrice = firstHolding.currentPrice
                    val totalCurrentValue = totalQuantity * currentPrice
                    val totalProfitLoss = totalCurrentValue - totalInvestedValue
                    val totalProfitLossPercent = if (totalInvestedValue > 0) (totalProfitLoss / totalInvestedValue) * 100 else 0.0
                    // Calculate days held from first purchase date to today
                    val now = kotlinx.datetime.Clock.System.now()
                    val daysHeld = (now.epochSeconds - firstPurchaseDate.epochSeconds) / 86400
                    val totalDayChange = stockHoldings.sumOf { it.dayChange }
                    val avgDayChangePercent = stockHoldings.map { it.dayChangePercent }.average()
                    
                    ConsolidatedHolding(
                        stockSymbol = symbol,
                        stockName = firstHolding.stockName,
                        totalQuantity = totalQuantity,
                        totalInvestedValue = totalInvestedValue,
                        avgBuyPrice = avgBuyPrice,
                        firstPurchaseDate = firstPurchaseDate,
                        lastPurchaseDate = lastPurchaseDate,
                        currentPrice = currentPrice,
                        totalCurrentValue = totalCurrentValue,
                        totalProfitLoss = totalProfitLoss,
                        totalProfitLossPercent = totalProfitLossPercent,
                        avgDaysHeld = daysHeld.toInt(),
                        totalDayChange = totalDayChange,
                        avgDayChangePercent = avgDayChangePercent
                    )
                }
                .sortedByDescending { it.firstPurchaseDate }
        }
    }
    
    override fun getPortfolioSummary(): Flow<PortfolioSummary> = 
        getConsolidatedHoldings().map { consolidatedHoldings ->
            val totalInvested = consolidatedHoldings.sumOf { it.dynamicInvestedValue }
            val currentValue = consolidatedHoldings.sumOf { it.dynamicCurrentValue }
            val profitLoss = currentValue - totalInvested
            val profitLossPercent = if (totalInvested > 0) (profitLoss / totalInvested) * 100 else 0.0
            
            val positiveStocks = consolidatedHoldings.count { it.dynamicProfitLoss >= 0 }
            val negativeStocks = consolidatedHoldings.count { it.dynamicProfitLoss < 0 }
            
            PortfolioSummary(
                totalInvested = totalInvested,
                currentValue = currentValue,
                profitLoss = profitLoss,
                profitLossPercent = profitLossPercent,
                totalStocks = consolidatedHoldings.size,
                positiveStocks = positiveStocks,
                negativeStocks = negativeStocks
            )
        }
    
    override fun getRecentPurchases(limit: Int): Flow<List<Holding>> = 
        _holdings.asStateFlow().map { holdings ->
            holdings.sortedByDescending { it.purchaseDate }.take(limit)
        }
    
    override fun getPositiveHoldings(): Flow<List<Holding>> = 
        _holdings.asStateFlow().map { holdings ->
            holdings.filter { it.profitLoss >= 0 }
        }
    
    override fun getNegativeHoldings(): Flow<List<Holding>> = 
        _holdings.asStateFlow().map { holdings ->
            holdings.filter { it.profitLoss < 0 }
        }
    
    override suspend fun addHolding(holding: Holding) {
        println("📝 DatabasePortfolioRepository: Adding holding - ${holding.stockSymbol}")
        
        database.portfolioDatabaseQueries.insertHolding(
            stock_symbol = holding.stockSymbol,
            stock_name = holding.stockName,
            quantity = holding.quantity.toLong(),
            buy_price = holding.buyPrice,
            purchase_date = holding.purchaseDate.epochSeconds,
            current_price = holding.currentPrice,
            current_value = holding.currentValue,
            invested_value = holding.investedValue,
            profit_loss = holding.profitLoss,
            profit_loss_percent = holding.profitLossPercent,
            days_held = holding.daysHeld.toLong(),
            day_change = holding.dayChange,
            day_change_percent = holding.dayChangePercent
        )
        
        println("✅ DatabasePortfolioRepository: Holding added successfully")
        
        // Refresh holdings from database to trigger UI update
        refreshHoldings()
        println("🔄 DatabasePortfolioRepository: Holdings refreshed after adding new holding")
    }
    
    override suspend fun updateHolding(holding: Holding) {
        println("💾 DatabasePortfolioRepository: ===== STEP 3: STORE TO DATABASE =====")
        println("💾 DatabasePortfolioRepository: Symbol: ${holding.stockSymbol}")
        println("💾 DatabasePortfolioRepository: STORING VALUES:")
        println("  - Current Price: ₹${holding.currentPrice}")
        println("  - Current Value: ₹${holding.dynamicCurrentValue}")
        println("  - Invested Value: ₹${holding.dynamicInvestedValue}")
        println("  - Profit/Loss: ₹${holding.dynamicProfitLoss}")
        
        database.portfolioDatabaseQueries.updateHolding(
            stock_name = holding.stockName,
            quantity = holding.quantity.toLong(),
            buy_price = holding.buyPrice,
            purchase_date = holding.purchaseDate.epochSeconds,
            current_price = holding.currentPrice,
            current_value = holding.dynamicCurrentValue,
            invested_value = holding.dynamicInvestedValue,
            profit_loss = holding.dynamicProfitLoss,
            profit_loss_percent = holding.dynamicProfitLossPercent,
            days_held = holding.daysHeld.toLong(),
            day_change = holding.dayChange,
            day_change_percent = holding.dayChangePercent,
            stock_symbol = holding.stockSymbol
        )
        
        println("✅ DatabasePortfolioRepository: DATABASE UPDATE COMPLETED")
        
        // Step 5: Debug cursor - Read back from database to verify
        println("🔍 DatabasePortfolioRepository: ===== STEP 5: DEBUG CURSOR =====")
        val updatedHoldings = database.portfolioDatabaseQueries.selectAll().executeAsList()
        println("🔍 DatabasePortfolioRepository: DATABASE CONTAINS ${updatedHoldings.size} holdings:")
        updatedHoldings.forEach { dbHolding ->
            println("  - ${dbHolding.stock_symbol}: Price=₹${dbHolding.current_price}, Value=₹${dbHolding.current_value}")
        }
        
        println("💾 DatabasePortfolioRepository: ===== STEP 3 COMPLETE =====")
        
        // Refresh holdings from database to trigger UI update
        refreshHoldings()
        println("🔄 DatabasePortfolioRepository: Holdings refreshed from database")
    }
    
    override suspend fun removeHolding(stockSymbol: String) {
        println("📝 DatabasePortfolioRepository: Removing holding - $stockSymbol")
        
        database.portfolioDatabaseQueries.deleteHolding(stockSymbol)
        
        println("✅ DatabasePortfolioRepository: Holding removed successfully")
        
        // Refresh holdings from database to trigger UI update
        refreshHoldings()
        println("🔄 DatabasePortfolioRepository: Holdings refreshed after removing holding")
    }
    
    override fun clearAllData() {
        println("🗑️ DatabasePortfolioRepository: Clearing all data...")
        
        // Delete all holdings from the database
        database.portfolioDatabaseQueries.deleteAllHoldings()
        
        println("✅ DatabasePortfolioRepository: All data cleared successfully")
        
        // Refresh holdings from database to trigger UI update
        refreshHoldings()
        println("🔄 DatabasePortfolioRepository: Holdings refreshed after clearing all data")
    }
    
    /**
     * Public method to manually refresh holdings from database
     */
    fun refreshHoldingsFromDatabase() {
        println("🔄 DatabasePortfolioRepository: Manual refresh requested")
        refreshHoldings()
    }
}

// Extension function to convert database entity to domain model
private fun DatabaseHolding.toHolding(): Holding {
    val holding = Holding(
        stockSymbol = stock_symbol,
        stockName = stock_name,
        quantity = quantity.toInt(),
        buyPrice = buy_price,
        purchaseDate = Instant.fromEpochSeconds(purchase_date),
        currentPrice = current_price,
        currentValue = current_value,
        investedValue = invested_value,
        profitLoss = profit_loss,
        profitLossPercent = profit_loss_percent,
        daysHeld = days_held.toInt(),
        dayChange = day_change,
        dayChangePercent = day_change_percent
    )
    
    // Recalculate days held dynamically
    return holding.calculateMetrics()
}

