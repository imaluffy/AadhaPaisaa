package com.aadhapaisa.shared.viewmodel

import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.models.PortfolioSummary
import com.aadhapaisa.shared.models.ConsolidatedHolding
import com.aadhapaisa.shared.repository.PortfolioRepository
import com.aadhapaisa.shared.service.MarketPriceUpdateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map

class StockViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val marketPriceUpdateService: MarketPriceUpdateService?
) {
    // Directly expose repository flows - no loading states
    val portfolioSummary: StateFlow<PortfolioSummary> = portfolioRepository.getPortfolioSummary().stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Eagerly, // Changed to Eagerly for immediate updates
        initialValue = PortfolioSummary(
            totalInvested = 0.0,
            currentValue = 0.0,
            profitLoss = 0.0,
            profitLossPercent = 0.0,
            totalStocks = 0,
            positiveStocks = 0,
            negativeStocks = 0
        )
    )

    val allHoldings: StateFlow<List<ConsolidatedHolding>> = portfolioRepository.getConsolidatedHoldings().stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Eagerly, // Changed to Eagerly for immediate updates
        initialValue = emptyList()
    ).also { flow ->
        // Debug: Log when holdings change
        CoroutineScope(Dispatchers.Main).launch {
            flow.collect { holdings ->
                println("🔄 StockViewModel: Holdings updated - ${holdings.size} holdings")
                holdings.forEach { holding ->
                    println("🔄 StockViewModel: - ${holding.stockSymbol}: ₹${holding.currentPrice}")
                }
            }
        }
    }

    val positiveHoldings: StateFlow<List<ConsolidatedHolding>> = allHoldings.let { holdingsFlow ->
        holdingsFlow.map { holdings ->
            holdings.filter { it.dynamicProfitLoss >= 0 }
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    val negativeHoldings: StateFlow<List<ConsolidatedHolding>> = allHoldings.let { holdingsFlow ->
        holdingsFlow.map { holdings ->
            holdings.filter { it.dynamicProfitLoss < 0 }
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private val _isDivided = MutableStateFlow(false)
    val isDivided: StateFlow<Boolean> = _isDivided.asStateFlow()

    // No loading states - always show UI immediately
    val isLoading: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    val error: StateFlow<String?> = MutableStateFlow<String?>(null).asStateFlow()

    fun loadData() {
        // No-op - data loads automatically via flows
    }

    fun togglePortfolioDivision() {
        _isDivided.value = !_isDivided.value
    }

    fun refreshData() {
        // Trigger manual price update
        marketPriceUpdateService?.let { service ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    service.updateAllHoldingsPrices()
                } catch (e: Exception) {
                    println("❌ StockViewModel: Error refreshing data - ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        // No-op - no errors to clear
    }
    
    /**
     * Start automatic price updates every 10 minutes
     */
    fun startPriceUpdates() {
        marketPriceUpdateService?.startPriceUpdates()
    }
    
    /**
     * Stop automatic price updates
     */
    fun stopPriceUpdates() {
        marketPriceUpdateService?.stopPriceUpdates()
    }
    
    /**
     * Check if price updates are currently running
     */
    fun isPriceUpdateRunning(): Boolean {
        return marketPriceUpdateService?.isRunning() ?: false
    }
    
    /**
     * Manually refresh portfolio data
     */
    fun refreshPortfolio() {
        println("🔄 StockViewModel: Manual refresh requested")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Force refresh by calling the repository refresh method
                if (portfolioRepository is com.aadhapaisa.shared.repository.DatabasePortfolioRepository) {
                    portfolioRepository.refreshHoldingsFromDatabase()
                    println("🔄 StockViewModel: Repository refresh completed")
                }
            } catch (e: Exception) {
                println("❌ StockViewModel: Error during manual refresh: ${e.message}")
            }
        }
    }
}
