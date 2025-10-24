package com.aadhapaisa.shared.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PreFilledStockData(
    val stockSymbol: String,
    val quantity: String,
    val avgPrice: String
)

object ExcelDataManager {
    private val _preFilledData = MutableStateFlow<PreFilledStockData?>(null)
    val preFilledData: StateFlow<PreFilledStockData?> = _preFilledData.asStateFlow()
    
    fun setPreFilledData(stockSymbol: String, quantity: String, avgPrice: String) {
        println("📊 ExcelDataManager: Setting pre-filled data - Symbol: $stockSymbol, Qty: $quantity, Price: $avgPrice")
        _preFilledData.value = PreFilledStockData(stockSymbol, quantity, avgPrice)
    }
    
    fun getPreFilledData(): PreFilledStockData? {
        return _preFilledData.value
    }
    
    fun clearPreFilledData() {
        println("📊 ExcelDataManager: Clearing pre-filled data")
        _preFilledData.value = null
    }
}
