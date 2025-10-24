package com.aadhapaisa.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography
import com.aadhapaisa.shared.theme.DashboardTypography

@Composable
fun SingleDashboardCard(
    currentValue: Double,
    investedValue: Double,
    profitLoss: Double,
    profitLossPercent: Double,
    isProfit: Boolean,
    allocationPercentage: Double? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.CardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top row with Current Value and Allocation Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Current Value - Large and prominent
                    Text(
                        text = "₹${String.format("%.2f", currentValue)}",
                        style = DashboardTypography.currentValue,
                        color = AppColors.OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Allocation Percentage on the right
                    allocationPercentage?.let { percentage ->
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Allocation",
                                style = AppTypography.bodySmall,
                                color = AppColors.SecondaryText
                            )
                            Text(
                                text = "${String.format("%.1f", percentage)}%",
                                style = AppTypography.titleMedium,
                                color = AppColors.Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                // Bottom row with invested value on right, profit/loss on left
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left side - Profit/Loss and percentage
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "₹${String.format("%.2f", profitLoss)}",
                            style = DashboardTypography.profitLossAmount,
                            color = if (isProfit) AppColors.Profit else AppColors.Loss
                        )
                        Text(
                            text = "${String.format("%.2f", profitLossPercent)}%",
                            style = DashboardTypography.profitLossPercentage,
                            color = if (isProfit) AppColors.Profit else AppColors.Loss
                        )
                    }
                    
                    // Right side - Invested value
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Invested",
                            style = DashboardTypography.investedLabel,
                            color = AppColors.SecondaryText
                        )
                        Text(
                            text = "₹${String.format("%.2f", investedValue)}",
                            style = DashboardTypography.investedAmount,
                            color = AppColors.OnSurface
                        )
                    }
                }
            }
        }
    }
}
