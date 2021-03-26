package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.enum.MouseButton

class UpOptions(
    /**
     * Defaults to {@code left}.
     */
    var button: MouseButton? = null,
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    var clickCount: Int? = null,
    fn: UpOptions.() -> Unit
) {
    init {
        fn()
    }
}