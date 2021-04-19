package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class ViewportSize @JvmOverloads constructor(
    /**
     * page width in pixels.
     */
    var width: Int? = null,
    /**
     * page height in pixels.
     */
    var height: Int? = null,
    private val builder: IBuilder<ViewportSize>
) {
    init {
        builder.build(this)
    }
}