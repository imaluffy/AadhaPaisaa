package com.aadhapaisa.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography

@Composable
fun RecentPurchaseCard(
    holding: Holding,
    displayMode: Int,
    onDisplayModeChange: (Int) -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // First card - Days (15% width, same height as right card)
        Card(
            modifier = Modifier
                .weight(0.15f)
                .height(IntrinsicSize.Min),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.SurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${holding.daysHeld}",
                    style = AppTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.OnSurface
                )
                Text(
                    text = "days",
                    style = AppTypography.labelSmall,
                    color = AppColors.OnSurface
                )
            }
        }
        
        // Second card - Stock info (85% width, same height as left card)
        Card(
            modifier = Modifier
                .weight(0.85f)
                .height(IntrinsicSize.Min),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.CardBackground
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AppColors.GradientStart,
                                AppColors.GradientEnd
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side - Stock name and quantity
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = holding.stockSymbol,
                            style = AppTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.OnSurface
                        )
                        
                        Text(
                            text = "${holding.quantity} shares",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                    }
                    
                    // Right side - Cycle through current/invested, profit/loss, and day change
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.clickable { onDisplayModeChange((displayMode + 1) % 3) }
                    ) {
                        when (displayMode) {
                            0 -> {
                                // Show current value and invested amount
                                Text(
                                    text = "₹${String.format("%.2f", holding.dynamicCurrentValue)}",
                                    style = AppTypography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        holding.dynamicCurrentValue > holding.dynamicInvestedValue -> AppColors.Profit // Green for profit
                                        holding.dynamicCurrentValue < holding.dynamicInvestedValue -> AppColors.Loss // Red for loss
                                        else -> AppColors.Secondary // Blue for break-even
                                    }
                                )
                                
                                Text(
                                    text = "₹${String.format("%.2f", holding.dynamicInvestedValue)}",
                                    style = AppTypography.bodySmall,
                                    color = AppColors.SecondaryText
                                )
                            }
                            1 -> {
                                // Show profit/loss and percentage
                                Text(
                                    text = "₹${String.format("%.2f", holding.dynamicProfitLoss)}",
                                    style = AppTypography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (holding.dynamicProfitLoss >= 0) AppColors.Profit else AppColors.Loss
                                )
                                
                                Text(
                                    text = "${if (holding.dynamicProfitLoss >= 0) "+" else ""}${String.format("%.2f", holding.dynamicProfitLossPercent)}%",
                                    style = AppTypography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (holding.dynamicProfitLoss >= 0) AppColors.Profit else AppColors.Loss
                                )
                            }
                            2 -> {
                                // Show total day change (per-share change × quantity) and percentage
                                val totalDayChange = holding.dayChange * holding.quantity
                                Text(
                                    text = "₹${String.format("%.2f", totalDayChange)}",
                                    style = AppTypography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (totalDayChange >= 0) AppColors.Profit else AppColors.Loss
                                )
                                
                                Text(
                                    text = "${if (holding.dayChange >= 0) "+" else ""}${String.format("%.2f", holding.dayChangePercent)}%",
                                    style = AppTypography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (holding.dayChange >= 0) AppColors.Profit else AppColors.Loss
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
