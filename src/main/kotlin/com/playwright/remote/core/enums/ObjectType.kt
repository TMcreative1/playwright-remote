package com.playwright.remote.core.enums

enum class ObjectType(val type: String) {
    BROWSER("Browser"),
    BROWSER_CONTEXT("BrowserContext"),
    FRAME("Frame"),
    PAGE("Page"),
    REMOTE_BROWSER("RemoteBrowser"),
    RESPONSE("Response"),
    REQUEST("Request"),
    SELECTORS("Selectors")
}