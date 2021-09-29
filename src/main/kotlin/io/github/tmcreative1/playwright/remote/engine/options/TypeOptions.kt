package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

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