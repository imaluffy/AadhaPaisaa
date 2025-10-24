package com.aadhapaisa.shared.service

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import java.io.InputStream

actual class ExcelReaderService {
    // Context is now managed by ContextManager
    
    private fun mapHeadersToFields(headers: List<String>): Map<String, Int> {
        val mapping = mutableMapOf<String, Int>()
        
        headers.forEachIndexed { index, header ->
            val cleanHeader = header.trim().lowercase()
            when {
                cleanHeader.contains("stock") && cleanHeader.contains("name") -> mapping["stock_name"] = index
                cleanHeader.contains("quantity") -> mapping["quantity"] = index
                cleanHeader.contains("avg") && cleanHeader.contains("buy") && cleanHeader.contains("price") -> mapping["avg_purchase_price"] = index
                cleanHeader.contains("buy") && cleanHeader.contains("value") -> mapping["invested_amount"] = index
                cleanHeader.contains("closing") && cleanHeader.contains("price") -> mapping["closing_mkt_price"] = index
                cleanHeader.contains("closing") && cleanHeader.contains("value") -> mapping["current_value"] = index
                cleanHeader.contains("unrealised") && cleanHeader.contains("p&l") -> mapping["profitloss"] = index
            }
        }
        
        println("📊 ExcelReaderService: Header mapping created: $mapping")
        return mapping
    }
    
    actual suspend fun readExcelFile(fileUri: String): ExcelData? {
        return withContext(Dispatchers.IO) {
            try {
                println("📊 ExcelReaderService: Starting to read Excel file: $fileUri")
                
                val context = ContextManager.getContext() as? Context
                if (context == null) {
                    println("❌ ExcelReaderService: Context is null")
                    return@withContext null
                }
                
                val uri = Uri.parse(fileUri)
                val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Could not open input stream for URI: $fileUri")
                
                println("📊 ExcelReaderService: Opened input stream successfully")
                
                val workbook = WorkbookFactory.create(inputStream)
                println("📊 ExcelReaderService: Created workbook, found ${workbook.numberOfSheets} sheets")
                
                val sheets = mutableListOf<ExcelSheet>()
                
                for (sheetIndex in 0 until workbook.numberOfSheets) {
                    val sheet: Sheet = workbook.getSheetAt(sheetIndex)
                    val sheetName = sheet.sheetName
                    println("📊 ExcelReaderService: Processing sheet: $sheetName")
                    println("📊 ExcelReaderService: Sheet has ${sheet.lastRowNum + 1} rows")
                    
                    val rows = mutableListOf<ExcelRow>()
                    
                    // Read from row 11 (index 10) onwards - this contains headers
                    val startRow = 10 // Row 11 in Excel (0-indexed)
                    val headerRow = sheet.getRow(startRow)
                    
                    if (headerRow != null) {
                        println("📊 ExcelReaderService: Found header row at index $startRow")
                        
                        // Read header row (row 11)
                        val headerCells = mutableListOf<String>()
                        for (cellIndex in 0 until headerRow.lastCellNum) {
                            val cell: Cell? = headerRow.getCell(cellIndex)
                            val cellValue = when (cell?.cellType) {
                                org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
                                org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
                                org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
                                org.apache.poi.ss.usermodel.CellType.FORMULA -> cell.cellFormula
                                else -> ""
                            }
                            headerCells.add(cellValue)
                        }
                        rows.add(ExcelRow(startRow + 1, headerCells))
                        
                        // Map the headers to our field names
                        val columnMapping = mapHeadersToFields(headerCells)
                        println("📊 ExcelReaderService: Column mapping: $columnMapping")
                        
                        // Read only the 12th row (index 11) - first data row
                        val dataRowIndex = 11 // Row 12 in Excel (0-indexed)
                        val dataRow = sheet.getRow(dataRowIndex)
                        
                        if (dataRow != null) {
                            println("📊 ExcelReaderService: Found data row at index $dataRowIndex")
                            
                            // Read the data row and show actual Excel values
                            val actualData = mutableListOf<String>()
                            
                            // Read all cells from the data row (row 12)
                            for (cellIndex in 0 until dataRow.lastCellNum) {
                                val cell: Cell? = dataRow.getCell(cellIndex)
                                val cellValue = when (cell?.cellType) {
                                    org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
                                    org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
                                    org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
                                    org.apache.poi.ss.usermodel.CellType.FORMULA -> cell.cellFormula
                                    else -> ""
                                }
                                actualData.add(cellValue)
                                println("📊 ExcelReaderService: Cell $cellIndex = $cellValue")
                            }
                            
                            // Extract stock data for automatic entry creation
                            if (actualData.size >= 6) {
                                val stockName = actualData[0] // Column 1: Stock Name
                                val stockSymbol = actualData[1] // Column 2: ISIN/Symbol (ADANIPOWER)
                                val quantity = actualData[2] // Column 3: Quantity
                                val avgPrice = actualData[3] // Column 4: Average buy price
                                
                                println("📊 ExcelReaderService: Extracted stock data:")
                                println("📊 ExcelReaderService: Stock Name: $stockName")
                                println("📊 ExcelReaderService: Stock Symbol: $stockSymbol")
                                println("📊 ExcelReaderService: Quantity: $quantity")
                                println("📊 ExcelReaderService: Avg Price: $avgPrice")
                                
                                // Store extracted data for processing (not displayed)
                                // This data will be used by StockEntryService
                            }
                            
                            rows.add(ExcelRow(dataRowIndex + 1, actualData))
                        } else {
                            println("❌ ExcelReaderService: No data row found at index $dataRowIndex")
                            rows.add(ExcelRow(dataRowIndex + 1, listOf("No data found")))
                        }
                    } else {
                        println("❌ ExcelReaderService: No header row found at index $startRow")
                        rows.add(ExcelRow(startRow + 1, listOf("No header found")))
                    }
                    
                    sheets.add(ExcelSheet(sheetName, rows))
                    println("📊 ExcelReaderService: Sheet '$sheetName' has ${rows.size} rows")
                }
                
                workbook.close()
                inputStream.close()
                
                val excelData = ExcelData(
                    fileName = uri.lastPathSegment ?: "Unknown",
                    sheets = sheets
                )
                
                println("📊 ExcelReaderService: Successfully parsed Excel file")
                println("📊 ExcelReaderService: Found ${excelData.sheets.size} sheets")
                
                excelData
            } catch (e: Exception) {
                println("❌ ExcelReaderService: Error reading Excel file: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
}
