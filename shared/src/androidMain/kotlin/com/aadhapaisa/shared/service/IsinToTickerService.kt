package com.aadhapaisa.shared.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

actual class IsinToTickerService {
    
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    actual suspend fun convertIsinToTicker(isin: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                println("🔍 IsinToTickerService: Converting ISIN to ticker: $isin")
                
                val request = listOf(OpenFigiRequest(
                    idType = "ID_ISIN",
                    idValue = isin
                ))
                
                val response: OpenFigiResponse = httpClient.post("https://api.openfigi.com/v3/mapping") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
                
                println("🔍 IsinToTickerService: OpenFIGI response: $response")
                
                val ticker = response.data?.firstOrNull()?.ticker
                if (ticker != null) {
                    println("🔍 IsinToTickerService: ✅ Found ticker: $ticker for ISIN: $isin")
                } else {
                    println("🔍 IsinToTickerService: ❌ No ticker found for ISIN: $isin")
                }
                
                return@withContext ticker
                
            } catch (e: Exception) {
                println("❌ IsinToTickerService: Error converting ISIN to ticker: ${e.message}")
                e.printStackTrace()
                return@withContext null
            }
        }
    }
}
