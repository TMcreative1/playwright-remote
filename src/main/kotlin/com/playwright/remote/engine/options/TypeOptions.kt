package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class TypeOptions @JvmOverloads constructor(
    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    var delay: Double? = null,
    @Transient private val builder: IBuilder<TypeOptions>
) {
    init {
        builder.build(this)
    }
}