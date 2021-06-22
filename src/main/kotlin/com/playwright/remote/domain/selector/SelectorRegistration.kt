package com.playwright.remote.domain.selector

import com.playwright.remote.engine.options.RegisterOptions

data class SelectorRegistration(
    val name: String,
    val script: String,
    val options: RegisterOptions?
)
