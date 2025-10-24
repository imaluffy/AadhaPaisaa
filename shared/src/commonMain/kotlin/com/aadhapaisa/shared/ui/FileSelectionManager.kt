package com.aadhapaisa.shared.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FileSelectionManager {
    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName: StateFlow<String?> = _selectedFileName.asStateFlow()
    
    fun setSelectedFileName(fileName: String?) {
        println("📁 FileSelectionManager: Setting selected file name: $fileName")
        _selectedFileName.value = fileName
    }
    
    fun clearSelection() {
        println("📁 FileSelectionManager: Clearing file selection")
        _selectedFileName.value = null
    }
}
