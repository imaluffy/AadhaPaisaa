package com.aadhapaisa.shared.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class IsinToTickerService {
    
    actual suspend fun convertIsinToTicker(isin: String): String? {
        return withContext(Dispatchers.Main) {
            try {
                println("🔍 IsinToTickerService: Converting ISIN to ticker (iOS): $isin")
                // iOS implementation would go here
                // For now, return null as mock implementation
                return@withContext null
            } catch (e: Exception) {
                println("❌ IsinToTickerService: Error converting ISIN to ticker (iOS): ${e.message}")
                return@withContext null
            }
        }
    }
}
