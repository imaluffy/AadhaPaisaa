package com.aadhapaisa.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography

@Composable
fun StockCard(
    holding: Holding,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
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
                // Days held badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${holding.daysHeld}d",
                        style = AppTypography.labelMedium,
                        color = AppColors.OnPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Stock info
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = holding.stockSymbol,
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurface
                    )
                    
                    Text(
                        text = "Qty: ${holding.quantity}",
                        style = AppTypography.bodySmall,
                        color = AppColors.SecondaryText
                    )
                    
                    Text(
                        text = "₹${String.format("%.2f", holding.dynamicCurrentValue)}",
                        style = AppTypography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            holding.dynamicCurrentValue > holding.dynamicInvestedValue -> AppColors.Profit // Green for profit
                            holding.dynamicCurrentValue < holding.dynamicInvestedValue -> AppColors.Loss // Red for loss
                            else -> AppColors.Secondary // Blue for break-even
                        }
                    )
                    
                    Text(
                        text = "Invested: ₹${String.format("%.2f", holding.dynamicInvestedValue)}",
                        style = AppTypography.bodySmall,
                        color = AppColors.SecondaryText
                    )
                    
                    Text(
                        text = "${if (holding.dynamicProfitLoss >= 0) "+" else ""}${String.format("%.2f", holding.dynamicProfitLossPercent)}%",
                        style = AppTypography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (holding.dynamicProfitLoss >= 0) AppColors.Profit else AppColors.Loss
                    )
                }
            }
        }
    }
}
