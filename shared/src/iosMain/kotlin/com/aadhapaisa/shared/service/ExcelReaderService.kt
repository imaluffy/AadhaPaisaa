package com.aadhapaisa.shared.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class ExcelReaderService {
    actual suspend fun readExcelFile(fileUri: String): ExcelData? {
        return withContext(Dispatchers.Main) {
            try {
                println("📊 ExcelReaderService: Starting to read Excel file: $fileUri")
                
                // For iOS, we'll implement this later
                // For now, return mock data
                val mockData = ExcelData(
                    fileName = "iOS Excel File",
                    sheets = listOf(
                        ExcelSheet(
                            name = "Portfolio",
                            rows = listOf(
                                ExcelRow(1, listOf("Symbol", "Company", "Quantity", "Buy Price", "Date")),
                                ExcelRow(2, listOf("AAPL", "Apple Inc.", "10", "150.00", "2024-01-15"))
                            )
                        )
                    )
                )
                
                println("📊 ExcelReaderService: Successfully parsed Excel file (iOS mock)")
                mockData
            } catch (e: Exception) {
                println("❌ ExcelReaderService: Error reading Excel file: ${e.message}")
                null
            }
        }
    }
}
