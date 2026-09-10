package com.example.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

object SecurityManager {

    /**
     * Enables or disables WindowManager.LayoutParams.FLAG_SECURE on the current window.
     * Prevents screenshots and screen recording on sensitive mathematical problem screens.
     */
    fun setSecureFlag(activity: Activity?, secure: Boolean) {
        activity?.window?.let { window ->
            if (secure) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    /**
     * Compose helper that automatically applies FLAG_SECURE while the composable is on screen,
     * and safely removes it when navigating away.
     */
    @Composable
    fun RequireScreenProtection(enabled: Boolean = true) {
        val context = LocalContext.current
        DisposableEffect(enabled) {
            val activity = context.findActivity()
            if (enabled) {
                setSecureFlag(activity, true)
            }
            onDispose {
                if (enabled) {
                    setSecureFlag(activity, false)
                }
            }
        }
    }
}
