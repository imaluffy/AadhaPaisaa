package com.aadhapaisa.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class Insight(
    val id: String,
    val title: String,
    val description: String,
    val relatedStocks: List<String>,
    val confidence: Double, // 0.0 to 1.0
    val category: InsightCategory,
    val timestamp: Long
)

@Serializable
enum class InsightCategory {
    PERFORMANCE,
    SECTOR_ANALYSIS,
    RISK_ASSESSMENT,
    OPPORTUNITY,
    WARNING
}

