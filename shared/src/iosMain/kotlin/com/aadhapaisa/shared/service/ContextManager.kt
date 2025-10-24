package com.aadhapaisa.shared.service

actual object ContextManager {
    private var context: Any? = null
    
    actual fun setContext(context: Any?) {
        this.context = context
        println("🍎 ContextManager: Context set - ${context?.let { it::class.simpleName } ?: "null"}")
    }
    
    actual fun getContext(): Any? {
        return context
    }
}
