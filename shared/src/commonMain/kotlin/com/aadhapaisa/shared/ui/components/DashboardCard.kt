package com.aadhapaisa.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography

@Composable
fun DashboardCard(
    title: String,
    value: String,
    subtitle: String? = null,
    isPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.CardBackground
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = AppTypography.titleMedium,
                    color = AppColors.SecondaryText
                )
                
                Text(
                    text = value,
                    style = AppTypography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.OnSurface
                )
                
                subtitle?.let {
                    Text(
                        text = it,
                        style = AppTypography.bodyMedium,
                        color = if (isPositive) AppColors.Profit else AppColors.Loss
                    )
                }
            }
        }
    }
}




