package com.playwright.remote.engine.browser.api

import com.playwright.remote.engine.options.RegisterOptions

interface ISelectors {
    fun register(name: String, script: String) = register(name, script, null)
    fun register(name: String, script: String, options: RegisterOptions? = null)
}