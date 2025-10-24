package com.aadhapaisa.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = "📊 Excel Data Preview",
                        style = AppTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurface
                    )
                    
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
                    
                    // Sheets content
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(excelData.sheets) { sheet ->
                            SheetPreview(sheet = sheet)
                        }
                    }
                    
                    // Close button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun SheetPreview(sheet: ExcelSheet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📋 Sheet: ${sheet.name}",
                style = AppTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
            
            Text(
                text = "${sheet.rows.size} rows",
                style = AppTypography.bodySmall,
                color = AppColors.SecondaryText
            )
            
            // Show first few rows as preview
            val previewRows = sheet.rows.take(5)
            previewRows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (row.rowNumber == 1) AppColors.Primary.copy(alpha = 0.2f) 
                            else AppColors.Surface
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${row.rowNumber}",
                        style = AppTypography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.SecondaryText,
                        modifier = Modifier.width(30.dp)
                    )
                    
                    row.cells.take(5).forEach { cell ->
                        Text(
                            text = cell.ifEmpty { "-" },
                            style = AppTypography.bodySmall,
                            color = if (row.rowNumber == 1) AppColors.Primary else AppColors.OnSurface,
                            fontWeight = if (row.rowNumber == 1) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (row.cells.size > 5) {
                        Text(
                            text = "...",
                            style = AppTypography.bodySmall,
                            color = AppColors.SecondaryText
                        )
                    }
                }
            }
            
            if (sheet.rows.size > 5) {
                Text(
                    text = "... and ${sheet.rows.size - 5} more rows",
                    style = AppTypography.bodySmall,
                    color = AppColors.SecondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
