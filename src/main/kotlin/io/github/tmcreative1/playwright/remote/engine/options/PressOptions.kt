package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class PressOptions @JvmOverloads constructor(
    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    var delay: Double? = null,
    @Transient private val builder: IBuilder<PressOptions>
) {
    init {
        builder.build(this)
    }
}