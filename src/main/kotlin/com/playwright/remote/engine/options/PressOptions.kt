package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class PressOptions @JvmOverloads constructor(
    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    var delay: Double? = null,
    private val builder: IBuilder<PressOptions>
) {
    init {
        builder.build(this)
    }
}