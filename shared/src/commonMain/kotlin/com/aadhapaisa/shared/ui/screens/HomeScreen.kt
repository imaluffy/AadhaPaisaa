package com.aadhapaisa.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.models.PortfolioSummary
import com.aadhapaisa.shared.models.ConsolidatedHolding
import com.aadhapaisa.shared.repository.PortfolioRepository
import com.aadhapaisa.shared.repository.SimplePersistentRepository
import com.aadhapaisa.shared.service.MarketPriceUpdateService
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography
import com.aadhapaisa.shared.ui.components.SingleDashboardCard
import com.aadhapaisa.shared.ui.components.RecentPurchaseCard
import com.aadhapaisa.shared.ui.components.PriceUpdateStatus
import com.aadhapaisa.shared.ui.components.ExcelImportDialog
import com.aadhapaisa.shared.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun HomeScreen(
    portfolioRepository: PortfolioRepository,
    marketPriceUpdateService: MarketPriceUpdateService?,
    onOpenFilePicker: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { 
        marketPriceUpdateService?.let { 
            HomeViewModel(portfolioRepository, it) 
        } ?: HomeViewModel(portfolioRepository, null)
    }
    val portfolioSummary by viewModel.portfolioSummary.collectAsState()
    val recentPurchases by viewModel.recentPurchases.collectAsState()
    val topPerformers by viewModel.topPerformers.collectAsState()
    val topLosers by viewModel.topLosers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isPriceUpdateRunning by remember { mutableStateOf(viewModel.isPriceUpdateRunning()) }
    
    // Shared display mode for all stock cards (0: current/invested, 1: profit/loss, 2: day change)
    var displayMode by remember { mutableStateOf(0) }
    
    // Excel import dialog state
    var showExcelImportDialog by remember { mutableStateOf(false) }
    
    
    // Get price update service state
    val isUpdating by marketPriceUpdateService?.isUpdating?.collectAsState() ?: remember { mutableStateOf(false) }
    val updateCount by marketPriceUpdateService?.updateCount?.collectAsState() ?: remember { mutableStateOf(0) }
    val lastUpdateTime by marketPriceUpdateService?.lastUpdateTime?.collectAsState() ?: remember { mutableStateOf(null) }
    
    // Force UI refresh when update count changes
    LaunchedEffect(updateCount) {
        if (updateCount > 0) {
            println("🔄 HomeScreen: Update count changed to $updateCount - UI should refresh")
        }
    }

    // No loading needed - data loads automatically

    // No error handling needed - data loads automatically

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            }
        } else {
            // Price Update Status
            item {
                PriceUpdateStatus(
                    isRunning = isPriceUpdateRunning,
                    isUpdating = isUpdating,
                    updateCount = updateCount,
                    lastUpdateTime = lastUpdateTime,
                    onManualRefresh = {
                        viewModel.refreshData()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Simple Refresh Button
            item {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                println("🔄 HomeScreen: Manual refresh triggered")
                                marketPriceUpdateService?.updateAllHoldingsPrices()
                            } catch (e: Exception) {
                                println("❌ HomeScreen: Manual refresh failed: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🔄 Refresh Stock Prices",
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnPrimary
                    )
                }
            }
            
            // Excel Import Button
            item {
                Button(
                    onClick = {
                        showExcelImportDialog = true
                        println("📊 HomeScreen: Excel import button clicked")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "📊 Import from Excel",
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnSurface
                    )
                }
            }
            
            // Clear All Data Button (for testing)
            item {
                Button(
                    onClick = {
                        try {
                            portfolioRepository.clearAllData()
                            println("✅ HomeScreen: All data cleared successfully")
                        } catch (e: Exception) {
                            println("❌ HomeScreen: Error clearing data: ${e.message}")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🗑️ Clear All Data (Test)",
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnSurface
                    )
                }
            }

            // Single Dashboard Card
            portfolioSummary?.let { summary ->
                item {
                    SingleDashboardCard(
                        currentValue = summary.currentValue,
                        investedValue = summary.totalInvested,
                        profitLoss = summary.profitLoss,
                        profitLossPercent = summary.profitLossPercent,
                        isProfit = summary.isProfit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }



            // Recent Purchases Section
            item {
                Text(
                    text = "Recent Purchases",
                    style = AppTypography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            items(recentPurchases) { holding ->
                RecentPurchaseCard(
                    holding = holding,
                    displayMode = displayMode,
                    onDisplayModeChange = { displayMode = it }
                )
            }

            // Top Performers Section (25%+ gain)
            if (topPerformers.isNotEmpty()) {
                item {
                    Text(
                        text = "🚀 Top Performers",
                        style = AppTypography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                items(topPerformers) { consolidatedHolding ->
                    // Convert ConsolidatedHolding to Holding for the original card
                    val holding = Holding(
                        stockSymbol = consolidatedHolding.stockSymbol,
                        stockName = consolidatedHolding.stockName,
                        quantity = consolidatedHolding.totalQuantity,
                        buyPrice = consolidatedHolding.avgBuyPrice,
                        purchaseDate = consolidatedHolding.firstPurchaseDate,
                        currentPrice = consolidatedHolding.currentPrice,
                        currentValue = consolidatedHolding.dynamicCurrentValue,
                        investedValue = consolidatedHolding.dynamicInvestedValue,
                        profitLoss = consolidatedHolding.dynamicProfitLoss,
                        profitLossPercent = consolidatedHolding.dynamicProfitLossPercent,
                        daysHeld = consolidatedHolding.avgDaysHeld,
                        dayChange = consolidatedHolding.totalDayChange,
                        dayChangePercent = consolidatedHolding.avgDayChangePercent
                    )
                    
                    RecentPurchaseCard(
                        holding = holding,
                        displayMode = displayMode,
                        onDisplayModeChange = { displayMode = it }
                    )
                }
            }

            // Top Losers Section (10%+ loss)
            if (topLosers.isNotEmpty()) {
                item {
                    Text(
                        text = "📉 Top Losers",
                        style = AppTypography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                items(topLosers) { consolidatedHolding ->
                    // Convert ConsolidatedHolding to Holding for the original card
                    val holding = Holding(
                        stockSymbol = consolidatedHolding.stockSymbol,
                        stockName = consolidatedHolding.stockName,
                        quantity = consolidatedHolding.totalQuantity,
                        buyPrice = consolidatedHolding.avgBuyPrice,
                        purchaseDate = consolidatedHolding.firstPurchaseDate,
                        currentPrice = consolidatedHolding.currentPrice,
                        currentValue = consolidatedHolding.dynamicCurrentValue,
                        investedValue = consolidatedHolding.dynamicInvestedValue,
                        profitLoss = consolidatedHolding.dynamicProfitLoss,
                        profitLossPercent = consolidatedHolding.dynamicProfitLossPercent,
                        daysHeld = consolidatedHolding.avgDaysHeld,
                        dayChange = consolidatedHolding.totalDayChange,
                        dayChangePercent = consolidatedHolding.avgDayChangePercent
                    )
                    
                    RecentPurchaseCard(
                        holding = holding,
                        displayMode = displayMode,
                        onDisplayModeChange = { displayMode = it }
                    )
                }
            }
        }

        error?.let { errorMessage ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Error.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = errorMessage,
                        style = AppTypography.bodyMedium,
                        color = AppColors.Error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        }
        
        // Excel Import Dialog
        if (showExcelImportDialog) {
            ExcelImportDialog(
                isVisible = showExcelImportDialog,
                onDismiss = { showExcelImportDialog = false },
                onFileSelected = { fileName ->
                    println("📊 HomeScreen: File selected: $fileName")
                    showExcelImportDialog = false
                },
                onOpenFilePicker = onOpenFilePicker
            )
        }
    }
}
