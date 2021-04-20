package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class Proxy @JvmOverloads constructor(
    val server: String,
    var bypass: String? = null,
    var username: String? = null,
    var password: String? = null,
    @Transient private val builder: IBuilder<Proxy>
) {
    init {
        builder.build(this)
    }
}