package com.aadhapaisa.shared.models

import kotlinx.datetime.Instant

data class ConsolidatedHolding(
    val stockSymbol: String,
    val stockName: String,
    val totalQuantity: Int,
    val totalInvestedValue: Double,
    val avgBuyPrice: Double,
    val firstPurchaseDate: Instant,
    val lastPurchaseDate: Instant,
    val currentPrice: Double = 0.0,
    val totalCurrentValue: Double = 0.0,
    val totalProfitLoss: Double = 0.0,
    val totalProfitLossPercent: Double = 0.0,
    val avgDaysHeld: Int = 0,
    val totalDayChange: Double = 0.0,
    val avgDayChangePercent: Double = 0.0
) {
    // Dynamic properties that are calculated on-the-fly
    val dynamicCurrentValue: Double
        get() = totalQuantity * currentPrice

    val dynamicInvestedValue: Double
        get() = totalInvestedValue

    val dynamicProfitLoss: Double
        get() = dynamicCurrentValue - dynamicInvestedValue

    val dynamicProfitLossPercent: Double
        get() = if (dynamicInvestedValue > 0) (dynamicProfitLoss / dynamicInvestedValue) * 100 else 0.0

    fun updateCurrentPrice(newCurrentPrice: Double): ConsolidatedHolding {
        return this.copy(
            currentPrice = newCurrentPrice,
            totalCurrentValue = totalQuantity * newCurrentPrice
        ).calculateMetrics()
    }

    private fun calculateMetrics(): ConsolidatedHolding {
        val newCurrentValue = totalQuantity * currentPrice
        val newProfitLoss = newCurrentValue - totalInvestedValue
        val newProfitLossPercent = if (totalInvestedValue > 0) (newProfitLoss / totalInvestedValue) * 100 else 0.0

        return this.copy(
            totalCurrentValue = newCurrentValue,
            totalProfitLoss = newProfitLoss,
            totalProfitLossPercent = newProfitLossPercent
        )
    }
}
