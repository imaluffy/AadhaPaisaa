package com.aadhapaisa.shared.service

expect object ContextManager {
    fun setContext(context: Any?)
    fun getContext(): Any?
}

// Platform-specific implementations will be in androidMain and iosMain
