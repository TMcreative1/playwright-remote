package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class ScreenSize @JvmOverloads constructor(
    /**
     * page width in pixels.
     */
    var width: Int? = null,
    /**
     * page height in pixels.
     */
    var height: Int? = null,
    @Transient private val builder: IBuilder<ScreenSize>
) {
    init {
        builder.build(this)
    }
}
