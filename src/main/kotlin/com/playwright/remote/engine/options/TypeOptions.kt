package com.playwright.remote.engine.options

class TypeOptions(
    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    var delay: Double? = null,
    fn: TypeOptions.() -> Unit
) {
    init {
        fn()
    }
}