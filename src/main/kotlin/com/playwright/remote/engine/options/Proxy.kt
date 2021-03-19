package com.playwright.remote.engine.options

class Proxy(
    val server: String,
    var bypass: String? = null,
    var username: String? = null,
    var password: String? = null,
    fn: Proxy.() -> Unit
) {
    init {
        fn()
    }
}