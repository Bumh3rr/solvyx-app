package com.solvyx.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** The installed app's real `versionName`, so the UI never shows a stale hardcoded version. */
@Composable
fun rememberAppVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
}
