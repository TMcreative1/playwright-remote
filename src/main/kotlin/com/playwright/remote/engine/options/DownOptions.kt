package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.enum.MouseButton

class DownOptions(
    /**
     * Defaults to {@code left}.
     */
    var button: MouseButton? = null,
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    fn: DownOptions.() -> Unit
) {
    init {
        fn()
    }
}