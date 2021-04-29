package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class Margin @JvmOverloads constructor(
    /**
     * Top margin, accepts values labeled with units. Defaults to {@code 0}.
     */
    var top: String? = null,
    /**
     * Right margin, accepts values labeled with units. Defaults to {@code 0}.
     */
    var right: String? = null,
    /**
     * Bottom margin, accepts values labeled with units. Defaults to {@code 0}.
     */
    var bottom: String? = null,
    /**
     * Left margin, accepts values labeled with units. Defaults to {@code 0}.
     */
    var left: String? = null,
    @Transient private val builder: IBuilder<Margin>
) {
    init {
        builder.build(this)
    }
}
