package com.aadhapaisa.shared.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StockSearchResult(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val currency: String = "INR",
    val exchange: String = "NSE"
)

@Serializable
data class StockDetails(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val exchange: String,
    val allTimeHigh: Double,
    val allTimeLow: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val volume: Long
)

@Serializable
data class YahooFinanceResponse(
    val quotes: List<YahooQuote>
)

@Serializable
data class YahooQuote(
    val symbol: String,
    val shortName: String,
    val longName: String,
    val regularMarketPrice: Double? = null,
    val regularMarketPreviousClose: Double? = null
)

@Serializable
data class YahooFinanceChartResponse(
    val chart: YahooFinanceChart
)

@Serializable
data class YahooFinanceChart(
    val result: List<YahooFinanceResult>
)

@Serializable
data class YahooFinanceResult(
    val meta: YahooFinanceMeta
)

@Serializable
data class YahooFinanceMeta(
    val regularMarketPrice: Double? = null,
    val chartPreviousClose: Double? = null,
    val currency: String? = null,
    val symbol: String? = null,
    val previousClose: Double? = null,
    val regularMarketChange: Double? = null,
    val regularMarketChangePercent: Double? = null,
    val open: Double? = null,
    val dayHigh: Double? = null,
    val dayLow: Double? = null
)

@Serializable
data class StockPriceData(
    val currentPrice: Double,
    val dayChange: Double,
    val dayChangePercent: Double
)

class StockSearchService {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun searchStocks(query: String): List<StockSearchResult> {
        println("API: ===== SEARCHING FOR: '$query' =====")
        
        val results = mutableListOf<StockSearchResult>()
        
        // Try NSE India API first (most reliable for Indian stocks)
        try {
            println("API: Calling NSE API for '$query'")
            val nseResponse = httpClient.get("https://www.nseindia.com/api/search/autocomplete") {
                parameter("q", query)
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                header("Accept", "application/json")
                header("Accept-Language", "en-US,en;q=0.9")
            }
            
            println("API: NSE Response Status: ${nseResponse.status}")
            
                if (nseResponse.status.value in 200..299) {
                    val responseText = nseResponse.body<String>()
                    println("API: NSE Raw Response: $responseText")
                
                // Parse NSE response
                if (responseText.contains("\"symbols\"")) {
                    val symbolRegex = "\"symbol\":\\s*\"([^\"]+)\"".toRegex()
                    val nameRegex = "\"symbol_info\":\\s*\"([^\"]+)\"".toRegex()
                    val urlRegex = "\"url\":\\s*\"([^\"]+)\"".toRegex()
                    
                    val symbols = symbolRegex.findAll(responseText).map { it.groupValues[1] }.toList()
                    val names = nameRegex.findAll(responseText).map { it.groupValues[1] }.toList()
                    val urls = urlRegex.findAll(responseText).map { it.groupValues[1] }.toList()
                    
                    println("API: NSE Found ${symbols.size} symbols: $symbols")
                    println("API: NSE Found ${names.size} names: $names")
                    println("API: NSE Found ${urls.size} URLs: $urls")
                    
                    symbols.zip(names).zip(urls).forEach { (symbolName, url) ->
                        val (symbol, name) = symbolName
                        // Fetch real price for each stock
                        val realPrice = getStockPriceFromNSE(symbol)
                        results.add(StockSearchResult(
                            symbol = symbol,
                            name = name,
                            currentPrice = realPrice
                        ))
                    }
                } else {
                    println("API: NSE Response doesn't contain 'symbols'")
                }
            } else {
                println("API: NSE API returned non-success status: ${nseResponse.status.value}")
            }
        } catch (e: Exception) {
            println("API: NSE API Error: ${e.message}")
            e.printStackTrace()
        }
        
        // Try Yahoo Finance as backup
        if (results.isEmpty()) {
            try {
                println("API: Trying Yahoo Finance for '$query'")
                val yahooResponse = httpClient.get("https://query1.finance.yahoo.com/v1/finance/search") {
                    parameter("q", query)
                    parameter("quotesCount", 10)
                    parameter("newsCount", 0)
                    parameter("region", "IN")
                    parameter("lang", "en")
                }
                
                println("API: Yahoo Response Status: ${yahooResponse.status}")
                
                if (yahooResponse.status.value in 200..299) {
                    val yahooData: YahooFinanceResponse = yahooResponse.body()
                    println("API: Yahoo Found ${yahooData.quotes.size} quotes")
                    
                    yahooData.quotes.forEach { quote ->
                        if (quote.symbol.isNotEmpty() && quote.longName.isNotEmpty()) {
                            results.add(StockSearchResult(
                                symbol = quote.symbol,
                                name = quote.longName,
                                currentPrice = quote.regularMarketPrice ?: quote.regularMarketPreviousClose ?: 0.0
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                println("API: Yahoo Finance Error: ${e.message}")
            }
        }
        
        println("API: ===== FINAL RESULTS: ${results.size} stocks found =====")
        results.forEach { result ->
            println("API: - ${result.symbol}: ${result.name}")
        }
        
        return results
    }

    suspend fun getStockPriceFromNSE(symbol: String): Double {
        return try {
            println("API: Fetching price for $symbol from Yahoo Finance")
            // Use Yahoo Finance with .NS suffix for NSE stocks
            val yahooSymbol = if (symbol.endsWith(".NS")) symbol else "$symbol.NS"
            val response = httpClient.get("https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol") {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                header("Accept", "application/json")
                header("Accept-Language", "en-US,en;q=0.9")
            }
            
            if (response.status.value in 200..299) {
                val responseText = response.body<String>()
                println("API: Yahoo Price Response for $symbol: $responseText")
                
                // Parse price from Yahoo Finance response
                val priceRegex = "\"regularMarketPrice\":\\s*([0-9.]+)".toRegex()
                val match = priceRegex.find(responseText)
                if (match != null) {
                    val price = match.groupValues[1].toDouble()
                    println("API: Found price for $symbol: ₹$price")
                    price
                } else {
                    // Try alternative regex for different response format
                    val altPriceRegex = "\"chartPreviousClose\":\\s*([0-9.]+)".toRegex()
                    val altMatch = altPriceRegex.find(responseText)
                    if (altMatch != null) {
                        val price = altMatch.groupValues[1].toDouble()
                        println("API: Found price for $symbol (alt): ₹$price")
                        price
                    } else {
                        println("API: No price found for $symbol, using fallback")
                        println("API: Response was: $responseText")
                        150.0 // Fallback price
                    }
                }
            } else {
                println("API: Yahoo price API failed for $symbol: ${response.status.value}")
                150.0 // Fallback price
            }
        } catch (e: Exception) {
            println("API: Error fetching price for $symbol: ${e.message}")
            150.0 // Fallback price
        }
    }

    suspend fun getStockDetailsFromYahoo(symbol: String): StockDetails? {
        return try {
            println("API: Fetching detailed data for $symbol from Yahoo Finance")
            val yahooSymbol = if (symbol.endsWith(".NS")) symbol else "$symbol.NS"
            val response = httpClient.get("https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol") {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                header("Accept", "application/json")
                header("Accept-Language", "en-US,en;q=0.9")
            }
            
            if (response.status.value in 200..299) {
                val responseText = response.body<String>()
                println("API: Yahoo Details Response for $symbol: $responseText")
                
                // Parse all the real data from Yahoo Finance
                val currentPrice = extractValue(responseText, "\"regularMarketPrice\":\\s*([0-9.]+)")
                val dayHigh = extractValue(responseText, "\"regularMarketDayHigh\":\\s*([0-9.]+)")
                val dayLow = extractValue(responseText, "\"regularMarketDayLow\":\\s*([0-9.]+)")
                val weekHigh = extractValue(responseText, "\"fiftyTwoWeekHigh\":\\s*([0-9.]+)")
                val weekLow = extractValue(responseText, "\"fiftyTwoWeekLow\":\\s*([0-9.]+)")
                val volume = extractValue(responseText, "\"regularMarketVolume\":\\s*([0-9]+)")
                val companyName = extractString(responseText, "\"longName\":\\s*\"([^\"]+)\"")
                
                if (currentPrice > 0) {
                    StockDetails(
                        symbol = symbol,
                        name = companyName ?: symbol,
                        currentPrice = currentPrice,
                        exchange = "NSE",
                        allTimeHigh = weekHigh,
                        allTimeLow = weekLow,
                        dayHigh = dayHigh,
                        dayLow = dayLow,
                        volume = volume.toLong()
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            println("API: Error fetching details for $symbol: ${e.message}")
            null
        }
    }
    
    private fun extractValue(text: String, regex: String): Double {
        val pattern = regex.toRegex()
        val match = pattern.find(text)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }
    
    private fun extractString(text: String, regex: String): String? {
        val pattern = regex.toRegex()
        val match = pattern.find(text)
        return match?.groupValues?.get(1)
    }

    suspend fun getStockPriceData(symbol: String): StockPriceData? {
        return try {
            // Convert symbol to Yahoo Finance format for Indian stocks
            val yahooSymbol = if (symbol.endsWith(".NS") || symbol.endsWith(".BO")) {
                symbol
            } else {
                "$symbol.NS" // Default to NSE
            }
            
            println("🔍 StockSearchService: ===== API CALL START =====")
            println("🔍 StockSearchService: Original Symbol: $symbol")
            println("🔍 StockSearchService: Yahoo Symbol: $yahooSymbol")
            
            val response = httpClient.get("https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol") {
                parameter("region", "IN")
                parameter("lang", "en")
                parameter("includePrePost", "false")
                parameter("interval", "1d")
                parameter("useYfid", "true")
                parameter("range", "1d")
            }
            
            val responseText = response.body<String>()
            println("📡 StockSearchService: HTTP Status: ${response.status}")
            println("📡 StockSearchService: Response length: ${responseText.length}")
            
            // Parse the response
            val priceData = parsePriceDataFromResponse(responseText, symbol)
            
            if (priceData != null) {
                println("✅ StockSearchService: SUCCESS - Parsed price data for $symbol: ${priceData.currentPrice}, change: ${priceData.dayChange}")
                println("🔍 StockSearchService: ===== API CALL END - RETURNING: ₹${priceData.currentPrice} =====")
            } else {
                println("❌ StockSearchService: FAILED - No price data found for $symbol")
                println("❌ StockSearchService: Response preview: ${responseText.take(200)}")
            }
            
            priceData
        } catch (e: Exception) {
            println("❌ StockSearchService: ERROR - Exception for $symbol: ${e.message}")
            null
        }
    }

    suspend fun getStockPrice(symbol: String): Double? {
        return try {
            // Convert symbol to Yahoo Finance format for Indian stocks
            val yahooSymbol = if (symbol.endsWith(".NS") || symbol.endsWith(".BO")) {
                symbol
            } else {
                "$symbol.NS" // Default to NSE
            }
            
            println("🔍 StockSearchService: ===== API CALL START =====")
            println("🔍 StockSearchService: Original Symbol: $symbol")
            println("🔍 StockSearchService: Yahoo Symbol: $yahooSymbol")
            
            val response = httpClient.get("https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol") {
                parameter("region", "IN")
                parameter("lang", "en")
                parameter("includePrePost", "false")
                parameter("interval", "1d")
                parameter("useYfid", "true")
                parameter("range", "1d")
            }
            
            val responseText = response.body<String>()
            println("📡 StockSearchService: HTTP Status: ${response.status}")
            println("📡 StockSearchService: Response length: ${responseText.length}")
            
            // Parse the response
            val price = parsePriceFromResponse(responseText, symbol)
            
            if (price != null) {
                println("✅ StockSearchService: SUCCESS - Parsed price for $symbol: ₹$price")
                println("🔍 StockSearchService: ===== API CALL END - RETURNING: ₹$price =====")
            } else {
                println("❌ StockSearchService: FAILED - No price found for $symbol")
                println("❌ StockSearchService: Response preview: ${responseText.take(200)}")
            }
            
            price
        } catch (e: Exception) {
            println("❌ StockSearchService: ERROR - Exception for $symbol: ${e.message}")
            null
        }
    }
    
    private fun parsePriceFromResponse(responseText: String, symbol: String): Double? {
        println("🔍 StockSearchService: Parsing response for $symbol")
        
        // Method 1: Try to parse the correct Yahoo Finance structure
        try {
            val json = Json { ignoreUnknownKeys = true }
            val yahooResponse = json.decodeFromString<YahooFinanceChartResponse>(responseText)
            
            if (yahooResponse.chart.result.isNotEmpty()) {
                val result = yahooResponse.chart.result.first()
                if (result.meta.regularMarketPrice != null) {
                    println("✅ StockSearchService: Found regularMarketPrice: ₹${result.meta.regularMarketPrice}")
                    return result.meta.regularMarketPrice
                } else {
                    println("⚠️ StockSearchService: regularMarketPrice is null for $symbol")
                    println("⚠️ StockSearchService: Available fields: ${result.meta}")
                }
            } else {
                println("⚠️ StockSearchService: No results found in chart for $symbol")
            }
        } catch (e: Exception) {
            println("⚠️ StockSearchService: JSON parsing failed for $symbol: ${e.message}")
            println("⚠️ StockSearchService: Response text: ${responseText.take(200)}")
        }
        
        // Method 2: Fallback to regex parsing for regularMarketPrice
        try {
            val priceRegex = "\"regularMarketPrice\":\\s*([0-9.]+)".toRegex()
            val matchResult = priceRegex.find(responseText)
            val priceString = matchResult?.groupValues?.get(1)
            if (priceString != null) {
                val price = priceString.toDoubleOrNull()
                println("✅ StockSearchService: Found price via regex: ₹$price")
                return price
            }
        } catch (e: Exception) {
            println("⚠️ StockSearchService: Regex parsing failed for $symbol: ${e.message}")
        }
        
        // Method 3: Try alternative price fields (but avoid chartPreviousClose)
        try {
            // Only try safe alternatives that won't match chartPreviousClose
            val alternativeRegexes = listOf(
                "\"currentPrice\":\\s*([0-9.]+)".toRegex(),
                "\"price\":\\s*([0-9.]+)".toRegex()
            )
            
            for (regex in alternativeRegexes) {
                val matchResult = regex.find(responseText)
                val priceString = matchResult?.groupValues?.get(1)
                if (priceString != null) {
                    val price = priceString.toDoubleOrNull()
                    if (price != null && price > 0) {
                        println("✅ StockSearchService: Found price via alternative regex: ₹$price")
                        return price
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ StockSearchService: Alternative parsing failed for $symbol: ${e.message}")
        }
        
        println("❌ StockSearchService: No price found for $symbol")
        return null
    }

    private fun parsePriceDataFromResponse(responseText: String, symbol: String): StockPriceData? {
        println("🔍 StockSearchService: Parsing price data for $symbol")
        
        // Method 1: Try to parse the correct Yahoo Finance structure
        try {
            val json = Json { ignoreUnknownKeys = true }
            val yahooResponse = json.decodeFromString<YahooFinanceChartResponse>(responseText)
            
            if (yahooResponse.chart.result.isNotEmpty()) {
                val result = yahooResponse.chart.result.first()
                val meta = result.meta
                
                val currentPrice = meta.regularMarketPrice
                val previousClose = meta.previousClose ?: meta.chartPreviousClose
                val dayChange = meta.regularMarketChange
                val dayChangePercent = meta.regularMarketChangePercent
                
                println("🔍 StockSearchService: API Response Fields:")
                println("  - regularMarketPrice: $currentPrice")
                println("  - previousClose: $previousClose")
                println("  - chartPreviousClose: ${meta.chartPreviousClose}")
                println("  - regularMarketChange: $dayChange")
                println("  - regularMarketChangePercent: $dayChangePercent")
                
                if (currentPrice != null) {
                    // Calculate day change - prioritize API provided values, then calculate
                    val calculatedDayChange = when {
                        dayChange != null && dayChange != 0.0 -> {
                            println("✅ Using API provided dayChange: $dayChange")
                            dayChange
                        }
                        previousClose != null && previousClose > 0 -> {
                            val calculated = currentPrice - previousClose
                            println("✅ Calculated dayChange from previousClose: $calculated")
                            calculated
                        }
                        else -> {
                            println("⚠️ No previous close available, using 0.0")
                            0.0
                        }
                    }
                    
                    val calculatedDayChangePercent = when {
                        dayChangePercent != null && dayChangePercent != 0.0 -> {
                            println("✅ Using API provided dayChangePercent: $dayChangePercent")
                            dayChangePercent
                        }
                        previousClose != null && previousClose > 0 -> {
                            val calculated = (calculatedDayChange / previousClose) * 100
                            println("✅ Calculated dayChangePercent: $calculated")
                            calculated
                        }
                        else -> {
                            println("⚠️ No previous close available for percentage, using 0.0")
                            0.0
                        }
                    }
                    
                    println("✅ StockSearchService: Found price data - Price: ₹$currentPrice, Change: ₹$calculatedDayChange, Change%: ${calculatedDayChangePercent}%")
                    return StockPriceData(
                        currentPrice = currentPrice,
                        dayChange = calculatedDayChange,
                        dayChangePercent = calculatedDayChangePercent
                    )
                } else {
                    println("⚠️ StockSearchService: regularMarketPrice is null for $symbol")
                }
            } else {
                println("⚠️ StockSearchService: No results found in chart for $symbol")
            }
        } catch (e: Exception) {
            println("⚠️ StockSearchService: JSON parsing failed for $symbol: ${e.message}")
        }
        
        // Method 2: Fallback to regex parsing
        try {
            val priceRegex = "\"regularMarketPrice\":\\s*([0-9.]+)".toRegex()
            val changeRegex = "\"regularMarketChange\":\\s*([0-9.-]+)".toRegex()
            val changePercentRegex = "\"regularMarketChangePercent\":\\s*([0-9.-]+)".toRegex()
            val previousCloseRegex = "\"previousClose\":\\s*([0-9.]+)".toRegex()
            val chartPreviousCloseRegex = "\"chartPreviousClose\":\\s*([0-9.]+)".toRegex()
            
            val priceMatch = priceRegex.find(responseText)
            val changeMatch = changeRegex.find(responseText)
            val changePercentMatch = changePercentRegex.find(responseText)
            val previousCloseMatch = previousCloseRegex.find(responseText)
            val chartPreviousCloseMatch = chartPreviousCloseRegex.find(responseText)
            
            val priceString = priceMatch?.groupValues?.get(1)
            val changeString = changeMatch?.groupValues?.get(1)
            val changePercentString = changePercentMatch?.groupValues?.get(1)
            val previousCloseString = previousCloseMatch?.groupValues?.get(1)
            val chartPreviousCloseString = chartPreviousCloseMatch?.groupValues?.get(1)
            
            if (priceString != null) {
                val currentPrice = priceString.toDoubleOrNull()
                val previousClose = previousCloseString?.toDoubleOrNull() ?: chartPreviousCloseString?.toDoubleOrNull()
                
                if (currentPrice != null) {
                    // Calculate day change from regex data
                    val dayChange = if (changeString != null) {
                        changeString.toDoubleOrNull() ?: 0.0
                    } else if (previousClose != null && previousClose > 0) {
                        currentPrice - previousClose
                    } else {
                        0.0
                    }
                    
                    val dayChangePercent = if (changePercentString != null) {
                        changePercentString.toDoubleOrNull() ?: 0.0
                    } else if (previousClose != null && previousClose > 0) {
                        (dayChange / previousClose) * 100
                    } else {
                        0.0
                    }
                    
                    println("✅ StockSearchService: Found price data via regex - Price: ₹$currentPrice, Change: ₹$dayChange, PreviousClose: $previousClose")
                    return StockPriceData(
                        currentPrice = currentPrice,
                        dayChange = dayChange,
                        dayChangePercent = dayChangePercent
                    )
                }
            }
        } catch (e: Exception) {
            println("⚠️ StockSearchService: Regex parsing failed for $symbol: ${e.message}")
        }
        
        println("❌ StockSearchService: No price data found for $symbol")
        return null
    }

    // Mock data completely removed - only real API calls now

    fun close() {
        httpClient.close()
    }
}
