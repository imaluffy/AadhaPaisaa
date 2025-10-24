import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.*

suspend fun testYahooFinanceAPI() {
    val httpClient = HttpClient(CIO)
    
    try {
        println("🔍 Testing Yahoo Finance API")
        println("📡 URL: https://query1.finance.yahoo.com/v8/finance/chart/TVSELECT.NS")
        println("📡 Parameters:")
        println("  - region: IN")
        println("  - lang: en") 
        println("  - includePrePost: false")
        println("  - interval: 1d")
        println("  - useYfid: true")
        println("  - range: 1d")
        println()
        
        val response = httpClient.get("https://query1.finance.yahoo.com/v8/finance/chart/TVSELECT.NS") {
            parameter("region", "IN")
            parameter("lang", "en")
            parameter("includePrePost", "false")
            parameter("interval", "1d")
            parameter("useYfid", "true")
            parameter("range", "1d")
        }
        
        println("📡 HTTP Status: ${response.status}")
        println("📡 Response Headers:")
        response.headers.forEach { name, values ->
            println("  $name: ${values.joinToString(", ")}")
        }
        println()
        
        val responseText = response.body<String>()
        println("📡 Response Length: ${responseText.length} characters")
        println()
        println("📡 First 1000 characters of response:")
        println(responseText.take(1000))
        println()
        println("📡 Last 1000 characters of response:")
        println(responseText.takeLast(1000))
        println()
        
        // Try to find price in response
        val priceRegex = "\"regularMarketPrice\":\\s*([0-9.]+)".toRegex()
        val match = priceRegex.find(responseText)
        if (match != null) {
            val price = match.groupValues[1]
            println("✅ Found price: ₹$price")
        } else {
            println("❌ No price found with regularMarketPrice regex")
            
            // Try alternative patterns
            val altPatterns = listOf(
                "\"currentPrice\":\\s*([0-9.]+)",
                "\"price\":\\s*([0-9.]+)",
                "\"close\":\\s*([0-9.]+)",
                "\"regularMarketPreviousClose\":\\s*([0-9.]+)"
            )
            
            for (pattern in altPatterns) {
                val regex = pattern.toRegex()
                val altMatch = regex.find(responseText)
                if (altMatch != null) {
                    val price = altMatch.groupValues[1]
                    println("✅ Found price with $pattern: ₹$price")
                    break
                }
            }
        }
        
    } catch (e: Exception) {
        println("❌ Error: ${e.message}")
        e.printStackTrace()
    } finally {
        httpClient.close()
    }
}
