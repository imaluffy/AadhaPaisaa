package com.aadhapaisa.shared.api

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TestResult(
    val query: String,
    val success: Boolean,
    val results: Int,
    val error: String? = null,
    val responseTime: Long
)

class ApiTestService {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun runComprehensiveTests(): List<TestResult> {
        val testQueries = listOf(
            "swi", "swig", "swiggy",
            "infi", "infibeam", 
            "avan", "avantel",
            "rel", "reliance",
            "tcs", "hdfc", "infy"
        )
        
        val results = mutableListOf<TestResult>()
        
        println("=== STARTING COMPREHENSIVE API TESTS ===")
        
        for (query in testQueries) {
            val startTime = System.currentTimeMillis()
            val result = testSingleQuery(query)
            val endTime = System.currentTimeMillis()
            
            results.add(TestResult(
                query = query,
                success = result.success,
                results = result.results,
                error = result.error,
                responseTime = endTime - startTime
            ))
            
            println("TEST RESULT: '$query' -> Success: ${result.success}, Results: ${result.results}, Time: ${endTime - startTime}ms")
        }
        
        println("=== API TESTS COMPLETED ===")
        println("Total tests: ${results.size}")
        println("Successful: ${results.count { it.success }}")
        println("Failed: ${results.count { !it.success }}")
        
        return results
    }
    
    private suspend fun testSingleQuery(query: String): TestResult {
        return try {
            println("TESTING: '$query'")
            
            // Test NSE API
            val nseResponse = httpClient.get("https://www.nseindia.com/api/search/autocomplete") {
                parameter("q", query)
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                header("Accept", "application/json")
            }
            
            println("NSE Response Status: ${nseResponse.status}")
            
            if (nseResponse.status.value in 200..299) {
                val responseText = nseResponse.bodyAsText()
                println("NSE Response Length: ${responseText.length}")
                
                if (responseText.contains("\"symbols\"")) {
                    val symbolRegex = "\"symbol\":\\s*\"([^\"]+)\"".toRegex()
                    val symbols = symbolRegex.findAll(responseText).map { it.groupValues[1] }.toList()
                    println("NSE Found ${symbols.size} symbols: $symbols")
                    
                    TestResult(
                        query = query,
                        success = true,
                        results = symbols.size,
                        responseTime = 0
                    )
                } else {
                    TestResult(
                        query = query,
                        success = false,
                        results = 0,
                        error = "No symbols in response",
                        responseTime = 0
                    )
                }
            } else {
                TestResult(
                    query = query,
                    success = false,
                    results = 0,
                    error = "HTTP ${nseResponse.status.value}",
                    responseTime = 0
                )
            }
        } catch (e: Exception) {
            println("ERROR for '$query': ${e.message}")
            TestResult(
                query = query,
                success = false,
                results = 0,
                error = e.message,
                responseTime = 0
            )
        }
    }
    
    fun close() {
        httpClient.close()
    }
}
