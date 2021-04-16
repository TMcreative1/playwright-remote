package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class HttpCredentials constructor(
    var userName: String? = null,
    var password: String? = null,
    private val builder: IBuilder<HttpCredentials>
) {
    init {
        builder.build(this)
    }
}