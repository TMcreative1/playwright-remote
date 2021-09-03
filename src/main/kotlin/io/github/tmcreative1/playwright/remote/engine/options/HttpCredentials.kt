package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class HttpCredentials @JvmOverloads constructor(
    var username: String? = null,
    var password: String? = null,
    @Transient private val builder: IBuilder<HttpCredentials>
) {
    init {
        builder.build(this)
    }
}