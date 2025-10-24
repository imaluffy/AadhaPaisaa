package com.aadhapaisa.shared.service

import com.aadhapaisa.shared.api.StockSearchService
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.repository.PortfolioRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock

/**
 * Service responsible for updating market prices for all holdings every 10 minutes
 */
class MarketPriceUpdateService(
    private val portfolioRepository: PortfolioRepository,
    private val stockSearchService: StockSearchService
) {
    private var updateJob: Job? = null
    private val updateInterval = 1.days // Changed to 1 day since markets are closed
    
    // State for tracking update status
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()
    
    private val _lastUpdateTime = MutableStateFlow<kotlinx.datetime.Instant?>(null)
    val lastUpdateTime: StateFlow<kotlinx.datetime.Instant?> = _lastUpdateTime.asStateFlow()
    
    private val _updateCount = MutableStateFlow(0)
    val updateCount: StateFlow<Int> = _updateCount.asStateFlow()
    
    // Toast messages
    private val _toastMessages = MutableSharedFlow<String>()
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()
    
    /**
     * Start the periodic price update service
     */
    fun startPriceUpdates() {
        if (updateJob?.isActive == true) {
            println("🔄 MarketPriceUpdateService: Price updates already running")
            return
        }
        
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            println("🚀 MarketPriceUpdateService: Starting periodic price updates every $updateInterval")
            
            // Do an immediate update first
            try {
                println("🔄 MarketPriceUpdateService: Performing initial price update")
                updateAllHoldingsPrices()
            } catch (e: Exception) {
                println("❌ MarketPriceUpdateService: Error in initial update - ${e.message}")
                e.printStackTrace()
            }
            
            while (isActive) {
                try {
                    println("⏰ MarketPriceUpdateService: Timer triggered - updating prices")
                    updateAllHoldingsPrices()
                } catch (e: Exception) {
                    println("❌ MarketPriceUpdateService: Error updating prices - ${e.message}")
                    e.printStackTrace()
                }
                
                delay(updateInterval)
            }
        }
    }
    
    /**
     * Stop the periodic price update service
     */
    fun stopPriceUpdates() {
        updateJob?.cancel()
        updateJob = null
        println("🛑 MarketPriceUpdateService: Stopped price updates")
    }
    
    /**
     * Show a toast message
     */
    private suspend fun showToast(message: String) {
        _toastMessages.emit(message)
    }
    
    /**
     * Manually trigger a price update for all holdings
     */
    suspend fun updateAllHoldingsPrices() {
        _isUpdating.value = true
        println("📊 MarketPriceUpdateService: Starting manual price update")
        
        // Show toast message for starting
        showToast("Refreshing values...")
        
        val holdings = portfolioRepository.getHoldings().first()
        if (holdings.isEmpty()) {
            println("📊 MarketPriceUpdateService: No holdings to update")
            _isUpdating.value = false
            return
        }
        
        println("📊 MarketPriceUpdateService: Updating prices for ${holdings.size} holdings")
        
        var updatedCount = 0
        // Update prices for each holding
        holdings.forEach { holding ->
            try {
                val wasUpdated = updateHoldingPrice(holding)
                if (wasUpdated) updatedCount++
            } catch (e: Exception) {
                println("❌ MarketPriceUpdateService: Failed to update price for ${holding.stockSymbol} - ${e.message}")
            }
        }
        
        _lastUpdateTime.value = Clock.System.now()
        _updateCount.value = _updateCount.value + 1
        _isUpdating.value = false
        
        println("✅ MarketPriceUpdateService: Price update completed - $updatedCount holdings updated")
        
        // Show completion toast message
        showToast("Refreshed!")
        
        // Force UI refresh by triggering a repository state change
        if (updatedCount > 0) {
            println("🔄 MarketPriceUpdateService: Triggering UI refresh for $updatedCount updated holdings")
            // The repository should automatically emit the updated data through its flows
        }
    }
    
    /**
     * Update the current price for a specific holding
     */
    private suspend fun updateHoldingPrice(holding: Holding): Boolean {
        return try {
            println("🔄 MarketPriceUpdateService: ===== STEP 2: UPDATE IN-MEMORY MODEL =====")
            println("🔄 MarketPriceUpdateService: Symbol: ${holding.stockSymbol}")
            println("📊 MarketPriceUpdateService: OLD VALUES - Current Price: ₹${holding.currentPrice}, Current Value: ₹${holding.dynamicCurrentValue}")
            
            // Step 1: Get fresh price and day change data from API
            val priceData = getStockPriceDataWithRetry(holding.stockSymbol)
            println("📊 MarketPriceUpdateService: API RETURNED: ${priceData?.let { "₹${it.currentPrice}, Change: ₹${it.dayChange}" } ?: "null"}")
            
            if (priceData != null && priceData.currentPrice > 0) {
                // Step 2: Update in-memory model first
                println("💰 MarketPriceUpdateService: UPDATING IN-MEMORY MODEL")
                println("💰 MarketPriceUpdateService: OLD PRICE: ₹${holding.currentPrice} → NEW PRICE: ₹${priceData.currentPrice}")
                println("💰 MarketPriceUpdateService: DAY CHANGE: ₹${priceData.dayChange} (${priceData.dayChangePercent}%)")
                
                val updatedHolding = holding.copy(
                    currentPrice = priceData.currentPrice,
                    dayChange = priceData.dayChange,
                    dayChangePercent = priceData.dayChangePercent
                ).calculateMetrics()
                
                println("📊 MarketPriceUpdateService: NEW IN-MEMORY VALUES:")
                println("  - Current Price: ₹${updatedHolding.currentPrice}")
                println("  - Current Value: ₹${updatedHolding.dynamicCurrentValue}")
                println("  - Invested Value: ₹${updatedHolding.dynamicInvestedValue}")
                println("  - Profit/Loss: ₹${updatedHolding.dynamicProfitLoss}")
                
                // Step 3: Store to database
                println("💾 MarketPriceUpdateService: CALLING DATABASE UPDATE")
                portfolioRepository.updateHolding(updatedHolding)
                println("✅ MarketPriceUpdateService: DATABASE UPDATE COMPLETED")
                
                println("🔄 MarketPriceUpdateService: ===== STEP 2 COMPLETE =====")
                true
            } else {
                println("⚠️ MarketPriceUpdateService: API FAILED - No valid price returned")
                false
            }
        } catch (e: Exception) {
            println("❌ MarketPriceUpdateService: EXCEPTION - ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get stock price with retry logic for network resilience
     */
    private suspend fun getStockPriceWithRetry(symbol: String, maxRetries: Int = 3): Double? {
        repeat(maxRetries) { attempt ->
            try {
                val price = stockSearchService.getStockPrice(symbol)
                if (price != null && price > 0) {
                    return price
                }
            } catch (e: Exception) {
                println("⚠️ MarketPriceUpdateService: Attempt ${attempt + 1} failed for $symbol - ${e.message}")
                if (attempt < maxRetries - 1) {
                    // Wait before retry (exponential backoff)
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            }
        }
        return null
    }
    
    /**
     * Get stock price data with retry logic for network resilience
     */
    private suspend fun getStockPriceDataWithRetry(symbol: String, maxRetries: Int = 3): com.aadhapaisa.shared.api.StockPriceData? {
        repeat(maxRetries) { attempt ->
            try {
                val priceData = stockSearchService.getStockPriceData(symbol)
                if (priceData != null && priceData.currentPrice > 0) {
                    return priceData
                }
            } catch (e: Exception) {
                println("⚠️ MarketPriceUpdateService: Attempt ${attempt + 1} failed for $symbol: ${e.message}")
                if (attempt < maxRetries - 1) {
                    // Wait before retry (exponential backoff)
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            }
        }
        return null
    }
    
    /**
     * Update price for a specific stock symbol
     */
    suspend fun updateStockPrice(stockSymbol: String): Double? {
        return try {
            val currentPrice = stockSearchService.getStockPrice(stockSymbol)
            if (currentPrice != null && currentPrice > 0) {
                println("💰 MarketPriceUpdateService: Updated price for $stockSymbol: ₹$currentPrice")
            }
            currentPrice
        } catch (e: Exception) {
            println("❌ MarketPriceUpdateService: Error updating price for $stockSymbol - ${e.message}")
            null
        }
    }
    
    /**
     * Check if the service is currently running
     */
    fun isRunning(): Boolean = updateJob?.isActive == true
    
    /**
     * Test method to verify API is working
     */
    suspend fun testApiConnection(): Boolean {
        return try {
            println("🧪 MarketPriceUpdateService: Testing API connection...")
            
            // Test with multiple symbols
            val testSymbols = listOf("RELIANCE.NS", "TCS.NS", "HDFC.NS", "INFY.NS")
            
            for (symbol in testSymbols) {
                println("🧪 MarketPriceUpdateService: Testing $symbol...")
                val testPrice = stockSearchService.getStockPrice(symbol)
                if (testPrice != null && testPrice > 0) {
                    println("✅ MarketPriceUpdateService: API test successful - $symbol price: ₹$testPrice")
                    return true
                } else {
                    println("❌ MarketPriceUpdateService: API test failed for $symbol - no price returned")
                }
            }
            
            println("❌ MarketPriceUpdateService: All API tests failed")
            false
        } catch (e: Exception) {
            println("❌ MarketPriceUpdateService: API test failed with exception: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Test method to check specific holding symbols
     */
    suspend fun testHoldingSymbols(): Boolean {
        return try {
            println("🧪 MarketPriceUpdateService: Testing holding symbols...")
            
            val holdings = portfolioRepository.getHoldings().first()
            if (holdings.isEmpty()) {
                println("❌ MarketPriceUpdateService: No holdings found to test")
                return false
            }
            
            for (holding in holdings) {
                println("🧪 MarketPriceUpdateService: Testing ${holding.stockSymbol}...")
                val testPrice = stockSearchService.getStockPrice(holding.stockSymbol)
                if (testPrice != null && testPrice > 0) {
                    println("✅ MarketPriceUpdateService: ${holding.stockSymbol} - Current price: ₹$testPrice")
                } else {
                    println("❌ MarketPriceUpdateService: ${holding.stockSymbol} - API failed")
                }
            }
            
            true
        } catch (e: Exception) {
            println("❌ MarketPriceUpdateService: Holding symbols test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Cleanup resources when service is no longer needed
     */
    fun cleanup() {
        stopPriceUpdates()
        stockSearchService.close()
        println("🧹 MarketPriceUpdateService: Cleaned up resources")
    }
}
