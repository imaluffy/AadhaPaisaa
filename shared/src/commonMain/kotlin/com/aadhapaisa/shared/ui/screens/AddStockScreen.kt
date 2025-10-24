package com.aadhapaisa.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.unit.sp
import com.aadhapaisa.shared.api.StockSearchService
import com.aadhapaisa.shared.api.StockSearchResult
import com.aadhapaisa.shared.api.StockDetails
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography
import kotlinx.coroutines.launch

data class StockSuggestion(
    val symbol: String,
    val name: String,
    val currentPrice: Double
)

@Composable
fun AddStockScreen(
    onBack: () -> Unit,
    onSave: (String, String, Double, Double, Int, LocalDate, Double?) -> Unit,
    preFilledSymbol: String? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf(preFilledSymbol ?: "") }
    var selectedStock by remember { mutableStateOf<StockSearchResult?>(null) }
        var quantity by remember { mutableStateOf("") }
        var purchaseDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
        var purchasePrice by remember { mutableStateOf("") }
        var useMarketPrice by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }
        var targetPrice by remember { mutableStateOf("") }
        var sellingPrice by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<StockSearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var stockDetails by remember { mutableStateOf<StockDetails?>(null) }
    var isLoadingDetails by remember { mutableStateOf(false) }
    
        val searchService = remember { StockSearchService() }
        val coroutineScope = rememberCoroutineScope()
    
        // Search for stocks when query changes - minimum 3 characters
        // But don't search if a stock is already selected
        LaunchedEffect(searchQuery) {
            if (searchQuery.isNotEmpty() && searchQuery.length >= 3 && selectedStock == null) {
                isLoading = true
                showSuggestions = true
                try {
                    println("UI: Searching for: '$searchQuery' (${searchQuery.length} chars)")
                    suggestions = searchService.searchStocks(searchQuery)
                    println("UI: Found ${suggestions.size} results for '$searchQuery'")
                } catch (e: Exception) {
                    println("UI: Search error for '$searchQuery': ${e.message}")
                    suggestions = emptyList()
                }
                isLoading = false
            } else if (selectedStock != null) {
                // If a stock is selected, don't search
                suggestions = emptyList()
                showSuggestions = false
                isLoading = false
            } else {
                suggestions = emptyList()
                showSuggestions = false
                isLoading = false
            }
        }
    
        // Clean up when composable is disposed
        DisposableEffect(Unit) {
            onDispose {
                searchService.close()
            }
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.OnBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Stock",
                style = AppTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.OnBackground
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Search Stock",
                            style = AppTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.OnBackground
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                selectedStock = null
                            },
                            label = { 
                                Text(
                                    "Stock Symbol or Name",
                                    style = TextStyle(fontSize = 10.sp)
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                                trailingIcon = {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = AppColors.Primary
                                        )
                                    } else if (selectedStock != null) {
                                        IconButton(
                                            onClick = {
                                                selectedStock = null
                                                stockDetails = null
                                                searchQuery = ""
                                                suggestions = emptyList()
                                                showSuggestions = false
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = AppColors.SecondaryText
                                            )
                                        }
                                    }
                                },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = AppColors.OnBackground
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.SecondaryText
                            )
                        )
                        
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (showSuggestions) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (isLoading) {
                                // Loading state
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = AppColors.Primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Searching for '$searchQuery'...",
                                            style = AppTypography.bodyMedium,
                                            color = AppColors.SecondaryText
                                        )
                                    }
                                }
                            } else if (suggestions.isNotEmpty()) {
                                // Results found
                                LazyColumn(
                                    modifier = Modifier.height(200.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                        items(suggestions) { suggestion ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { 
                                                        selectedStock = suggestion
                                                        searchQuery = suggestion.symbol
                                                        showSuggestions = false
                                                        suggestions = emptyList() // Clear suggestions
                                                        isLoadingDetails = true
                                                        // Fetch detailed stock information with REAL data
                                                        coroutineScope.launch {
                                                            try {
                                                                // Get real stock details from Yahoo Finance
                                                                val realStockDetails = searchService.getStockDetailsFromYahoo(suggestion.symbol)
                                                                
                                                                if (realStockDetails != null) {
                                                                    stockDetails = realStockDetails
                                                                    selectedStock = suggestion.copy(currentPrice = realStockDetails.currentPrice)
                                                                    println("Real stock details loaded for ${suggestion.symbol}: ₹${realStockDetails.currentPrice}")
                                                                } else {
                                                                    // Fallback to suggestion data if API fails
                                                                    stockDetails = StockDetails(
                                                                        symbol = suggestion.symbol,
                                                                        name = suggestion.name,
                                                                        currentPrice = suggestion.currentPrice,
                                                                        exchange = "NSE",
                                                                        allTimeHigh = suggestion.currentPrice * 1.8,
                                                                        allTimeLow = suggestion.currentPrice * 0.4,
                                                                        dayHigh = suggestion.currentPrice * 1.08,
                                                                        dayLow = suggestion.currentPrice * 0.92,
                                                                        volume = 2500000L
                                                                    )
                                                                    selectedStock = suggestion
                                                                    println("Using fallback data for ${suggestion.symbol}: ₹${suggestion.currentPrice}")
                                                                }
                                                            } catch (e: Exception) {
                                                                println("Error fetching details for ${suggestion.symbol}: ${e.message}")
                                                                // Create stock details with real price from suggestion
                                                                val realPrice = suggestion.currentPrice
                                                                stockDetails = StockDetails(
                                                                    symbol = suggestion.symbol,
                                                                    name = suggestion.name,
                                                                    currentPrice = realPrice,
                                                                    exchange = "NSE",
                                                                    allTimeHigh = realPrice * 1.8,
                                                                    allTimeLow = realPrice * 0.4,
                                                                    dayHigh = realPrice * 1.08,
                                                                    dayLow = realPrice * 0.92,
                                                                    volume = 2500000L
                                                                )
                                                            }
                                                            isLoadingDetails = false
                                                        }
                                                    },
                                            colors = CardDefaults.cardColors(
                                                containerColor = AppColors.SurfaceVariant
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp)
                                            ) {
                                                Text(
                                                    text = suggestion.symbol,
                                                    style = AppTypography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppColors.OnSurface
                                                )
                                                Text(
                                                    text = suggestion.name,
                                                    style = AppTypography.bodySmall,
                                                    color = AppColors.SecondaryText
                                                )
                                                Text(
                                                    text = "₹${String.format("%.2f", suggestion.currentPrice)}",
                                                    style = AppTypography.bodySmall,
                                                    color = AppColors.Primary
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (searchQuery.length >= 3) {
                                // No results found
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No stocks found for '$searchQuery'",
                                        style = AppTypography.bodyMedium,
                                        color = AppColors.SecondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Stock Details Section
            if (isLoadingDetails) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = AppColors.Primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Loading stock details...",
                                    style = AppTypography.bodyMedium,
                                    color = AppColors.SecondaryText
                                )
                            }
                        }
                    }
                }
            } else if (stockDetails != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                // Stock Header
                                Column {
                                    Text(
                                        text = stockDetails!!.symbol,
                                        style = AppTypography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.OnBackground
                                    )
                                    Text(
                                        text = stockDetails!!.name,
                                        style = AppTypography.bodyMedium,
                                        color = AppColors.SecondaryText
                                    )
                                }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                                // Market Information Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Exchange and Market Price - Top Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "NSE",
                                                style = AppTypography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.Primary
                                            )
                                            Text(
                                                text = "Mkt Price - ₹${String.format("%.2f", stockDetails!!.currentPrice)}",
                                                style = AppTypography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.OnBackground
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // 52 Week Low and All Time High - Middle Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "52 Week Low",
                                                    style = AppTypography.bodySmall,
                                                    color = AppColors.SecondaryText
                                                )
                                                Text(
                                                    text = "₹${String.format("%.2f", stockDetails!!.allTimeLow)}",
                                                    style = AppTypography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AppColors.Error
                                                )
                                            }
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "All Time High",
                                                    style = AppTypography.bodySmall,
                                                    color = AppColors.SecondaryText
                                                )
                                                Text(
                                                    text = "₹${String.format("%.2f", stockDetails!!.allTimeHigh)}",
                                                    style = AppTypography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AppColors.Primary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Day High and Day Low - Bottom Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Day High",
                                                    style = AppTypography.bodySmall,
                                                    color = AppColors.SecondaryText
                                                )
                                                Text(
                                                    text = "₹${String.format("%.2f", stockDetails!!.dayHigh)}",
                                                    style = AppTypography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AppColors.OnSurface
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "Day Low",
                                                    style = AppTypography.bodySmall,
                                                    color = AppColors.SecondaryText
                                                )
                                                Text(
                                                    text = "₹${String.format("%.2f", stockDetails!!.dayLow)}",
                                                    style = AppTypography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AppColors.OnSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Form Fields
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Quantity") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = AppColors.SecondaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Purchase Price with Toggle
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Purchase Price",
                                            style = AppTypography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppColors.OnBackground
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Use Market Price",
                                                style = AppTypography.bodySmall,
                                                color = AppColors.SecondaryText
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Switch(
                                                checked = useMarketPrice,
                                                onCheckedChange = { 
                                                    useMarketPrice = it
                                                    if (it && stockDetails != null) {
                                                        purchasePrice = String.format("%.2f", stockDetails!!.currentPrice)
                                                    } else if (!it) {
                                                        purchasePrice = ""
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = AppColors.Primary,
                                                    checkedTrackColor = AppColors.Primary.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    OutlinedTextField(
                                        value = purchasePrice,
                                        onValueChange = { 
                                            purchasePrice = it
                                            useMarketPrice = false // Disable toggle when manually typing
                                        },
                                        label = { Text("Price you bought at") },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !useMarketPrice,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.Primary,
                                            unfocusedBorderColor = AppColors.SecondaryText
                                        )
                                    )
                                    
                                    if (useMarketPrice && stockDetails != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Using current market price: ₹${String.format("%.2f", stockDetails!!.currentPrice)}",
                                            style = AppTypography.bodySmall,
                                            color = AppColors.Primary
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true }
                            ) {
                                OutlinedTextField(
                                    value = purchaseDate.toString(),
                                    onValueChange = { /* Not directly editable */ },
                                    label = { Text("Purchase Date") },
                                    readOnly = true,
                                    enabled = false, // Make it look more like a button
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = AppColors.SecondaryText,
                                        disabledBorderColor = AppColors.SecondaryText,
                                        disabledTextColor = AppColors.OnSurface,
                                        disabledLabelColor = AppColors.SecondaryText
                                    ),
                                    trailingIcon = {
                                        Text(
                                            text = "📅",
                                            style = AppTypography.bodyLarge
                                        )
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Target Price with Percentage Options
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Target Price (Optional)",
                                        style = AppTypography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.OnBackground
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = targetPrice,
                                            onValueChange = { targetPrice = it },
                                            label = { Text("Enter target price") },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = if (targetPrice.isNotEmpty() && stockDetails != null) {
                                                    val targetValue = targetPrice.toDoubleOrNull() ?: 0.0
                                                    if (targetValue > stockDetails!!.currentPrice) AppColors.Primary else AppColors.Error
                                                } else AppColors.Primary,
                                                unfocusedBorderColor = if (targetPrice.isNotEmpty() && stockDetails != null) {
                                                    val targetValue = targetPrice.toDoubleOrNull() ?: 0.0
                                                    if (targetValue > stockDetails!!.currentPrice) AppColors.Primary else AppColors.Error
                                                } else AppColors.SecondaryText
                                            )
                                        )
                                        
                                        if (targetPrice.isNotEmpty() && stockDetails != null) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val currentPrice = stockDetails!!.currentPrice
                                            val targetPriceValue = targetPrice.toDoubleOrNull() ?: 0.0
                                            val percentageChange = if (currentPrice > 0) {
                                                ((targetPriceValue - currentPrice) / currentPrice) * 100
                                            } else 0.0
                                            
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (percentageChange >= 0) AppColors.Primary.copy(alpha = 0.2f) else AppColors.Error.copy(alpha = 0.2f)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "${if (percentageChange >= 0) "+" else ""}${String.format("%.1f", percentageChange)}%",
                                                    style = AppTypography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (percentageChange >= 0) AppColors.Primary else AppColors.Error,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Quick Set (from current price ₹${String.format("%.2f", stockDetails?.currentPrice ?: 0.0)})",
                                        style = AppTypography.bodySmall,
                                        color = AppColors.SecondaryText
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (stockDetails != null) {
                                                    targetPrice = String.format("%.2f", stockDetails!!.currentPrice * 1.10)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text("+10%", style = AppTypography.bodySmall, color = AppColors.OnPrimary)
                                        }
                                        Button(
                                            onClick = {
                                                if (stockDetails != null) {
                                                    targetPrice = String.format("%.2f", stockDetails!!.currentPrice * 1.15)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text("+15%", style = AppTypography.bodySmall, color = AppColors.OnPrimary)
                                        }
                                        Button(
                                            onClick = {
                                                if (stockDetails != null) {
                                                    targetPrice = String.format("%.2f", stockDetails!!.currentPrice * 1.20)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text("+20%", style = AppTypography.bodySmall, color = AppColors.OnPrimary)
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            // Selling Price with Percentage Options
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selling Price (Optional)",
                                        style = AppTypography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.OnBackground
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = sellingPrice,
                                            onValueChange = { sellingPrice = it },
                                            label = { Text("Enter selling price") },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = if (sellingPrice.isNotEmpty() && stockDetails != null) {
                                                    val sellingValue = sellingPrice.toDoubleOrNull() ?: 0.0
                                                    if (sellingValue < stockDetails!!.currentPrice) AppColors.Error else AppColors.Primary
                                                } else AppColors.Error,
                                                unfocusedBorderColor = if (sellingPrice.isNotEmpty() && stockDetails != null) {
                                                    val sellingValue = sellingPrice.toDoubleOrNull() ?: 0.0
                                                    if (sellingValue < stockDetails!!.currentPrice) AppColors.Error else AppColors.Primary
                                                } else AppColors.SecondaryText
                                            )
                                        )
                                        
                                        if (sellingPrice.isNotEmpty() && stockDetails != null) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val currentPrice = stockDetails!!.currentPrice
                                            val sellingPriceValue = sellingPrice.toDoubleOrNull() ?: 0.0
                                            val percentageChange = if (currentPrice > 0) {
                                                ((sellingPriceValue - currentPrice) / currentPrice) * 100
                                            } else 0.0
                                            
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (percentageChange >= 0) AppColors.Primary.copy(alpha = 0.2f) else AppColors.Error.copy(alpha = 0.2f)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "${if (percentageChange >= 0) "+" else ""}${String.format("%.1f", percentageChange)}%",
                                                    style = AppTypography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (percentageChange >= 0) AppColors.Primary else AppColors.Error,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Quick Set (from current price ₹${String.format("%.2f", stockDetails?.currentPrice ?: 0.0)})",
                                        style = AppTypography.bodySmall,
                                        color = AppColors.SecondaryText
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (stockDetails != null) {
                                                    sellingPrice = String.format("%.2f", stockDetails!!.currentPrice * 0.90)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text("-10%", style = AppTypography.bodySmall)
                                        }
                                        Button(
                                            onClick = {
                                                if (stockDetails != null) {
                                                    sellingPrice = String.format("%.2f", stockDetails!!.currentPrice * 0.85)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text("-15%", style = AppTypography.bodySmall)
                                        }
                                        Button(
                                            onClick = {
                                                if (stockDetails != null) {
                                                    sellingPrice = String.format("%.2f", stockDetails!!.currentPrice * 0.80)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text("-20%", style = AppTypography.bodySmall)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                    Button(
                                        onClick = {
                                            val qty = quantity.toIntOrNull() ?: 0
                                            val target = targetPrice.toDoubleOrNull()
                                            val purchase = if (useMarketPrice) {
                                                stockDetails!!.currentPrice
                                            } else {
                                                purchasePrice.toDoubleOrNull() ?: stockDetails!!.currentPrice
                                            }
                                            val current = stockDetails!!.currentPrice
                                            if (qty > 0) {
                                                onSave(stockDetails!!.symbol, stockDetails!!.name, purchase, current, qty, purchaseDate, target)
                                            }
                                        },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "Add to Portfolio",
                                        style = AppTypography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.OnPrimary
                                    )
                                }
                                
                                    Button(
                                        onClick = {
                                            selectedStock = null
                                            stockDetails = null
                                            searchQuery = ""
                                            quantity = ""
                                            purchasePrice = ""
                                            useMarketPrice = false
                                            targetPrice = ""
                                            sellingPrice = ""
                                        },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.SurfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "Cancel",
                                        style = AppTypography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.OnSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                selectedDate = purchaseDate,
                onDateSelected = { date ->
                    purchaseDate = date
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var tempDate by remember { mutableStateOf(selectedDate) }
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Purchase Date",
                    style = AppTypography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.OnSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Calendar Grid
                CalendarGrid(
                    selectedDate = tempDate,
                    onDateSelected = { date ->
                        if (date <= today) {
                            tempDate = date
                        }
                    },
                    today = today
                )
                
                // Warning for future dates
                if (tempDate > today) {
                    Text(
                        text = "⚠️ Future dates are not allowed",
                        style = AppTypography.bodySmall,
                        color = AppColors.Error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.SecondaryText
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = AppColors.OnSurface)
                    }
                    
                    Button(
                        onClick = {
                            if (tempDate <= today) {
                                onDateSelected(tempDate)
                            }
                        },
                        enabled = tempDate <= today,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    today: LocalDate
) {
    var currentMonth by remember { mutableStateOf(today.monthNumber) }
    var currentYear by remember { mutableStateOf(today.year) }
    
    // Get first day of month and number of days
    val firstDayOfMonth = LocalDate(currentYear, currentMonth, 1)
    val daysInMonth = firstDayOfMonth.month.length(currentYear % 4 == 0)
    val startDayOfWeek = (firstDayOfMonth.dayOfWeek.ordinal + 1) % 7 // Adjust to make Sunday = 0, Monday = 1, etc.
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month/Year Header with working navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous month button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { 
                        if (currentMonth == 1) {
                            currentMonth = 12
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = AppColors.OnSurface, style = AppTypography.titleLarge, fontWeight = FontWeight.Bold)
            }
            
            // Month and Year with clickable year dropdown
            var showYearDropdown by remember { mutableStateOf(false) }
            Box {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${firstDayOfMonth.month.name.uppercase()}",
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurface
                    )
                    
                    Text(
                        text = "$currentYear",
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary,
                        modifier = Modifier.clickable { showYearDropdown = true }
                    )
                }
                
                // Year dropdown
                if (showYearDropdown) {
                    Card(
                        modifier = Modifier
                            .offset(y = 40.dp)
                            .width(120.dp)
                            .height(200.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            val currentYearValue = today.year
                            val years = (currentYearValue - 20..currentYearValue).toList().reversed()
                            
                            items(years) { year ->
                                Text(
                                    text = year.toString(),
                                    style = AppTypography.bodyMedium,
                                    color = if (year == currentYear) AppColors.Primary else AppColors.OnSurface,
                                    fontWeight = if (year == currentYear) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            currentYear = year
                                            showYearDropdown = false
                                        }
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Next month button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { 
                        if (currentMonth == 12) {
                            currentMonth = 1
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("›", color = AppColors.OnSurface, style = AppTypography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Day headers (Sun, Mon, Tue, etc.)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    style = AppTypography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.SecondaryText,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Calendar days
        val totalWeeks = ((startDayOfWeek + daysInMonth - 1) / 7) + 1
        
        repeat(totalWeeks) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    val dayNumber = (week * 7) + dayOfWeek - startDayOfWeek + 1
                    val isCurrentMonth = dayNumber in 1..daysInMonth
                    val date = if (isCurrentMonth) {
                        LocalDate(currentYear, currentMonth, dayNumber)
                    } else null
                    
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val isFuture = date != null && date > today
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable(
                                enabled = isCurrentMonth && !isFuture
                            ) {
                                if (date != null && !isFuture) {
                                    onDateSelected(date)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrentMonth) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isToday -> AppColors.Primary // Highlight today's date
                                        isSelected -> AppColors.SecondaryText // Selected date in gray
                                        isFuture -> AppColors.Surface.copy(alpha = 0.3f)
                                        else -> AppColors.Surface
                                    }
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        style = AppTypography.bodyMedium,
                                        color = when {
                                            isToday -> AppColors.OnSurface // Today in white
                                            isSelected -> AppColors.OnSurface // Selected in white
                                            isFuture -> AppColors.SecondaryText.copy(alpha = 0.3f)
                                            else -> AppColors.OnSurface
                                        },
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
