package com.aadhapaisa.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aadhapaisa.shared.service.ExcelData
import com.aadhapaisa.shared.service.ExcelSheet
import com.aadhapaisa.shared.service.ExcelRow
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography

@Composable
fun ExcelDataDialog(
    isVisible: Boolean,
    excelData: ExcelData,
    onDismiss: () -> Unit,
    onCreateStockEntry: (() -> Unit)? = null
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .fillMaxHeight(0.95f)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Excel Data Preview",
                            style = AppTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.OnSurface
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = AppColors.OnSurface
                            )
                        }
                    }
                    
                    // File info
                    Text(
                        text = "File: ${excelData.fileName}",
                        style = AppTypography.bodyLarge,
                        color = AppColors.SecondaryText
                    )
                    
                    Text(
                        text = "Found ${excelData.sheets.size} sheet(s)",
                        style = AppTypography.bodyMedium,
                        color = AppColors.SecondaryText
                    )
                    
                    // Sheets content with horizontal scrolling
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(excelData.sheets) { sheet ->
                            HorizontalScrollableSheetPreview(sheet = sheet)
                        }
                    }
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Create Stock Entry button
                        if (onCreateStockEntry != null) {
                            Button(
                                onClick = onCreateStockEntry,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Secondary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "📈 Create Stock Entry",
                                    color = AppColors.OnPrimary,
                                    style = AppTypography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Close button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Close",
                                color = AppColors.OnPrimary,
                                style = AppTypography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalScrollableSheetPreview(sheet: ExcelSheet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sheet header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋",
                    style = AppTypography.titleLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sheet: ${sheet.name}",
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }
            
            Text(
                text = "${sheet.rows.size} rows",
                style = AppTypography.bodySmall,
                color = AppColors.SecondaryText
            )
            
            // Scrollable table with both horizontal and vertical scrolling
            ScrollableTable(rows = sheet.rows)
        }
    }
}

@Composable
private fun ScrollableTable(rows: List<ExcelRow>) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Scrollable content with both horizontal and vertical scrolling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp) // Limit height to enable vertical scrolling
                .verticalScroll(verticalScrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                // Table with increased minimum width to prevent text wrapping
                Column(
                    modifier = Modifier.widthIn(min = 1500.dp) // Increased from 1000.dp to accommodate longer stock names
                ) {
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (row.rowNumber == 11) AppColors.Primary.copy(alpha = 0.2f)
                                    else if (row.rowNumber >= 12) AppColors.Secondary.copy(alpha = 0.1f)
                                    else AppColors.Surface
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Row number
                            Text(
                                text = "${row.rowNumber}",
                                style = AppTypography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (row.rowNumber == 11) AppColors.Primary else AppColors.SecondaryText,
                                modifier = Modifier.width(40.dp)
                            )
                            
                            // Cells in horizontal layout
                            row.cells.forEach { cell ->
                                Card(
                                    modifier = Modifier
                                        .widthIn(min = 120.dp)
                                        .padding(horizontal = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (row.rowNumber == 11) 
                                            AppColors.Primary.copy(alpha = 0.3f)
                                        else AppColors.SurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = cell.ifEmpty { "-" },
                                        style = AppTypography.bodySmall,
                                        color = if (row.rowNumber == 11) AppColors.Primary else AppColors.OnSurface,
                                        fontWeight = if (row.rowNumber == 11) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Scroll indicators
        if (horizontalScrollState.maxValue > 0 || verticalScrollState.maxValue > 0) {
            Text(
                text = "← Scroll horizontally and vertically to see all data →",
                style = AppTypography.bodySmall,
                color = AppColors.SecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HorizontalScrollableTable(rows: List<ExcelRow>) {
    val horizontalScrollState = rememberScrollState()
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Horizontal scrollable content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
        ) {
            // Table with increased minimum width to prevent text wrapping
            Column(
                modifier = Modifier.widthIn(min = 1500.dp) // Increased from 1000.dp to accommodate longer stock names
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (row.rowNumber == 11) AppColors.Primary.copy(alpha = 0.2f)
                                else if (row.rowNumber == 12) AppColors.Secondary.copy(alpha = 0.1f)
                                else AppColors.Surface
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Row number
                        Text(
                            text = "${row.rowNumber}",
                            style = AppTypography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (row.rowNumber == 11) AppColors.Primary else AppColors.SecondaryText,
                            modifier = Modifier.width(40.dp)
                        )
                        
                        // Cells in horizontal layout
                        row.cells.forEach { cell ->
                            Card(
                                modifier = Modifier
                                    .widthIn(min = 120.dp)
                                    .padding(horizontal = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (row.rowNumber == 11) 
                                        AppColors.Primary.copy(alpha = 0.3f)
                                    else AppColors.SurfaceVariant
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = cell.ifEmpty { "-" },
                                    style = AppTypography.bodySmall,
                                    color = if (row.rowNumber == 11) AppColors.Primary else AppColors.OnSurface,
                                    fontWeight = if (row.rowNumber == 11) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(8.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Scroll indicator
        if (horizontalScrollState.canScrollForward || horizontalScrollState.canScrollBackward) {
            Text(
                text = "← Scroll horizontally to see all columns →",
                style = AppTypography.bodySmall,
                color = AppColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}