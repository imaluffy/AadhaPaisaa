package com.aadhapaisa.shared.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate

private fun formatDate(date: kotlinx.datetime.Instant): String {
    val localDate = date.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return "${localDate.dayOfMonth} ${monthNames[localDate.monthNumber - 1]} ${localDate.year}"
}

@Composable
fun StockDetailBottomSheet(
    holding: Holding?,
    onDismiss: () -> Unit,
    onBuy: (Holding) -> Unit = {},
    onSell: (Holding) -> Unit = {}
) {
    if (holding != null) {
        var isVisible by remember { mutableStateOf(false) }
        var isExpanded by remember { mutableStateOf(false) }
        var dragOffset by remember { mutableStateOf(0f) }
        
        // Calculate screen height for positioning
        val screenHeight = remember { 800f } // Approximate screen height
        val halfScreenHeight = screenHeight * 0.5f
        val fullScreenHeight = screenHeight * 0.9f // 90% of screen height - leave 10% at top
        
        // Calculate target position - start from bottom of screen
        val targetPosition = if (isExpanded) (screenHeight * 0.1f) else (screenHeight - halfScreenHeight)
        
        // Animate the bottom sheet position
        val animatedOffset by animateFloatAsState(
            targetValue = if (isVisible) targetPosition + dragOffset else screenHeight,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            ),
            label = "bottomSheetPosition"
        )
        
        // Animate backdrop opacity
        val backdropAlpha by animateFloatAsState(
            targetValue = if (isVisible) 0.5f else 0f,
            animationSpec = tween(300),
            label = "backdropAlpha"
        )
        
        // Trigger animation when holding becomes non-null
        LaunchedEffect(holding) {
            isVisible = true
        }
        
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Backdrop with fade animation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backdropAlpha))
                    .clickable { 
                        isVisible = false
                        onDismiss() 
                    }
            )
            
            // Bottom Sheet Content with collapsible behavior
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isExpanded) fullScreenHeight.dp else halfScreenHeight.dp)
                    .offset(y = animatedOffset.dp)
                    .background(
                        AppColors.Surface,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { /* Start of drag */ },
                            onDragEnd = {
                                // Snap to nearest position based on drag distance
                                val dragThreshold = 60f // Moderate threshold for better control
                                if (dragOffset < -dragThreshold) {
                                    isExpanded = true
                                } else if (dragOffset > dragThreshold) {
                                    isExpanded = false
                                }
                                dragOffset = 0f
                            }
                        ) { _, dragAmount ->
                            // More responsive drag logic
                            val newOffset = dragOffset + dragAmount.y
                            
                            if (!isExpanded) {
                                // When collapsed, only allow upward drag to expand
                                dragOffset = newOffset.coerceAtMost(0f)
                            } else {
                                // When expanded, allow both directions with better sensitivity
                                dragOffset = newOffset.coerceAtLeast(-30f) // Allow some upward drag
                            }
                        }
                    }
            ) {
                // Handle bar - clickable to toggle expansion
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                AppColors.SecondaryText.copy(alpha = 0.5f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
                
                // Content with conditional scroll capability
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(
                            state = rememberScrollState(),
                            enabled = isExpanded // Only allow scrolling when expanded
                        )
                ) {
                        StockDetailContent(
                            holding = holding,
                            onBuy = { onBuy(holding) },
                            onSell = { onSell(holding) },
                            onClose = {
                                isVisible = false
                                onDismiss()
                            }
                        )
                }
            }
        }
    }
}

@Composable
fun StockDetailContent(
    holding: Holding,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onClose: () -> Unit = {}
) {
    // Step 4: UI Re-render - Log values received by UI
    LaunchedEffect(holding) {
        println("🖥️ StockDetailBottomSheet: ===== STEP 4: UI RE-RENDER =====")
        println("🖥️ StockDetailBottomSheet: UI RECEIVED VALUES:")
        println("  - Symbol: ${holding.stockSymbol}")
        println("  - Current Price: ₹${holding.currentPrice}")
        println("  - Buy Price: ₹${holding.buyPrice}")
        println("  - Dynamic Current Value: ₹${holding.dynamicCurrentValue}")
        println("  - Dynamic Profit/Loss: ₹${holding.dynamicProfitLoss}")
        println("🖥️ StockDetailBottomSheet: UI WILL DISPLAY: Market Price = ₹${holding.currentPrice}")
        println("🖥️ StockDetailBottomSheet: ===== STEP 4 COMPLETE =====")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header with stock symbol, name, and close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stock info
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = holding.stockSymbol,
                    style = AppTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = holding.stockName,
                    style = AppTypography.bodyLarge,
                    color = AppColors.SecondaryText
                )
            }
            
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = AppColors.OnSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Divider(color = AppColors.SecondaryText.copy(alpha = 0.3f))
        
        // Price Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.Background),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Price Information",
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Buy Price",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "₹${String.format("%.2f", holding.buyPrice)}",
                            style = AppTypography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.OnSurface
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Market Price",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "₹${String.format("%.2f", holding.currentPrice)}",
                            style = AppTypography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (holding.currentPrice > holding.buyPrice) AppColors.Profit else AppColors.Loss
                        )
                    }
                }
            }
        }
        
        // Investment Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.Background),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Investment Summary",
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Invested",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "₹${String.format("%.2f", holding.investedValue)}",
                            style = AppTypography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.OnSurface
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Current Value",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "₹${String.format("%.2f", holding.currentValue)}",
                            style = AppTypography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (holding.currentValue > holding.investedValue) AppColors.Profit else AppColors.Loss
                        )
                    }
                }
                
                Divider(color = AppColors.SecondaryText.copy(alpha = 0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Profit/Loss",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "₹${String.format("%.2f", holding.profitLoss)}",
                            style = AppTypography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (holding.profitLoss >= 0) AppColors.Profit else AppColors.Loss
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Return %",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "${String.format("%.2f", holding.profitLossPercent)}%",
                            style = AppTypography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (holding.profitLossPercent >= 0) AppColors.Profit else AppColors.Loss
                        )
                    }
                }
            }
        }
        
        // Purchase Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.Background),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Purchase Information",
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Purchase Date",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = formatDate(holding.purchaseDate),
                            style = AppTypography.bodyMedium,
                            color = AppColors.OnSurface
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Days Held",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "${holding.daysHeld} days",
                            style = AppTypography.bodyMedium,
                            color = AppColors.OnSurface
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Quantity",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "${holding.quantity} shares",
                            style = AppTypography.bodyMedium,
                            color = AppColors.OnSurface
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Avg. Price",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "₹${String.format("%.2f", holding.buyPrice)}",
                            style = AppTypography.bodyMedium,
                            color = AppColors.OnSurface
                        )
                    }
                }
            }
        }
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBuy,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Profit
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Buy More",
                    color = AppColors.OnPrimary,
                    style = AppTypography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Button(
                onClick = onSell,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Loss
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Sell",
                    color = AppColors.OnPrimary,
                    style = AppTypography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Add some bottom padding for better spacing
        Spacer(modifier = Modifier.height(16.dp))
    }
}
