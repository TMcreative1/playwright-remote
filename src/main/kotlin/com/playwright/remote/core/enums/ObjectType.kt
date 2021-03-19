package com.playwright.remote.core.enums

enum class ObjectType(val type: String) {
    BROWSER("Browser"),
    BROWSER_CONTEXT("BrowserContext"),
    SELECTORS("Selectors"),
    REMOTE_BROWSER("RemoteBrowser")
}