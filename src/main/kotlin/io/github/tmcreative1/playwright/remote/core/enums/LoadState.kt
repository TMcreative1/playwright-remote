package io.github.tmcreative1.playwright.remote.core.enums

enum class LoadState(val value: String) {
    LOAD("load"),
    DOMCONTENTLOADED("domcontentloaded"),
    NETWORKIDLE("networkidle")
}