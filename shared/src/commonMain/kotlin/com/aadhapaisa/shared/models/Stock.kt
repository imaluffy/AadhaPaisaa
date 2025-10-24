package com.aadhapaisa.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class Stock(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val changePercent: Double,
    val sector: String,
    val marketCap: Long? = null,
    val volume: Long? = null
)

