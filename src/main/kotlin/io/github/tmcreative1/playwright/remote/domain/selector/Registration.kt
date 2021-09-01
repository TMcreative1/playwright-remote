package io.github.tmcreative1.playwright.remote.domain.selector

import io.github.tmcreative1.playwright.remote.engine.options.RegisterOptions

data class Registration(
    val name: String,
    val script: String,
    val options: RegisterOptions?
)