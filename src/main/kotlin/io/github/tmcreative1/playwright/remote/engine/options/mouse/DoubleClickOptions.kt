package io.github.tmcreative1.playwright.remote.engine.options.mouse

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder
import io.github.tmcreative1.playwright.remote.engine.options.enum.MouseButton

data class DoubleClickOptions @JvmOverloads constructor(
    /**
     * Defaults to {@code left}.
     */
    var button: MouseButton? = null,
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    var delay: Double? = null,
    @Transient private val builder: IBuilder<DoubleClickOptions>
) {
    init {
        builder.build(this)
    }
}