package com.aadhapaisa.shared.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.models.PortfolioSummary
import com.aadhapaisa.shared.repository.*
import com.aadhapaisa.shared.database.DatabaseDriverFactory
import com.aadhapaisa.shared.service.MarketPriceUpdateService
import com.aadhapaisa.shared.api.StockSearchService
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import com.aadhapaisa.shared.theme.AadhaPaisaTheme
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.ui.screens.*

sealed class Screen(val title: String, val icon: ImageVector) {
    object Home : Screen("Home", Icons.Default.Home)
    object Stock : Screen("Portfolio", Icons.Default.List)
    object CustomGrouping : Screen("Groups", Icons.Default.Star)
    object Insights : Screen("Insights", Icons.Default.Settings)
    object AddStock : Screen("Add Stock", Icons.Default.Add)
}


@Composable
fun MainApp(
    onOpenUrl: (String) -> Unit = {},
    context: Any? = null
) {
    println("🚀 MainApp: Starting with context: ${context?.let { it::class.simpleName } ?: "null"}")
    
    AadhaPaisaTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var preFilledStockSymbol by remember { mutableStateOf<String?>(null) }
        
        // Initialize repositories - database storage
        val portfolioRepository = remember { 
            if (context != null) {
                DatabasePortfolioRepository(
                    DatabaseDriverFactory(context as android.content.Context)
                )
            } else {
                null
            }
        }
        
        // Initialize market price update service
        val stockSearchService = remember { StockSearchService() }
        val marketPriceUpdateService = remember { 
            portfolioRepository?.let { 
                MarketPriceUpdateService(it, stockSearchService) 
            }
        }
        
        // Initialize repository with context (only once when context is available)
        LaunchedEffect(context) {
            if (context != null && portfolioRepository != null) {
                println("🔧 MainApp: Initializing database repository")
                portfolioRepository.initialize()
                
                // Start automatic price updates when repository is initialized
                marketPriceUpdateService?.startPriceUpdates()
                println("🚀 MainApp: Started automatic price updates every 10 minutes")
            }
        }
        
        val insightRepository = remember { MockInsightRepository() }
        val newsRepository = remember { MockNewsRepository() }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = AppColors.Surface,
                    contentColor = AppColors.OnSurface
                ) {
                    listOf(Screen.Home, Screen.Stock, Screen.CustomGrouping, Screen.Insights).forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.Primary,
                                selectedTextColor = AppColors.Primary,
                                unselectedIconColor = AppColors.SecondaryText,
                                unselectedTextColor = AppColors.SecondaryText
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    is Screen.Home -> {
                        portfolioRepository?.let { repo ->
                            HomeScreen(
                                portfolioRepository = repo,
                                marketPriceUpdateService = marketPriceUpdateService,
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Initializing database...")
                            }
                        }
                    }
                    is Screen.Stock -> {
                        portfolioRepository?.let { repo ->
                            StockScreen(
                                portfolioRepository = repo,
                                marketPriceUpdateService = marketPriceUpdateService,
                                onAddStock = { currentScreen = Screen.AddStock },
                                onBuyMore = { symbol ->
                                    preFilledStockSymbol = symbol
                                    currentScreen = Screen.AddStock
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Initializing database...")
                            }
                        }
                    }
                    is Screen.CustomGrouping -> {
                        portfolioRepository?.let { repo ->
                            CustomGroupingScreen(
                                portfolioRepository = repo,
                                marketPriceUpdateService = marketPriceUpdateService,
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Initializing database...")
                            }
                        }
                    }
                    is Screen.AddStock -> {
                        portfolioRepository?.let { repo ->
                            AddStockScreen(
                                onBack = { 
                                    currentScreen = Screen.Stock
                                    preFilledStockSymbol = null
                                },
                                preFilledSymbol = preFilledStockSymbol,
                                onSave = { symbol, name, purchasePrice, currentPrice, quantity, date, target ->
                                    // Create new holding and save to repository
                                    // purchasePrice = what user paid, currentPrice = current market price
                                    // Convert LocalDate to Instant using epoch seconds
                                    val purchaseInstant = Instant.fromEpochSeconds(date.toEpochDays() * 86400L)
                                    println("🔍 DEBUG: User selected date: $date")
                                    println("🔍 DEBUG: Converted to instant: $purchaseInstant")
                                    
                                    val holding = Holding(
                                        stockSymbol = symbol,
                                        stockName = name,
                                        quantity = quantity,
                                        buyPrice = purchasePrice, // What the user actually paid
                                        purchaseDate = purchaseInstant, // Use the user-selected date
                                        currentPrice = currentPrice // Current market price from API
                                    ).calculateMetrics()
                                    
                                    println("💾 MainApp: Adding holding - ${holding.stockSymbol}: ${holding.stockName}")
                                    println("💾 MainApp: Days held: ${holding.daysHeld}")
                                    
                                    // Use coroutine scope to call suspend function
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            repo.addHolding(holding)
                                            println("✅ MainApp: Successfully added holding to repository")
                                            preFilledStockSymbol = null
                                            currentScreen = Screen.Stock
                                        } catch (e: Exception) {
                                            println("❌ MainApp: Error adding holding: ${e.message}")
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Initializing database...")
                            }
                        }
                    }
                    is Screen.Insights -> {
                        InsightsScreen(
                            insightRepository = insightRepository,
                            newsRepository = newsRepository,
                            onOpenUrl = onOpenUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
