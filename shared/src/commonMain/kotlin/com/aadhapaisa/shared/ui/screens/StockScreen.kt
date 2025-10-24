package com.aadhapaisa.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.models.PortfolioSummary
import com.aadhapaisa.shared.models.SortOption
import com.aadhapaisa.shared.models.SortDirection
import com.aadhapaisa.shared.models.ConsolidatedHolding
import com.aadhapaisa.shared.repository.PortfolioRepository
import com.aadhapaisa.shared.service.MarketPriceUpdateService
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography
import com.aadhapaisa.shared.theme.DashboardTypography
import com.aadhapaisa.shared.ui.components.DashboardCard
import com.aadhapaisa.shared.ui.components.SingleDashboardCard
import com.aadhapaisa.shared.ui.components.RecentPurchaseCard
import com.aadhapaisa.shared.ui.components.PortfolioDivisionCard
import com.aadhapaisa.shared.ui.components.StockDetailBottomSheet
import com.aadhapaisa.shared.viewmodel.StockViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun StockScreen(
    portfolioRepository: PortfolioRepository,
    marketPriceUpdateService: MarketPriceUpdateService?,
    onAddStock: () -> Unit = {},
    onBuyMore: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel = remember { 
        marketPriceUpdateService?.let { 
            StockViewModel(portfolioRepository, it) 
        } ?: StockViewModel(portfolioRepository, null)
    }
    val portfolioSummary by viewModel.portfolioSummary.collectAsState()
    val allHoldings by viewModel.allHoldings.collectAsState()
    val positiveHoldings by viewModel.positiveHoldings.collectAsState()
    
    // Toast state
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Collect toast messages
    LaunchedEffect(marketPriceUpdateService) {
        marketPriceUpdateService?.toastMessages?.collect { message ->
            toastMessage = message
            showToast = true
            delay(1000) // Show for 1 second
            showToast = false
        }
    }
    
    val negativeHoldings by viewModel.negativeHoldings.collectAsState()
    val isDivided by viewModel.isDivided.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Shared display mode for all stock cards (0: current/invested, 1: profit/loss, 2: day change)
    var displayMode by remember { mutableStateOf(0) }
    
    // Sort functionality
    var selectedSortOption by remember { mutableStateOf(SortOption.DAYS) }
    var selectedSortDirection by remember { mutableStateOf(SortDirection.DESCENDING) }
    var showSortBottomSheet by remember { mutableStateOf(false) }
    
    // Bottom sheet state - use symbol to reference live data
    var selectedHoldingSymbol by remember { mutableStateOf<String?>(null) }
    
    // Get the live holding data for the selected symbol
    val selectedHolding = remember(selectedHoldingSymbol, allHoldings) {
        println("🔄 StockScreen: selectedHolding recalculated")
        println("🔄 StockScreen: selectedHoldingSymbol = $selectedHoldingSymbol")
        println("🔄 StockScreen: allHoldings.size = ${allHoldings.size}")
        allHoldings.forEach { holding ->
            println("🔄 StockScreen: - ${holding.stockSymbol}: ₹${holding.currentPrice}")
        }
        
        val result = selectedHoldingSymbol?.let { symbol ->
            allHoldings.find { it.stockSymbol == symbol }
        }
        
        result?.let { holding ->
            println("🔄 StockScreen: selectedHolding found: ${holding.stockSymbol} with price ₹${holding.currentPrice}")
        }
        
        result
    }
    
    // Sort holdings based on selected option and direction
    val sortedHoldings = remember(allHoldings, selectedSortOption, selectedSortDirection) {
        val sorted = when (selectedSortOption) {
            SortOption.DAYS -> allHoldings.sortedBy { it.avgDaysHeld }
            SortOption.INVESTED -> allHoldings.sortedBy { it.dynamicInvestedValue }
            SortOption.PROFIT -> allHoldings.sortedBy { it.dynamicProfitLoss }
            SortOption.SHARES -> allHoldings.sortedBy { it.totalQuantity }
            SortOption.DAILY_PROFIT -> allHoldings.sortedBy { it.totalDayChange }
        }
        
        if (selectedSortDirection == SortDirection.DESCENDING) {
            sorted.reversed()
        } else {
            sorted
        }
    }

    // No loading needed - data loads automatically

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        item {
            // Header
            Text(
                text = "My Holdings",
                style = AppTypography.headlineSmall,
                color = AppColors.OnBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }

        // Single Portfolio Dashboard Card
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

        // Portfolio Division Card
        portfolioSummary?.let { summary ->
            item {
                PortfolioDivisionCard(
                    positiveCount = summary.positiveStocks,
                    negativeCount = summary.negativeStocks,
                    isDivided = isDivided,
                    onToggle = { viewModel.togglePortfolioDivision() }
                )
            }
        }

        if (isDivided) {
            // Positive Section
            val positiveTotalInvested = positiveHoldings.sumOf { it.dynamicInvestedValue }
            val positiveCurrentValue = positiveHoldings.sumOf { it.dynamicCurrentValue }
            val positiveTotalGain = positiveCurrentValue - positiveTotalInvested
            val positiveTotalGainPercent = if (positiveTotalInvested > 0) (positiveTotalGain / positiveTotalInvested) * 100 else 0.0

            item {
                Text(
                    text = "🟢 Positive Returns",
                    style = AppTypography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item {
                SingleDashboardCard(
                    currentValue = positiveCurrentValue,
                    investedValue = positiveTotalInvested,
                    profitLoss = positiveTotalGain,
                    profitLossPercent = positiveTotalGainPercent,
                    isProfit = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(positiveHoldings) { consolidatedHolding ->
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
                    onDisplayModeChange = { displayMode = it },
                    onClick = { selectedHoldingSymbol = holding.stockSymbol }
                )
            }

            // Negative Section
            val negativeTotalInvested = negativeHoldings.sumOf { it.dynamicInvestedValue }
            val negativeCurrentValue = negativeHoldings.sumOf { it.dynamicCurrentValue }
            val negativeTotalGain = negativeCurrentValue - negativeTotalInvested
            val negativeTotalGainPercent = if (negativeTotalInvested > 0) (negativeTotalGain / negativeTotalInvested) * 100 else 0.0

            item {
                Text(
                    text = "🔴 Negative Returns",
                    style = AppTypography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item {
                SingleDashboardCard(
                    currentValue = negativeCurrentValue,
                    investedValue = negativeTotalInvested,
                    profitLoss = negativeTotalGain,
                    profitLossPercent = negativeTotalGainPercent,
                    isProfit = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(negativeHoldings) { consolidatedHolding ->
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
                    onDisplayModeChange = { displayMode = it },
                    onClick = { selectedHoldingSymbol = holding.stockSymbol }
                )
            }
        } else {
            // Combined View with Sort functionality
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "All Holdings",
                        style = AppTypography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnBackground
                    )
                    
                        // Refresh Button (Icon only)
                        IconButton(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        println("🔄 StockScreen: Manual refresh triggered")
                                        marketPriceUpdateService?.updateAllHoldingsPrices()
                                    } catch (e: Exception) {
                                        println("❌ StockScreen: Manual refresh failed: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = AppColors.OnBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Sort Button
                        OutlinedButton(
                        onClick = { showSortBottomSheet = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.OnSurface
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = selectedSortOption.displayName,
                                style = AppTypography.bodySmall
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Sort",
                                modifier = Modifier.size(16.dp)
                            )
                    }
                }
            }

            items(sortedHoldings) { consolidatedHolding ->
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
                    onDisplayModeChange = { displayMode = it },
                    onClick = { selectedHoldingSymbol = holding.stockSymbol }
                )
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
        
        // FloatingActionButton positioned in top right
        FloatingActionButton(
            onClick = onAddStock,
            containerColor = AppColors.Primary,
            contentColor = AppColors.OnPrimary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Stock"
            )
        }
    }
    
    // Bottom Sheet for Stock Details
    selectedHolding?.let { consolidatedHolding ->
        // Convert ConsolidatedHolding to Holding for the bottom sheet
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
        
    StockDetailBottomSheet(
            holding = holding,
            onDismiss = { selectedHoldingSymbol = null },
            onBuy = { _ ->
                onBuyMore(consolidatedHolding.stockSymbol)
                selectedHoldingSymbol = null
            },
            onSell = { _ ->
            // TODO: Implement sell functionality
                selectedHoldingSymbol = null
            }
        )
    }
    
    // Sort Bottom Sheet
    if (showSortBottomSheet) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                .clickable { showSortBottomSheet = false },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 0.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sort Holdings",
                        style = AppTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Sort Options
                    SortOption.values().forEach { option ->
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSortOption = option }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedSortOption == option,
                                    onClick = { selectedSortOption = option },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AppColors.Primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = option.displayName,
                                    style = AppTypography.bodyLarge,
                                    color = AppColors.OnSurface
                                )
                            }
                            
                            // Show sort direction options only for the selected option
                            if (selectedSortOption == option) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 40.dp, top = 2.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SortDirection.values().forEach { direction ->
                                        OutlinedButton(
                                            onClick = { selectedSortDirection = direction },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = if (selectedSortDirection == direction) AppColors.Primary else AppColors.OnSurface,
                                                containerColor = if (selectedSortDirection == direction) AppColors.Primary.copy(alpha = 0.1f) else AppColors.Surface
                                            ),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                                width = if (selectedSortDirection == direction) 2.dp else 1.dp
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = direction.displayName,
                                                style = AppTypography.bodySmall,
                                                color = if (selectedSortDirection == direction) AppColors.Primary else AppColors.OnSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Apply Button
                    Button(
                        onClick = { showSortBottomSheet = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary
                        )
                    ) {
                        Text(
                            text = "Apply",
                            style = AppTypography.titleMedium,
                            color = AppColors.OnPrimary
                        )
                    }
                }
            }
        }
    }
    
    // Toast Display at bottom
    if (showToast) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(bottom = 100.dp), // Position above bottom navigation
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = toastMessage,
                    style = AppTypography.bodyLarge,
                    color = AppColors.OnSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
