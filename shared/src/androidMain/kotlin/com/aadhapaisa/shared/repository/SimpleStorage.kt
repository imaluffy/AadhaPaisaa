package com.aadhapaisa.shared.repository

import android.content.Context
import android.content.SharedPreferences

actual class SimpleStorage {
    private var prefs: SharedPreferences? = null
    
    actual fun initialize(context: Any) {
        prefs = (context as Context).getSharedPreferences("aadhapaisa_data", Context.MODE_PRIVATE)
        println("🔧 Android SimpleStorage initialized with SharedPreferences")
    }
    
    actual fun saveData(key: String, data: String) {
        println("💾 Android: Saving data for key '$key': $data")
        if (prefs == null) {
            println("❌ Android: SharedPreferences not initialized!")
            return
        }
        prefs?.edit()?.putString(key, data)?.commit() // Use commit() for synchronous save
        println("✅ Android: Data saved successfully")
    }
    
    actual fun loadData(key: String): String? {
        if (prefs == null) {
            println("❌ Android: SharedPreferences not initialized!")
            return null
        }
        val data = prefs?.getString(key, null)
        println("🔍 Android: Loading data for key '$key': $data")
        return data
    }
    
    actual fun clearData(key: String) {
        println("🗑️ Android: Clearing data for key '$key'")
        if (prefs == null) {
            println("❌ Android: SharedPreferences not initialized!")
            return
        }
        prefs?.edit()?.remove(key)?.commit() // Use commit() for synchronous save
    }
}
