package com.aadhapaisa.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.aadhapaisa.shared.ui.MainApp
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    MainApp(
        onOpenUrl = { url ->
            val nsUrl = platform.Foundation.NSURL.URLWithString(url)
            nsUrl?.let {
                platform.UIKit.UIApplication.sharedApplication.openURL(it)
            }
        }
    )
}




