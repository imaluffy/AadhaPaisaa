package com.aadhapaisa.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.models.PortfolioSummary
import com.aadhapaisa.shared.models.ConsolidatedHolding
import com.aadhapaisa.shared.repository.PortfolioRepository
import com.aadhapaisa.shared.service.MarketPriceUpdateService
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography
import com.aadhapaisa.shared.theme.DashboardTypography
import com.aadhapaisa.shared.ui.components.SingleDashboardCard
import com.aadhapaisa.shared.ui.components.StockCard
import com.aadhapaisa.shared.viewmodel.StockViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun CustomGroupingScreen(
    portfolioRepository: PortfolioRepository,
    marketPriceUpdateService: MarketPriceUpdateService?,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { 
        marketPriceUpdateService?.let { 
            StockViewModel(portfolioRepository, it) 
        } ?: StockViewModel(portfolioRepository, null)
    }
    val allHoldings by viewModel.allHoldings.collectAsState()
    
    // State for selected stocks
    var selectedStockSymbols by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Calculate combined insights for selected stocks
    val selectedHoldings = allHoldings.filter { it.stockSymbol in selectedStockSymbols }
    val combinedInsights = remember(selectedHoldings) {
        if (selectedHoldings.isEmpty()) {
            null
        } else {
            val totalInvested = selectedHoldings.sumOf { it.dynamicInvestedValue }
            val totalCurrent = selectedHoldings.sumOf { it.dynamicCurrentValue }
            val totalProfitLoss = totalCurrent - totalInvested
            val totalProfitLossPercent = if (totalInvested > 0) (totalProfitLoss / totalInvested) * 100 else 0.0
            
            PortfolioSummary(
                totalInvested = totalInvested,
                currentValue = totalCurrent,
                profitLoss = totalProfitLoss,
                profitLossPercent = totalProfitLossPercent,
                totalStocks = selectedHoldings.size,
                positiveStocks = selectedHoldings.count { it.dynamicProfitLoss > 0 },
                negativeStocks = selectedHoldings.count { it.dynamicProfitLoss < 0 }
            )
        }
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            // Header
            Text(
                text = "Custom Stock Grouping",
                style = DashboardTypography.holdingsTitle,
                color = AppColors.OnBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        
        // Instructions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📊 Select stocks to analyze together",
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnSurface
                    )
                    Text(
                        text = "Choose any combination of your holdings to see combined insights, profit/loss, and performance metrics.",
                        style = AppTypography.bodyMedium,
                        color = AppColors.OnSurface
                    )
                }
            }
        }
        
        // Combined Insights for Selected Stocks
        combinedInsights?.let { insights ->
            item {
                Text(
                    text = "Selected Group Insights",
                    style = AppTypography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            
            item {
                val totalPortfolioValue = allHoldings.sumOf { it.dynamicInvestedValue }
                val allocationPercentage = if (totalPortfolioValue > 0) {
                    (insights.totalInvested / totalPortfolioValue) * 100
                } else 0.0
                
                SingleDashboardCard(
                    currentValue = insights.currentValue,
                    investedValue = insights.totalInvested,
                    profitLoss = insights.profitLoss,
                    profitLossPercent = insights.profitLossPercent,
                    isProfit = insights.isProfit,
                    allocationPercentage = allocationPercentage,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stocks Count
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${insights.totalStocks}",
                                    style = AppTypography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Stocks",
                                    style = AppTypography.bodySmall,
                                    color = AppColors.OnSurface
                                )
                            }
                        }
                    }
                    
                    // Positive Stocks
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${insights.positiveStocks}",
                                    style = AppTypography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Success
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Profitable",
                                    style = AppTypography.bodySmall,
                                    color = AppColors.OnSurface
                                )
                            }
                        }
                    }
                    
                    // Negative Stocks
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${insights.negativeStocks}",
                                    style = AppTypography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Loss",
                                    style = AppTypography.bodySmall,
                                    color = AppColors.OnSurface
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // All Holdings with Selection
        if (allHoldings.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Holdings",
                        style = AppTypography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnBackground
                    )
                    
                    // Select All / Clear All buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { 
                                selectedStockSymbols = allHoldings.map { it.stockSymbol }.toSet()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.Primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Select All",
                                style = AppTypography.bodySmall
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { selectedStockSymbols = emptySet() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.OnSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Clear All",
                                style = AppTypography.bodySmall
                            )
                        }
                    }
                }
            }
            
            items(allHoldings, key = { it.stockSymbol }) { consolidatedHolding ->
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
                
                StockSelectionCard(
                    holding = holding,
                    isSelected = holding.stockSymbol in selectedStockSymbols,
                    onSelectionChanged = { isSelected ->
                        selectedStockSymbols = if (isSelected) {
                            selectedStockSymbols + holding.stockSymbol
                        } else {
                            selectedStockSymbols - holding.stockSymbol
                        }
                    }
                )
            }
        } else {
            item {
                Text(
                    text = "No holdings available. Add some stocks to create custom groups!",
                    style = AppTypography.bodyLarge,
                    color = AppColors.OnBackground,
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockSelectionCard(
    holding: Holding,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.Primary.copy(alpha = 0.1f) else AppColors.Surface
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = { onSelectionChanged(!isSelected) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Selection Checkbox
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) AppColors.Primary else AppColors.OnSurface,
                modifier = Modifier.size(24.dp)
            )
            
            // Add spacing between icon and stock info
            Spacer(modifier = Modifier.width(12.dp))
            
            // Stock Info
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = holding.stockName,
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) AppColors.Primary else AppColors.OnSurface
                )
                Text(
                    text = "${holding.quantity} shares",
                    style = AppTypography.bodyMedium,
                    color = AppColors.OnSurface
                )
            }
            
            // Current Value
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₹${String.format("%.2f", holding.dynamicCurrentValue)}",
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (holding.dynamicProfitLoss >= 0) AppColors.Success else AppColors.Error
                )
                Text(
                    text = "${if (holding.dynamicProfitLoss >= 0) "+" else ""}₹${String.format("%.2f", holding.dynamicProfitLoss)}",
                    style = AppTypography.bodySmall,
                    color = if (holding.dynamicProfitLoss >= 0) AppColors.Success else AppColors.Error
                )
            }
        }
    }
}

@Composable
private fun PerformanceHighlights(
    selectedInsights: PortfolioSummary,
    totalPortfolioValue: Double,
    totalPortfolioProfit: Double,
    modifier: Modifier = Modifier
) {
    // Calculate portfolio percentages
    val portfolioPercentage = if (totalPortfolioValue > 0) {
        (selectedInsights.totalInvested / totalPortfolioValue) * 100
    } else 0.0
    
    val totalPortfolioProfitPercent = if (totalPortfolioValue > 0) {
        (totalPortfolioProfit / totalPortfolioValue) * 100
    } else 0.0
    
    // Generate intelligent highlights
    val highlights = remember(selectedInsights, portfolioPercentage, totalPortfolioProfitPercent) {
        buildList {
            // Portfolio allocation highlight
            if (portfolioPercentage > 0) {
                add(
                    HighlightItem(
                        icon = "📊",
                        title = "Portfolio Allocation",
                        description = "This group represents ${String.format("%.1f", portfolioPercentage)}% of your total portfolio",
                        color = AppColors.Primary
                    )
                )
            }
            
            // Performance comparison highlight
            if (selectedInsights.profitLossPercent != 0.0 && totalPortfolioProfitPercent != 0.0) {
                val performanceDifference = selectedInsights.profitLossPercent - totalPortfolioProfitPercent
                if (kotlin.math.abs(performanceDifference) > 5.0) { // Only show if difference is significant
                    add(
                        HighlightItem(
                            icon = if (performanceDifference > 0) "🚀" else "⚠️",
                            title = if (performanceDifference > 0) "Outperforming" else "Underperforming",
                            description = if (performanceDifference > 0) {
                                "This group is performing ${String.format("%.1f", performanceDifference)}% better than your overall portfolio"
                            } else {
                                "This group is performing ${String.format("%.1f", kotlin.math.abs(performanceDifference))}% worse than your overall portfolio"
                            },
                            color = if (performanceDifference > 0) AppColors.Success else AppColors.Error
                        )
                    )
                }
            }
            
            // High concentration highlight
            if (portfolioPercentage > 50.0) {
                add(
                    HighlightItem(
                        icon = "⚖️",
                        title = "High Concentration",
                        description = "This group makes up more than half of your portfolio - consider diversification",
                        color = AppColors.Warning
                    )
                )
            }
            
            // Small allocation highlight
            if (portfolioPercentage < 10.0 && selectedInsights.totalStocks > 0) {
                add(
                    HighlightItem(
                        icon = "💡",
                        title = "Small Allocation",
                        description = "This group represents less than 10% of your portfolio",
                        color = AppColors.Secondary
                    )
                )
            }
            
            // All profitable highlight
            if (selectedInsights.positiveStocks == selectedInsights.totalStocks && selectedInsights.totalStocks > 1) {
                add(
                    HighlightItem(
                        icon = "🎯",
                        title = "Perfect Performance",
                        description = "All ${selectedInsights.totalStocks} stocks in this group are profitable!",
                        color = AppColors.Success
                    )
                )
            }
            
            // All loss highlight
            if (selectedInsights.negativeStocks == selectedInsights.totalStocks && selectedInsights.totalStocks > 1) {
                add(
                    HighlightItem(
                        icon = "📉",
                        title = "All Losses",
                        description = "All ${selectedInsights.totalStocks} stocks in this group are at a loss",
                        color = AppColors.Error
                    )
                )
            }
            
            // Mixed performance highlight
            if (selectedInsights.positiveStocks > 0 && selectedInsights.negativeStocks > 0) {
                val profitRatio = (selectedInsights.positiveStocks.toDouble() / selectedInsights.totalStocks) * 100
                add(
                    HighlightItem(
                        icon = "🔄",
                        title = "Mixed Performance",
                        description = "${String.format("%.0f", profitRatio)}% of stocks are profitable, ${String.format("%.0f", 100 - profitRatio)}% are at a loss",
                        color = AppColors.Secondary
                    )
                )
            }
        }
    }
    
    if (highlights.isNotEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "💡 Performance Insights",
                style = AppTypography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.OnBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            highlights.forEach { highlight ->
                HighlightCard(highlight = highlight)
            }
        }
    }
}

@Composable
private fun HighlightCard(
    highlight: HighlightItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = highlight.color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = highlight.icon,
                style = AppTypography.titleLarge
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = highlight.title,
                    style = AppTypography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = highlight.color
                )
                Text(
                    text = highlight.description,
                    style = AppTypography.bodyMedium,
                    color = AppColors.OnSurface
                )
            }
        }
    }
}

private data class HighlightItem(
    val icon: String,
    val title: String,
    val description: String,
    val color: androidx.compose.ui.graphics.Color
)
