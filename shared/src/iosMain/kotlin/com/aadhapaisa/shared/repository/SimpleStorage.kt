package com.aadhapaisa.shared.repository

import platform.Foundation.NSUserDefaults

actual class SimpleStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual fun initialize(context: Any) {
        // iOS doesn't need context initialization
    }
    
    actual fun saveData(key: String, data: String) {
        println("💾 iOS: Saving data for key '$key': $data")
        userDefaults.setObject(data, key)
        println("✅ iOS: Data saved successfully")
    }
    
    actual fun loadData(key: String): String? {
        val data = userDefaults.stringForKey(key)
        println("🔍 iOS: Loading data for key '$key': $data")
        return data
    }
    
    actual fun clearData(key: String) {
        println("🗑️ iOS: Clearing data for key '$key'")
        userDefaults.removeObjectForKey(key)
        println("✅ iOS: Data cleared successfully")
    }
}
