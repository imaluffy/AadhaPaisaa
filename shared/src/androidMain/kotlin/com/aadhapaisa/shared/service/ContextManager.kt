package com.aadhapaisa.shared.service

import android.content.Context

actual object ContextManager {
    private var context: Context? = null
    
    actual fun setContext(context: Any?) {
        this.context = context as? Context
        println("📱 ContextManager: Context set - ${context?.let { it::class.simpleName } ?: "null"}")
    }
    
    actual fun getContext(): Any? {
        return context
    }
}
