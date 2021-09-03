package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class GrantPermissionsOptions @JvmOverloads constructor(
    /**
     * The [origin] to grant permissions to, e.g. "https://example.com".
     */
    var origin: String? = null,
    @Transient private val builder: IBuilder<GrantPermissionsOptions>
) {
    init {
        builder.build(this)
    }
}