package com.aadhapaisa.shared.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Holding(
    val stockSymbol: String,
    val stockName: String = "",
    val quantity: Int,
    val buyPrice: Double,
    val purchaseDate: Instant,
    val currentPrice: Double = 0.0,
    val currentValue: Double = 0.0,
    val investedValue: Double = 0.0,
    val profitLoss: Double = 0.0,
    val profitLossPercent: Double = 0.0,
    val daysHeld: Int = 0,
    val dayChange: Double = 0.0,
    val dayChangePercent: Double = 0.0
) {
    // Dynamic calculation properties - these are calculated on-the-fly, not stored
    val dynamicCurrentValue: Double get() = quantity * currentPrice
    val dynamicInvestedValue: Double get() = quantity * buyPrice
    val dynamicProfitLoss: Double get() = dynamicCurrentValue - dynamicInvestedValue
    val dynamicProfitLossPercent: Double get() = if (dynamicInvestedValue > 0) (dynamicProfitLoss / dynamicInvestedValue) * 100 else 0.0
    
    fun calculateMetrics(): Holding {
        val currentVal = dynamicCurrentValue
        val investedVal = dynamicInvestedValue
        val profitLoss = dynamicProfitLoss
        val profitLossPercent = dynamicProfitLossPercent
        
        // Calculate days held
        val now = kotlinx.datetime.Clock.System.now()
        val daysHeld = (now - purchaseDate).inWholeDays.toInt() + 1
        
        return copy(
            currentValue = currentVal,
            investedValue = investedVal,
            profitLoss = profitLoss,
            profitLossPercent = profitLossPercent,
            daysHeld = daysHeld
        )
    }
    
    // Method to update only the current price and recalculate everything dynamically
    fun updateCurrentPrice(newCurrentPrice: Double): Holding {
        return copy(currentPrice = newCurrentPrice).calculateMetrics()
    }
}
