package com.aadhapaisa.shared.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.repository.DatabasePortfolioRepository
import com.aadhapaisa.shared.database.DatabaseDriverFactory
import com.aadhapaisa.shared.api.StockSearchService
import com.aadhapaisa.shared.api.StockDetails
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

actual class StockEntryService {
    
    actual suspend fun createStockEntryFromExcel(excelData: ExcelData, onProgress: (String) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                println("📊 StockEntryService: Processing Excel data for stock entry creation")
                
                // Find the extracted stock data from Excel
                val stockDataList = findMultipleStockDataFromExcel(excelData)
                if (stockDataList.isNotEmpty()) {
                    println("📊 StockEntryService: Found ${stockDataList.size} stock entries to create")
                    onProgress("Found ${stockDataList.size} stock entries to process...")
                    
                    var successCount = 0
                    for ((index, stockData) in stockDataList.withIndex()) {
                        val currentEntry = index + 1
                        val totalEntries = stockDataList.size
                        
                        onProgress("Creating entry $currentEntry/$totalEntries: ${stockData.stockSymbol}")
                        println("📊 StockEntryService: Creating entry $currentEntry/$totalEntries: ${stockData.stockSymbol}")
                        
                        val success = searchAndCreateEntry(
                            stockData.stockSymbol,
                            stockData.quantity,
                            stockData.avgPrice
                        )
                        
                        if (success) {
                            successCount++
                            onProgress("✅ Entry $currentEntry/$totalEntries created: ${stockData.stockSymbol}")
                            println("📊 StockEntryService: ✅ Entry $currentEntry created successfully")
                        } else {
                            onProgress("❌ Entry $currentEntry/$totalEntries failed: ${stockData.stockSymbol}")
                            println("📊 StockEntryService: ❌ Entry $currentEntry failed")
                        }
                        
                        // Small delay to show progress
                        kotlinx.coroutines.delay(200)
                    }
                    
                    onProgress("✅ Completed: $successCount/${stockDataList.size} entries created successfully")
                    println("📊 StockEntryService: Created $successCount out of ${stockDataList.size} entries")
                    return@withContext successCount > 0
                } else {
                    onProgress("❌ No stock data found in Excel file")
                    println("❌ StockEntryService: No stock data found in Excel")
                    return@withContext false
                }
            } catch (e: Exception) {
                println("❌ StockEntryService: Error creating stock entry: ${e.message}")
                e.printStackTrace()
                return@withContext false
            }
        }
    }
    
    actual suspend fun searchAndCreateEntry(stockSymbol: String, quantity: String, avgPrice: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                println("📊 StockEntryService: Automatically creating entry for symbol: $stockSymbol")
                println("📊 StockEntryService: Quantity: $quantity, Avg Price: $avgPrice")
                
                // Step 1: Search for the stock
                println("📊 StockEntryService: Searching for stock: $stockSymbol")
                val searchService = StockSearchService()
                val searchResults = searchService.searchStocks(stockSymbol)
                
                if (searchResults.isEmpty()) {
                    println("❌ StockEntryService: No stocks found for symbol: $stockSymbol")
                    return@withContext false
                }
                
                // Step 2: Get the first matching stock
                val stockResult = searchResults.first()
                println("📊 StockEntryService: Found stock: ${stockResult.name} (${stockResult.symbol})")
                
                // Step 3: Get detailed stock information
                println("📊 StockEntryService: Getting detailed stock information...")
                val stockDetails = searchService.getStockDetailsFromYahoo(stockResult.symbol)
                
                if (stockDetails == null) {
                    println("❌ StockEntryService: Could not get stock details for: $stockSymbol")
                    return@withContext false
                }
                
                println("📊 StockEntryService: Stock details - Current Price: ₹${stockDetails.currentPrice}")
                
                // Step 4: Create database repository
                val context = ContextManager.getContext() as? android.content.Context
                if (context == null) {
                    println("❌ StockEntryService: Context is null, cannot create database")
                    return@withContext false
                }
                
                val driverFactory = DatabaseDriverFactory(context)
                val portfolioRepository = DatabasePortfolioRepository(driverFactory)
                portfolioRepository.initialize()
                
                // Step 5: Create Holding object with real stock data
                // Fix: Parse quantity as double first, then convert to int
                val parsedQuantity = quantity.toDoubleOrNull()?.toInt() ?: 0
                val parsedPrice = avgPrice.toDoubleOrNull() ?: 0.0
                
                println("📊 StockEntryService: Parsed values:")
                println("📊 StockEntryService: - Raw quantity: '$quantity' -> Parsed: $parsedQuantity")
                println("📊 StockEntryService: - Raw price: '$avgPrice' -> Parsed: $parsedPrice")
                
                val holding = Holding(
                    stockSymbol = stockResult.symbol,
                    stockName = stockResult.name,
                    quantity = parsedQuantity,
                    buyPrice = parsedPrice,
                    purchaseDate = Clock.System.now(),
                    currentPrice = stockDetails.currentPrice
                ).calculateMetrics() // Calculate all metrics
                
                println("📊 StockEntryService: Created holding object: $holding")
                println("📊 StockEntryService: Holding details:")
                println("📊 StockEntryService: - stockSymbol: ${holding.stockSymbol}")
                println("📊 StockEntryService: - stockName: ${holding.stockName}")
                println("📊 StockEntryService: - quantity: ${holding.quantity}")
                println("📊 StockEntryService: - buyPrice: ${holding.buyPrice}")
                println("📊 StockEntryService: - currentPrice: ${holding.currentPrice}")
                println("📊 StockEntryService: - currentValue: ${holding.currentValue}")
                println("📊 StockEntryService: - investedValue: ${holding.investedValue}")
                println("📊 StockEntryService: - profitLoss: ${holding.profitLoss}")
                
                // Step 6: Save to database
                portfolioRepository.addHolding(holding)
                
                println("📊 StockEntryService: ✅ AUTOMATIC STOCK ENTRY CREATED AND SAVED:")
                println("📊 StockEntryService: - Stock Symbol: ${stockResult.symbol}")
                println("📊 StockEntryService: - Stock Name: ${stockResult.name}")
                println("📊 StockEntryService: - Quantity: $quantity")
                println("📊 StockEntryService: - Purchase Price: $avgPrice")
                println("📊 StockEntryService: - Current Price: ₹${stockDetails.currentPrice}")
                println("📊 StockEntryService: - Entry Date: ${Clock.System.now()}")
                
                // Close search service
                searchService.close()
                
                println("📊 StockEntryService: Stock entry creation completed successfully")
                return@withContext true
            } catch (e: Exception) {
                println("❌ StockEntryService: Error in searchAndCreateEntry: ${e.message}")
                e.printStackTrace()
                return@withContext false
            }
        }
    }
    
    private fun findMultipleStockDataFromExcel(excelData: ExcelData): List<StockEntryData> {
        println("📊 StockEntryService: Searching for multiple stock data in Excel...")
        val stockDataList = mutableListOf<StockEntryData>()
        
        for (sheet in excelData.sheets) {
            println("📊 StockEntryService: Checking sheet: ${sheet.name}")
            for (row in sheet.rows) {
                println("📊 StockEntryService: Row ${row.rowNumber}: ${row.cells}")
                // Look for all data rows (row 12 onwards) and extract the first 4 columns
                if (row.rowNumber >= 12 && row.cells.size >= 4) {
                    val stockName = row.cells[0]
                    val stockSymbol = row.cells[1]
                    val quantity = row.cells[2]
                    val avgPrice = row.cells[3]
                    
                    // Skip empty rows
                    if (stockName.isNotBlank() && stockSymbol.isNotBlank() && quantity.isNotBlank() && avgPrice.isNotBlank()) {
                        println("📊 StockEntryService: Found data in row ${row.rowNumber}:")
                        println("📊 StockEntryService: - Stock Name: '$stockName'")
                        println("📊 StockEntryService: - Stock Symbol: '$stockSymbol'")
                        println("📊 StockEntryService: - Quantity: '$quantity'")
                        println("📊 StockEntryService: - Avg Price: '$avgPrice'")
                        
                        stockDataList.add(StockEntryData(
                            stockName = stockName,
                            stockSymbol = stockSymbol,
                            quantity = quantity,
                            avgPrice = avgPrice
                        ))
                    }
                }
            }
        }
        
        println("📊 StockEntryService: Found ${stockDataList.size} valid stock entries")
        return stockDataList
    }
    
    private fun findStockDataFromExcel(excelData: ExcelData): StockEntryData? {
        println("📊 StockEntryService: Searching for stock data in Excel...")
        for (sheet in excelData.sheets) {
            println("📊 StockEntryService: Checking sheet: ${sheet.name}")
            for (row in sheet.rows) {
                println("📊 StockEntryService: Row ${row.rowNumber}: ${row.cells}")
                // Look for row 12 (the data row) and extract the first 4 columns
                if (row.rowNumber == 12 && row.cells.size >= 4) {
                    val stockName = row.cells[0]
                    val stockSymbol = row.cells[1]
                    val quantity = row.cells[2]
                    val avgPrice = row.cells[3]
                    
                    println("📊 StockEntryService: Found data in row 12:")
                    println("📊 StockEntryService: - Stock Name: '$stockName'")
                    println("📊 StockEntryService: - Stock Symbol: '$stockSymbol'")
                    println("📊 StockEntryService: - Quantity: '$quantity'")
                    println("📊 StockEntryService: - Avg Price: '$avgPrice'")
                    
                    return StockEntryData(
                        stockName = stockName,
                        stockSymbol = stockSymbol,
                        quantity = quantity,
                        avgPrice = avgPrice
                    )
                }
            }
        }
        println("❌ StockEntryService: No data found in row 12")
        return null
    }
}
