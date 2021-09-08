package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder
import com.playwright.remote.engine.options.enum.MouseButton

data class UpOptions @JvmOverloads constructor(
    /**
     * Defaults to {@code left}.
     */
    var button: MouseButton? = null,
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    var clickCount: Int? = null,
    @Transient private val builder: IBuilder<UpOptions>
) {
    init {
        builder.build(this)
    }
}