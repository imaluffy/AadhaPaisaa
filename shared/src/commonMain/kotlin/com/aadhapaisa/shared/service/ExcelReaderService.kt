package com.aadhapaisa.shared.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExcelRow(
    val rowNumber: Int,
    val cells: List<String>
)

data class ExcelSheet(
    val name: String,
    val rows: List<ExcelRow>
)

data class ExcelData(
    val fileName: String,
    val sheets: List<ExcelSheet>
)

expect class ExcelReaderService {
    suspend fun readExcelFile(fileUri: String): ExcelData?
}