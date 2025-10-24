package com.aadhapaisa.android

import android.net.Uri
import android.os.Bundle
import android.content.ContentResolver
import android.database.Cursor
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.aadhapaisa.shared.ui.MainApp
import com.aadhapaisa.shared.ui.FileSelectionManager

class MainActivity : ComponentActivity() {
    
    // File picker launcher using GetContent
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            println("📁 File selected: ${it.toString()}")
            println("📁 File scheme: ${it.scheme}")
            println("📁 File path: ${it.path}")
            // Pass the file URI to the shared code
            onFileSelected(it.toString())
        } ?: run {
            println("📁 No file selected or selection cancelled")
        }
    }
    
    // Alternative file picker launcher using OpenDocument (might work better for Excel files)
    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            println("📁 Document selected: ${it.toString()}")
            println("📁 Document scheme: ${it.scheme}")
            println("📁 Document path: ${it.path}")
            // Pass the file URI to the shared code
            onFileSelected(it.toString())
        } ?: run {
            println("📁 No document selected or selection cancelled")
        }
    }
    
    private fun onFileSelected(fileUri: String) {
        println("📊 MainActivity: File selected - $fileUri")
        
        val fileName = try {
            val uri = Uri.parse(fileUri)
            println("📊 MainActivity: URI scheme: ${uri.scheme}")
            println("📊 MainActivity: URI path: ${uri.path}")
            println("📊 MainActivity: URI lastPathSegment: ${uri.lastPathSegment}")
            
            // Use ContentResolver to get the actual file name
            val fileNameFromContentResolver = getFileNameFromUri(uri)
            println("📊 MainActivity: File name from ContentResolver: $fileNameFromContentResolver")
            
            fileNameFromContentResolver ?: "Excel File"
        } catch (e: Exception) {
            println("📊 MainActivity: Error extracting file name: ${e.message}")
            "Excel File"
        }
        
        // Update the shared state
        FileSelectionManager.setSelectedFileName(fileName)
        println("📊 MainActivity: File name set in shared state: $fileName")
    }
    
    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        var cursor: Cursor? = null
        
        try {
            cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                    println("📊 MainActivity: DISPLAY_NAME: $fileName")
                }
            }
        } catch (e: Exception) {
            println("📊 MainActivity: Error querying ContentResolver: ${e.message}")
        } finally {
            cursor?.close()
        }
        
        // If ContentResolver didn't work, try fallback methods
        if (fileName.isNullOrEmpty()) {
            fileName = uri.lastPathSegment
            println("📊 MainActivity: Fallback to lastPathSegment: $fileName")
        }
        
        return fileName
    }
    
    fun openFilePicker() {
        println("📁 MainActivity: Opening file picker")
        // Try OpenDocument first (better for Excel files), then fallback to GetContent
        try {
            // OpenDocument approach - often works better for Excel files
            documentPickerLauncher.launch(arrayOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
                "application/vnd.ms-excel" // .xls
            ))
        } catch (e: Exception) {
            println("📁 OpenDocument failed, trying GetContent: ${e.message}")
            // Fallback to GetContent with multiple MIME types
            val mimeTypes = arrayOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
                "application/vnd.ms-excel", // .xls
                "application/excel",
                "application/x-excel",
                "application/x-msexcel",
                "*/*" // Fallback to allow all files
            )
            filePickerLauncher.launch(mimeTypes.joinToString(","))
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp(
                context = this,
                onOpenFilePicker = { openFilePicker() },
                onFileSelected = { fileName ->
                    println("📊 MainActivity: Received file selection callback: $fileName")
                    // This will be called when a file is selected
                }
            )
        }
    }
}