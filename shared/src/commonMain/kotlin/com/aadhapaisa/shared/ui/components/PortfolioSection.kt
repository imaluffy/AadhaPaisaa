package com.aadhapaisa.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aadhapaisa.shared.models.Holding
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography
import com.aadhapaisa.shared.theme.DashboardTypography

@Composable
fun PortfolioSection(
    title: String,
    holdings: List<Holding>,
    totalInvested: Double,
    currentValue: Double,
    totalGain: Double,
    totalGainPercent: Double,
    isPositive: Boolean,
    displayMode: Int,
    onDisplayModeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) AppColors.Profit.copy(alpha = 0.1f) else AppColors.Loss.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
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
                .padding(20.dp)
        ) {
            // Section Header
            Text(
                text = title,
                style = DashboardTypography.sectionHeader,
                color = AppColors.OnSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Mini Dashboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Invested",
                        style = DashboardTypography.label,
                        color = AppColors.SecondaryText
                    )
                    Text(
                        text = "₹${String.format("%.2f", totalInvested)}",
                        style = DashboardTypography.value,
                        color = AppColors.OnSurface
                    )
                }
                
                Column {
                    Text(
                        text = "Current Value",
                        style = DashboardTypography.label,
                        color = AppColors.SecondaryText
                    )
                    Text(
                        text = "₹${String.format("%.2f", currentValue)}",
                        style = DashboardTypography.value,
                        color = AppColors.OnSurface
                    )
                }
                
                Column {
                    Text(
                        text = if (isPositive) "Total Gain" else "Total Loss",
                        style = DashboardTypography.label,
                        color = AppColors.SecondaryText
                    )
                    Text(
                        text = "₹${String.format("%.2f", totalGain)}",
                        style = DashboardTypography.value,
                        color = if (isPositive) AppColors.Profit else AppColors.Loss
                    )
                    Text(
                        text = "${String.format("%.2f", totalGainPercent)}%",
                        style = DashboardTypography.percentage,
                        color = if (isPositive) AppColors.Profit else AppColors.Loss
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Holdings List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(holdings) { holding ->
                    RecentPurchaseCard(
                        holding = holding,
                        displayMode = displayMode,
                        onDisplayModeChange = onDisplayModeChange
                    )
                }
            }
        }
    }
}
