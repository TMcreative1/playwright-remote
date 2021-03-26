package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.enum.MouseButton

class DoubleClickOptions(
    /**
     * Defaults to {@code left}.
     */
    var button: MouseButton? = null,
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    var delay: Double? = null,
    fn: DoubleClickOptions.() -> Unit
) {
    init {
        fn()
    }
}