package com.aadhapaisa.shared.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StockEntryData(
    val stockName: String,
    val stockSymbol: String,
    val quantity: String,
    val avgPrice: String
)

expect class StockEntryService {
    suspend fun createStockEntryFromExcel(excelData: ExcelData): Boolean
    suspend fun searchAndCreateEntry(stockSymbol: String, quantity: String, avgPrice: String): Boolean
}
