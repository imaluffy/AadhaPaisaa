package com.aadhapaisa.shared.ai

import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.models.Insight
import com.aadhapaisa.shared.models.InsightCategory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface AiInsightService {
    suspend fun generateInsights(holdings: List<Holding>): List<Insight>
    suspend fun analyzePortfolioRisk(holdings: List<Holding>): List<Insight>
    suspend fun suggestRebalancing(holdings: List<Holding>): List<Insight>
}

class OpenAiInsightService(
    private val apiKey: String
) : AiInsightService {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    override suspend fun generateInsights(holdings: List<Holding>): List<Insight> {
        return try {
            val prompt = buildPortfolioAnalysisPrompt(holdings)
            val response = callOpenAiApi(prompt)
            parseInsightsFromResponse(response)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun analyzePortfolioRisk(holdings: List<Holding>): List<Insight> {
        return try {
            val prompt = buildRiskAnalysisPrompt(holdings)
            val response = callOpenAiApi(prompt)
            parseInsightsFromResponse(response)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun suggestRebalancing(holdings: List<Holding>): List<Insight> {
        return try {
            val prompt = buildRebalancingPrompt(holdings)
            val response = callOpenAiApi(prompt)
            parseInsightsFromResponse(response)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun buildPortfolioAnalysisPrompt(holdings: List<Holding>): String {
        val portfolioSummary = buildPortfolioSummary(holdings)
        return """
            Analyze the following stock portfolio and provide AI-driven insights:
            
            Portfolio Summary:
            - Total Invested: ₹${holdings.sumOf { it.investedValue }}
            - Current Value: ₹${holdings.sumOf { it.currentValue }}
            - Total P&L: ₹${holdings.sumOf { it.profitLoss }}
            - Number of Holdings: ${holdings.size}
            
            Holdings:
            ${holdings.joinToString("\n") { 
                "- ${it.stockSymbol}: ${it.quantity} shares @ ₹${it.buyPrice} (Current: ₹${it.currentPrice}, P&L: ${it.profitLossPercent}%)"
            }}
            
            Please provide 3-5 actionable insights focusing on:
            1. Performance analysis
            2. Sector diversification
            3. Risk assessment
            4. Opportunities for improvement
            5. Market trends affecting the portfolio
            
            Format the response as JSON with insights containing title, description, relatedStocks, confidence, and category.
        """.trimIndent()
    }
    
    private fun buildRiskAnalysisPrompt(holdings: List<Holding>): String {
        return """
            Analyze the risk profile of this portfolio:
            
            ${holdings.joinToString("\n") { 
                "- ${it.stockSymbol}: ${it.quantity} shares, ${it.profitLossPercent}% P&L"
            }}
            
            Provide risk assessment insights focusing on:
            1. Concentration risk
            2. Sector exposure
            3. Volatility analysis
            4. Correlation risks
            5. Recommendations for risk mitigation
        """.trimIndent()
    }
    
    private fun buildRebalancingPrompt(holdings: List<Holding>): String {
        return """
            Suggest portfolio rebalancing strategies for:
            
            ${holdings.joinToString("\n") { 
                "- ${it.stockSymbol}: ${it.quantity} shares, ${it.profitLossPercent}% P&L"
            }}
            
            Consider:
            1. Current allocation vs optimal allocation
            2. Tax implications
            3. Market conditions
            4. Risk tolerance
            5. Specific buy/sell recommendations
        """.trimIndent()
    }
    
    private suspend fun callOpenAiApi(prompt: String): String {
        val requestBody = buildJsonObject {
            put("model", "gpt-3.5-turbo")
            put("messages", buildJsonObject {
                put("role", "user")
                put("content", prompt)
            })
            put("max_tokens", 1000)
            put("temperature", 0.7)
        }
        
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(requestBody)
        }
        
        return response.bodyAsText()
    }
    
    private fun parseInsightsFromResponse(response: String): List<Insight> {
        // Parse OpenAI response and convert to Insight objects
        // This is a simplified implementation
        return emptyList() // Placeholder for now
    }
    
    private fun buildPortfolioSummary(holdings: List<Holding>): String {
        val totalInvested = holdings.sumOf { it.investedValue }
        val currentValue = holdings.sumOf { it.currentValue }
        val totalPnL = currentValue - totalInvested
        val totalPnLPercent = if (totalInvested > 0) (totalPnL / totalInvested) * 100 else 0.0
        
        return """
            Total Invested: ₹${String.format("%.2f", totalInvested)}
            Current Value: ₹${String.format("%.2f", currentValue)}
            Total P&L: ₹${String.format("%.2f", totalPnL)} (${String.format("%.2f", totalPnLPercent)}%)
        """.trimIndent()
    }
}

class GeminiInsightService(
    private val apiKey: String
) : AiInsightService {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    override suspend fun generateInsights(holdings: List<Holding>): List<Insight> {
        return try {
            // Google Gemini API implementation
            emptyList() // Placeholder for now
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun analyzePortfolioRisk(holdings: List<Holding>): List<Insight> {
        return try {
            emptyList() // Placeholder for now
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun suggestRebalancing(holdings: List<Holding>): List<Insight> {
        return try {
            emptyList() // Placeholder for now
        } catch (e: Exception) {
            emptyList()
        }
    }
}


