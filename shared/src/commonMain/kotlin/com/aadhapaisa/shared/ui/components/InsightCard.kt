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
import com.aadhapaisa.shared.models.Insight
import com.aadhapaisa.shared.models.InsightCategory
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography

@Composable
fun InsightCard(
    insight: Insight,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (insight.category) {
        InsightCategory.PERFORMANCE -> AppColors.Primary
        InsightCategory.SECTOR_ANALYSIS -> AppColors.Secondary
        InsightCategory.RISK_ASSESSMENT -> AppColors.Warning
        InsightCategory.OPPORTUNITY -> AppColors.Profit
        InsightCategory.WARNING -> AppColors.Error
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with category and confidence
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = insight.title,
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurface
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category badge
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = categoryColor.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = insight.category.name,
                                style = AppTypography.labelSmall,
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        // Confidence indicator
                        Text(
                            text = "${(insight.confidence * 100).toInt()}%",
                            style = AppTypography.labelSmall,
                            color = AppColors.SecondaryText
                        )
                    }
                }
                
                // Description
                Text(
                    text = insight.description,
                    style = AppTypography.bodyMedium,
                    color = AppColors.OnSurfaceVariant
                )
                
                // Related stocks
                if (insight.relatedStocks.isNotEmpty()) {
                    Text(
                        text = "Related: ${insight.relatedStocks.joinToString(", ")}",
                        style = AppTypography.bodySmall,
                        color = AppColors.SecondaryText
                    )
                }
            }
        }
    }
}

