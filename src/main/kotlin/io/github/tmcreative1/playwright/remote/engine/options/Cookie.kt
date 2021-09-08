package com.playwright.remote.engine.options

import com.playwright.remote.core.enums.SameSiteAttribute
import com.playwright.remote.engine.options.api.IBuilder

data class Cookie @JvmOverloads constructor(
    var name: String? = null,
    var value: String? = null,
    /**
     * either url or domain / path are required. Optional.
     */
    var url: String? = null,
    /**
     * either url or domain / path are required Optional.
     */
    var domain: String? = null,
    /**
     * either url or domain / path are required Optional.
     */
    var path: String? = null,
    /**
     * Unix time in seconds. Optional.
     */
    var expires: Double? = null,
    /**
     * Optional.
     */
    var httpOnly: Boolean? = null,
    /**
     * Optional.
     */
    var secure: Boolean? = null,
    /**
     * Optional.
     */
    var sameSite: SameSiteAttribute? = null,
    @Transient private val builder: IBuilder<Cookie>
) {
    init {
        builder.build(this)
    }
}