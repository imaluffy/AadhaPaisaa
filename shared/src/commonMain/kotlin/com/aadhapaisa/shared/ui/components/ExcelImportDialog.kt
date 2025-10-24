package com.aadhapaisa.shared.ui.components

import androidx.compose.foundation.layout.*
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
import com.aadhapaisa.shared.theme.AppColors
import com.aadhapaisa.shared.theme.AppTypography

@Composable
fun ExcelImportDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onFileSelected: (String) -> Unit,
    onOpenFilePicker: (() -> Unit)? = null,
    selectedFileName: String? = null
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
                    .padding(horizontal = 0.dp, vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                shape = RoundedCornerShape(0.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Title
                    Text(
                        text = "Import from Excel",
                        style = AppTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurface
                    )
                    
                    // Description
                    Text(
                        text = "Select an Excel file (.xlsx or .xls) containing your portfolio data",
                        style = AppTypography.bodyLarge,
                        color = AppColors.OnSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    // Instructions for Mac file access
                    Text(
                        text = "To access files from your Mac:\n1. Drag & drop Excel file onto emulator\n2. Or use: adb push /path/to/file.xlsx /sdcard/Download/",
                        style = AppTypography.bodySmall,
                        color = AppColors.SecondaryText,
                        textAlign = TextAlign.Center
                    )
                    
                    // File selection display
                    if (selectedFileName != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.Primary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📄 Selected File:",
                                    style = AppTypography.bodyMedium,
                                    color = AppColors.OnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedFileName,
                                    style = AppTypography.bodyLarge,
                                    color = AppColors.Primary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    // Choose File button (at the top)
                    Button(
                        onClick = {
                            println("📊 File picker clicked - opening file picker")
                            onOpenFilePicker?.invoke()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedFileName != null) AppColors.SecondaryText else AppColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedFileName != null) "Choose Different File" else "Choose File",
                            color = AppColors.OnPrimary,
                            style = AppTypography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Two buttons below - Cancel and Import
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Cancel button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.SecondaryText
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                color = AppColors.OnSurface,
                                style = AppTypography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Import button
                        Button(
                            onClick = {
                                println("📊 Import button clicked")
                                selectedFileName?.let { fileName ->
                                    onFileSelected(fileName)
                                }
                                onDismiss()
                            },
                            enabled = selectedFileName != null,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedFileName != null) AppColors.Primary else AppColors.SecondaryText,
                                contentColor = if (selectedFileName != null) AppColors.OnPrimary else AppColors.OnSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Import",
                                color = if (selectedFileName != null) AppColors.OnPrimary else AppColors.OnSurface,
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
