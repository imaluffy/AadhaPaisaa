package com.aadhapaisa.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aadhapaisa.shared.models.Insight
import com.aadhapaisa.shared.models.NewsItem
import com.aadhapaisa.shared.repository.InsightRepository
import com.aadhapaisa.shared.repository.NewsRepository
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography
import com.aadhapaisa.shared.ui.components.InsightCard
import com.aadhapaisa.shared.ui.components.NewsCard
import com.aadhapaisa.shared.viewmodel.InsightsViewModel
import kotlinx.coroutines.launch

@Composable
fun InsightsScreen(
    insightRepository: InsightRepository,
    newsRepository: NewsRepository,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { InsightsViewModel(insightRepository, newsRepository) }
    val insights by viewModel.insights.collectAsState()
    val news by viewModel.news.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            }
        } else {
            // Insights Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Insights",
                        style = AppTypography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnBackground
                    )
                    
                    Text(
                        text = "${insights.size} insights",
                        style = AppTypography.bodySmall,
                        color = AppColors.SecondaryText
                    )
                }
            }

            items(insights) { insight ->
                InsightCard(insight = insight)
            }

            // News Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Latest News",
                        style = AppTypography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnBackground
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${news.size} articles",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                        
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = AppColors.Primary
                            )
                        } else {
                            IconButton(
                                onClick = { viewModel.refreshNews() }
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                                    contentDescription = "Refresh News",
                                    tint = AppColors.Primary
                                )
                            }
                        }
                    }
                }
            }

            items(news) { newsItem ->
                NewsCard(
                    newsItem = newsItem,
                    onReadMore = onOpenUrl
                )
            }
        }

        error?.let { errorMessage ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Error.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = errorMessage,
                        style = AppTypography.bodyMedium,
                        color = AppColors.Error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
