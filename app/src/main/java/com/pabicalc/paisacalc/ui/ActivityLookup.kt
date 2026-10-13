package com.pabicalc.paisacalc.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * The consent form and the Play purchase sheet need a real `Activity`, and
 * Compose hands us a context that may be wrapped. Walks the wrapper chain and
 * returns null when there is none (previews, tests), so callers can no-op
 * instead of crashing.
 */
fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
