package com.aadhapaisa.shared.repository

import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.models.PortfolioSummary
import com.aadhapaisa.shared.models.ConsolidatedHolding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class SavedPortfolio(
    val holdings: List<Holding> = emptyList()
)

expect class SimpleStorage {
    fun initialize(context: Any)
    fun saveData(key: String, data: String)
    fun loadData(key: String): String?
    fun clearData(key: String)
}

class SimplePersistentRepository : PortfolioRepository {
    private val _holdings = MutableStateFlow<List<Holding>>(emptyList())
    private val storage = SimpleStorage()
    private val json = Json { ignoreUnknownKeys = true }
    private var isInitialized = false
    
    init {
        // Don't load data here - wait for context initialization
        println("🏗️ SimplePersistentRepository created, waiting for context...")
    }
    
    
    fun initialize(context: Any) {
        if (isInitialized) {
            println("🔧 SimplePersistentRepository already initialized, skipping...")
            return
        }
        
        println("🔧 Initializing SimplePersistentRepository with context: ${context::class.simpleName}")
        // Platform-specific initialization
        storage.initialize(context)
        
        // Test storage immediately
        println("🧪 Testing storage immediately...")
        storage.saveData("test", "test_value")
        val testResult = storage.loadData("test")
        println("🧪 Storage test result: $testResult")
        storage.clearData("test")
        
        println("🔄 Loading data after initialization")
        loadFromStorage()
        println("📊 Final holdings count: ${_holdings.value.size}")
        
        isInitialized = true
        println("✅ SimplePersistentRepository initialization completed")
    }
    
    fun isInitialized(): Boolean = isInitialized
    
    // Debug method to check what's in storage
    fun debugStorage() {
        println("🔍 DEBUG: Checking storage status...")
        println("🔍 DEBUG: Repository initialized: $isInitialized")
        println("🔍 DEBUG: Current holdings in memory: ${_holdings.value.size}")
        _holdings.value.forEach { holding ->
            println("  - ${holding.stockSymbol}: ${holding.stockName} (${holding.quantity} shares)")
        }
        
        // Check what's actually in storage
        val savedData = storage.loadData("portfolio")
        println("🔍 DEBUG: Raw data in storage: $savedData")
        
        if (savedData != null && savedData.isNotEmpty()) {
            try {
                val portfolio = json.decodeFromString<SavedPortfolio>(savedData)
                println("🔍 DEBUG: Parsed portfolio has ${portfolio.holdings.size} holdings")
                portfolio.holdings.forEach { holding ->
                    println("  - ${holding.stockSymbol}: ${holding.stockName} (${holding.quantity} shares)")
                }
            } catch (e: Exception) {
                println("❌ DEBUG: Failed to parse stored data: ${e.message}")
                e.printStackTrace()
            }
        } else {
            println("🔍 DEBUG: No data found in storage")
        }
    }
    
    private fun loadFromStorage() {
        try {
            println("🔍 Attempting to load from storage...")
            val savedData = storage.loadData("portfolio")
            println("🔍 Loading from storage: $savedData")
            if (savedData != null && savedData.isNotEmpty()) {
                println("📦 Found saved data, attempting to parse...")
                val portfolio = json.decodeFromString<SavedPortfolio>(savedData)
                _holdings.value = portfolio.holdings
                println("✅ Loaded ${portfolio.holdings.size} holdings from storage")
                portfolio.holdings.forEach { holding ->
                    println("  - ${holding.stockSymbol}: ${holding.stockName} (${holding.quantity} shares @ ₹${holding.buyPrice})")
                }
            } else {
                println("📭 No saved data found, starting with empty portfolio")
                _holdings.value = emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error loading from storage: ${e.message}")
            e.printStackTrace()
            // If loading fails, start with empty portfolio
            _holdings.value = emptyList()
        }
    }
    
    private fun saveToStorage() {
        try {
            println("💾 Attempting to save to storage...")
            val portfolio = SavedPortfolio(_holdings.value)
            println("💾 Preparing to save portfolio with ${portfolio.holdings.size} holdings")
            portfolio.holdings.forEach { holding ->
                println("  - ${holding.stockSymbol}: ${holding.stockName} (${holding.quantity} shares @ ₹${holding.buyPrice})")
            }
            
            val jsonString = json.encodeToString(portfolio)
            println("💾 JSON to save: $jsonString")
            storage.saveData("portfolio", jsonString)
            println("✅ Successfully saved ${_holdings.value.size} holdings to storage")
        } catch (e: Exception) {
            println("❌ Error saving to storage: ${e.message}")
            e.printStackTrace()
            // Silently fail - data will be lost but app won't crash
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
        getHoldings().map { holdings ->
            holdings.sortedByDescending { it.purchaseDate }.take(limit)
        }

    override fun getPositiveHoldings(): Flow<List<Holding>> = 
        getHoldings().map { holdings ->
            holdings.filter { it.dynamicProfitLoss >= 0 }
        }

    override fun getNegativeHoldings(): Flow<List<Holding>> = 
        getHoldings().map { holdings ->
            holdings.filter { it.dynamicProfitLoss < 0 }
        }

    override suspend fun addHolding(holding: Holding) {
        if (!isInitialized) {
            println("❌ Repository not initialized! Cannot add holding.")
            return
        }
        
        println("📝 addHolding called: ${holding.stockSymbol} - ${holding.stockName}")
        val currentHoldings = _holdings.value.toMutableList()
        println("📊 Current holdings before add: ${currentHoldings.size}")
        currentHoldings.add(holding)
        _holdings.value = currentHoldings
        println("📊 Holdings after add: ${_holdings.value.size}")
        println("💾 Calling saveToStorage...")
        saveToStorage()
        println("✅ addHolding completed successfully")
    }

    override suspend fun updateHolding(holding: Holding) {
        println("🔄 SimplePersistentRepository: Updating holding ${holding.stockSymbol}")
        println("📊 SimplePersistentRepository: New current price: ₹${holding.currentPrice}")
        println("📊 SimplePersistentRepository: New current value: ₹${holding.dynamicCurrentValue}")
        
        val currentHoldings = _holdings.value.toMutableList()
        val index = currentHoldings.indexOfFirst { it.stockSymbol == holding.stockSymbol }
        if (index != -1) {
            val oldHolding = currentHoldings[index]
            println("📊 SimplePersistentRepository: Old price: ₹${oldHolding.currentPrice}, New price: ₹${holding.currentPrice}")
            
            currentHoldings[index] = holding
            _holdings.value = currentHoldings
            
            println("✅ SimplePersistentRepository: Updated ${holding.stockSymbol} in repository")
            println("📊 SimplePersistentRepository: Repository now has ${_holdings.value.size} holdings")
            
            saveToStorage()
        } else {
            println("❌ SimplePersistentRepository: Could not find holding ${holding.stockSymbol} to update")
        }
    }

    override suspend fun removeHolding(stockSymbol: String) {
        val currentHoldings = _holdings.value.toMutableList()
        currentHoldings.removeAll { it.stockSymbol == stockSymbol }
        _holdings.value = currentHoldings
        saveToStorage()
    }
    
    // Test method to verify persistence
    fun testPersistence() {
        println("🧪 Testing persistence...")
        println("📊 Current holdings: ${_holdings.value.size}")
        _holdings.value.forEach { holding ->
            println("  - ${holding.stockSymbol}: ${holding.stockName} (${holding.quantity} shares)")
        }
        
        // Force save current state
        saveToStorage()
        println("💾 Forced save completed")
        
        // Clear memory and reload
        _holdings.value = emptyList()
        println("🗑️ Cleared memory, holdings count: ${_holdings.value.size}")
        
        loadFromStorage()
        println("🔄 Reloaded from storage, holdings count: ${_holdings.value.size}")
    }
    
    // Method to clear all data and start fresh
    override fun clearAllData() {
        println("🗑️ Clearing all data...")
        storage.clearData("portfolio")
        _holdings.value = emptyList()
        println("✅ All data cleared")
    }
    
    
    // Test JSON serialization without Instant
    fun testJsonSerialization() {
        println("🧪 Testing JSON serialization...")
        try {
            val testData = SavedPortfolio(
                holdings = listOf(
                    Holding(
                        stockSymbol = "TEST",
                        stockName = "Test Company",
                        quantity = 1,
                        buyPrice = 100.0,
                        purchaseDate = kotlinx.datetime.Clock.System.now(),
                        currentPrice = 110.0
                    )
                )
            )
            
            val jsonString = json.encodeToString(testData)
            println("✅ JSON serialization successful: $jsonString")
            
            val parsed = json.decodeFromString<SavedPortfolio>(jsonString)
            println("✅ JSON deserialization successful: ${parsed.holdings.size} holdings")
            
        } catch (e: Exception) {
            println("❌ JSON serialization failed: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Check if storage is properly initialized
    fun checkStorageStatus() {
        println("🔍 Checking storage status...")
        try {
            val testData = storage.loadData("test_key")
            println("✅ Storage is accessible, test read: $testData")
            
            // Try to save test data
            storage.saveData("test_key", "test_value")
            val savedData = storage.loadData("test_key")
            println("✅ Storage write/read test successful: $savedData")
            
            // Clean up test data
            storage.clearData("test_key")
            println("✅ Storage test completed successfully")
            
        } catch (e: Exception) {
            println("❌ Storage test failed: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Test complete data flow
    fun testCompleteDataFlow() {
        println("🧪 Testing complete data flow...")
        
        // 1. Create a test holding
        val testHolding = Holding(
            stockSymbol = "FLOWTEST",
            stockName = "Flow Test Stock",
            quantity = 5,
            buyPrice = 200.0,
            purchaseDate = kotlinx.datetime.Clock.System.now(),
            currentPrice = 220.0
        ).calculateMetrics()
        
        println("📝 Created test holding: ${testHolding.stockSymbol}")
        
        // 2. Add to repository (synchronous test)
        try {
            // Test JSON serialization first
            val portfolio = SavedPortfolio(listOf(testHolding))
            val jsonString = json.encodeToString(portfolio)
            println("💾 JSON serialization test: $jsonString")
            
            // Test JSON deserialization
            val parsed = json.decodeFromString<SavedPortfolio>(jsonString)
            println("✅ JSON deserialization test: ${parsed.holdings.size} holdings")
            
            // Test storage directly
            storage.saveData("flow_test", jsonString)
            val loaded = storage.loadData("flow_test")
            println("💾 Storage test: $loaded")
            
            // Clean up
            storage.clearData("flow_test")
            println("✅ Complete data flow test completed")
            
        } catch (e: Exception) {
            println("❌ Test failed: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Test actual persistence with real data
    fun testRealPersistence() {
        println("🧪 Testing REAL persistence flow...")
        
        if (!isInitialized) {
            println("❌ Repository not initialized! Cannot test persistence.")
            return
        }
        
        // 1. Create a test holding
        val testHolding = Holding(
            stockSymbol = "PERSISTTEST",
            stockName = "Persistence Test Stock",
            quantity = 3,
            buyPrice = 150.0,
            purchaseDate = kotlinx.datetime.Clock.System.now(),
            currentPrice = 160.0
        ).calculateMetrics()
        
        println("📝 Created test holding: ${testHolding.stockSymbol}")
        
        // 2. Test JSON serialization
        try {
            val portfolio = SavedPortfolio(listOf(testHolding))
            val jsonString = json.encodeToString(portfolio)
            println("💾 JSON serialization: $jsonString")
            
            // 3. Test saving to storage
            storage.saveData("persist_test", jsonString)
            println("✅ Data saved to storage")
            
            // 4. Test loading from storage
            val loadedData = storage.loadData("persist_test")
            println("💾 Loaded data: $loadedData")
            
            if (loadedData != null && loadedData.isNotEmpty()) {
                val loadedPortfolio = json.decodeFromString<SavedPortfolio>(loadedData)
                println("✅ Successfully loaded ${loadedPortfolio.holdings.size} holdings from storage")
                loadedPortfolio.holdings.forEach { holding ->
                    println("  - ${holding.stockSymbol}: ${holding.stockName} (${holding.quantity} shares @ ₹${holding.buyPrice})")
                }
            } else {
                println("❌ No data found in storage!")
            }
            
            // 5. Clean up
            storage.clearData("persist_test")
            println("✅ Persistence test completed successfully")
            
        } catch (e: Exception) {
            println("❌ Persistence test failed: ${e.message}")
            e.printStackTrace()
        }
    }
}
