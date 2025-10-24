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
                    
                    val rows = mutableListOf<ExcelRow>()
                    
                    for (rowIndex in 0..sheet.lastRowNum) {
                        val row: Row? = sheet.getRow(rowIndex)
                        if (row != null) {
                            val cells = mutableListOf<String>()
                            
                            for (cellIndex in 0 until row.lastCellNum) {
                                val cell: Cell? = row.getCell(cellIndex)
                                val cellValue = when (cell?.cellType) {
                                    org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
                                    org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
                                    org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
                                    org.apache.poi.ss.usermodel.CellType.FORMULA -> cell.cellFormula
                                    else -> ""
                                }
                                cells.add(cellValue)
                            }
                            
                            rows.add(ExcelRow(rowIndex + 1, cells))
                        }
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
