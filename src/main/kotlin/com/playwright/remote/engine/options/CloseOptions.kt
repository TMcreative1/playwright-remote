package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class CloseOptions @JvmOverloads constructor(
    /**
     * Defaults to {@code false}. Whether to run the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/Events/beforeunload">before unload</a> page handlers.
     */
    var runBeforeUnload: Boolean? = null,
    @Transient private val builder: IBuilder<CloseOptions>
) {
    init {
        builder.build(this)
    }
}
