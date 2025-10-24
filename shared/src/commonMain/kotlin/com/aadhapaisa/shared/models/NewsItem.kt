package com.aadhapaisa.shared.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class NewsItem(
    val id: String,
    val title: String,
    val source: String,
    val url: String,
    val summary: String,
    val publishedAt: Instant,
    val relatedStocks: List<String>,
    val sentiment: NewsSentiment? = null
)

@Serializable
enum class NewsSentiment {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

