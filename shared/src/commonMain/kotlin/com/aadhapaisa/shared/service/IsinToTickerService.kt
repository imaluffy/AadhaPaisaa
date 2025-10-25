package com.aadhapaisa.shared.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class OpenFigiRequest(
    val idType: String,
    val idValue: String
)

@Serializable
data class OpenFigiResponse(
    val data: List<OpenFigiData>? = null
)

@Serializable
data class OpenFigiData(
    val figi: String? = null,
    val name: String? = null,
    val ticker: String? = null,
    val exchCode: String? = null,
    val compositeFIGI: String? = null,
    val uniqueID: String? = null,
    val shareClassFIGI: String? = null,
    val securityType: String? = null,
    val marketSector: String? = null,
    val securityDescription: String? = null
)

expect class IsinToTickerService {
    suspend fun convertIsinToTicker(isin: String): String?
}
