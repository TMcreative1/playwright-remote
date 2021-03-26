package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.enum.MouseButton

class ClickOptions(
    /**
     * Defaults to {@code left}.
     */
    var button: MouseButton? = null,
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    var clickCount: Int? = null,
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    var delay: Double? = null,
    fn: ClickOptions.() -> Unit
) {
    init {
        fn()
    }
}