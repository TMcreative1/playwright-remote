package com.playwright.remote.engine.options

class PressOptions(
    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    var delay: Double? = null,
    fn: PressOptions.() -> Unit
) {
    init {
        fn()
    }
}