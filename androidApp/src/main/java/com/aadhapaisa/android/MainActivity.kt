package com.aadhapaisa.android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.aadhapaisa.shared.ui.MainApp

class MainActivity : ComponentActivity() {
    
    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            println("📁 File selected: ${it.toString()}")
            // Pass the file URI to the shared code
            onFileSelected(it.toString())
        }
    }
    
    private fun onFileSelected(fileUri: String) {
        println("📊 MainActivity: File selected - $fileUri")
        // This will be handled by the shared code
    }
    
    fun openFilePicker() {
        println("📁 MainActivity: Opening file picker")
        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp(
                context = this,
                onOpenFilePicker = { openFilePicker() }
            )
        }
    }
}