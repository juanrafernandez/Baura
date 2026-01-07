package com.jrlabs.baura.ui.components.dialogs

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * A full-screen dialog that extends edge-to-edge behind system bars.
 * The content inside should handle its own insets using statusBarsPadding()
 * and navigationBarsPadding() where needed.
 *
 * @param onDismissRequest Called when the dialog should be dismissed
 * @param dismissOnBackPress Whether pressing back dismisses the dialog
 * @param dismissOnClickOutside Whether clicking outside dismisses the dialog
 * @param content The content of the dialog
 */
@Composable
fun EdgeToEdgeDialog(
    onDismissRequest: () -> Unit,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = false,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current
        val dialogWindowProvider = view.parent as? DialogWindowProvider
        val activity = view.context as? Activity

        DisposableEffect(Unit) {
            val handler = Handler(Looper.getMainLooper())

            fun applyStatusBarConfig() {
                // Configure dialog window
                dialogWindowProvider?.window?.let { window ->
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                    window.setDimAmount(0f)

                    window.attributes = window.attributes.apply {
                        width = WindowManager.LayoutParams.MATCH_PARENT
                        height = WindowManager.LayoutParams.MATCH_PARENT
                        gravity = Gravity.TOP or Gravity.START
                        horizontalMargin = 0f
                        verticalMargin = 0f
                    }

                    // Dark icons on light background
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }
                }

                // Force Activity window to use dark status bar icons
                activity?.window?.let { activityWindow ->
                    WindowInsetsControllerCompat(activityWindow, activityWindow.decorView).apply {
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }
                }
            }

            // Apply immediately
            applyStatusBarConfig()

            // Apply again after a short delay to ensure it takes effect
            handler.postDelayed({ applyStatusBarConfig() }, 100)

            onDispose {
                handler.removeCallbacksAndMessages(null)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
