package com.playwright.remote.core.enums

enum class LoadState(val value: String) {
    LOAD("load"),
    DOMCONTENTLOADED("domcontentloaded"),
    NETWORKIDLE("networkidle")
}