package com.aadhapaisa.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class PortfolioSummary(
    val totalInvested: Double,
    val currentValue: Double,
    val profitLoss: Double,
    val profitLossPercent: Double,
    val totalStocks: Int,
    val positiveStocks: Int,
    val negativeStocks: Int
) {
    val isProfit: Boolean get() = profitLoss >= 0
}

