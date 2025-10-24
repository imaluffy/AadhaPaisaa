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
import com.aadhapaisa.shared.models.NewsItem
import com.aadhapaisa.shared.models.NewsSentiment
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun NewsCard(
    newsItem: NewsItem,
    onReadMore: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sentimentColor = when (newsItem.sentiment) {
        NewsSentiment.POSITIVE -> AppColors.PositiveSentiment
        NewsSentiment.NEGATIVE -> AppColors.NegativeSentiment
        NewsSentiment.NEUTRAL -> AppColors.NeutralSentiment
        null -> AppColors.SecondaryText
    }

    val timeAgo = try {
        val now = Clock.System.now()
        val published = newsItem.publishedAt
        val duration = now - published
        
        when {
            duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m ago"
            duration.inWholeHours < 24 -> "${duration.inWholeHours}h ago"
            else -> "${duration.inWholeDays}d ago"
        }
    } catch (e: Exception) {
        "Recently"
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
                // Header with source and time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = newsItem.source,
                        style = AppTypography.labelMedium,
                        color = AppColors.SecondaryText
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        newsItem.sentiment?.let { sentiment ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = sentimentColor.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = sentiment.name,
                                    style = AppTypography.labelSmall,
                                    color = sentimentColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = timeAgo,
                            style = AppTypography.labelSmall,
                            color = AppColors.SecondaryText
                        )
                    }
                }
                
                // Title
                Text(
                    text = newsItem.title,
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnSurface
                )
                
                // Summary
                Text(
                    text = newsItem.summary,
                    style = AppTypography.bodyMedium,
                    color = AppColors.OnSurfaceVariant
                )
                
                // Related stocks
                if (newsItem.relatedStocks.isNotEmpty()) {
                    Text(
                        text = "Related: ${newsItem.relatedStocks.joinToString(", ")}",
                        style = AppTypography.bodySmall,
                        color = AppColors.SecondaryText
                    )
                }
                
                // Read More button
                Button(
                    onClick = { onReadMore(newsItem.url) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Read More",
                        style = AppTypography.labelMedium,
                        color = AppColors.OnPrimary
                    )
                }
            }
        }
    }
}
