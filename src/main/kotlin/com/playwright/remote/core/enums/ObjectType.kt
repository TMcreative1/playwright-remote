package com.playwright.remote.core.enums

enum class ObjectType(val type: String) {
    BROWSER("Browser"),
    BROWSER_CONTEXT("BrowserContext"),
    CONSOLE_MESSAGE("ConsoleMessage"),
    FRAME("Frame"),
    PAGE("Page"),
    REMOTE_BROWSER("RemoteBrowser"),
    RESPONSE("Response"),
    REQUEST("Request"),
    SELECTORS("Selectors")
}