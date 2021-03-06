package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class MoveOptions @JvmOverloads constructor(
    /**
     * defaults to 1. Sends intermediate {@code mousemove} events.
     */
    var steps: Int? = null,
    @Transient private val builder: IBuilder<MoveOptions>
) {
    init {
        builder.build(this)
    }
}