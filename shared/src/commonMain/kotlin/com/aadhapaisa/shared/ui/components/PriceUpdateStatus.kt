package com.aadhapaisa.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aadhapaisa.shared.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun PriceUpdateStatus(
    isRunning: Boolean,
    isUpdating: Boolean = false,
    updateCount: Int = 0,
    lastUpdateTime: kotlinx.datetime.Instant? = null,
    onManualRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    
    Column(
        modifier = modifier
            .background(
                color = if (isRunning) AppColors.Success.copy(alpha = 0.1f) else AppColors.Surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = when {
                            isUpdating -> AppColors.Primary
                            isRunning -> AppColors.Success
                            else -> AppColors.SecondaryText
                        },
                        shape = CircleShape
                    )
            )
            
            // Status text
            Text(
                text = when {
                    isUpdating -> "Updating prices..."
                    isRunning -> "Live prices updating (20s)"
                    else -> "Prices static"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isUpdating -> AppColors.Primary
                    isRunning -> AppColors.Success
                    else -> AppColors.SecondaryText
                },
                fontWeight = FontWeight.Medium
            )
            
            // Manual refresh button
            IconButton(
                onClick = onManualRefresh,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh prices",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Additional info row
        if (isRunning) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Updates: $updateCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.SecondaryText,
                    fontSize = 10.sp
                )
                
                lastUpdateTime?.let { updateTime ->
                    Text(
                        text = "Last: ${formatTimeAgo(updateTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.SecondaryText,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: kotlinx.datetime.Instant): String {
    val now = Clock.System.now()
    val diff = now - timestamp
    
    return when {
        diff.inWholeMinutes < 1 -> "just now"
        diff.inWholeMinutes < 60 -> "${diff.inWholeMinutes}m ago"
        diff.inWholeHours < 24 -> "${diff.inWholeHours}h ago"
        else -> "${diff.inWholeDays}d ago"
    }
}
