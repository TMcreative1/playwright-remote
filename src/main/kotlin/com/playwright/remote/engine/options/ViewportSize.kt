package com.playwright.remote.playwright.options

class ViewportSize(
    /**
     * page width in pixels.
     */
    var width: Int? = null,
    /**
     * page height in pixels.
     */
    var height: Int? = null,
    fn: ViewportSize.() -> Unit
) {
    init {
        fn()
    }
}