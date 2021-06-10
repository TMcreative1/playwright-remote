package com.playwright.remote.core.enums

enum class ObjectType(val type: String) {
    ANDROID("Android"),
    ARTIFACT("Artifact"),
    BINDING_CALL("BindingCall"),
    BROWSER("Browser"),
    BROWSER_TYPE("BrowserType"),
    BROWSER_CONTEXT("BrowserContext"),
    CONSOLE_MESSAGE("ConsoleMessage"),
    DIALOG("Dialog"),
    ELECTRON("Electron"),
    ELEMENT_HANDLE("ElementHandle"),
    FRAME("Frame"),
    JS_HANDLE("JSHandle"),
    PAGE("Page"),
    PLAYWRIGHT("Playwright"),
    REMOTE_BROWSER("RemoteBrowser"),
    REQUEST("Request"),
    RESPONSE("Response"),
    ROUTE("Route"),
    SELECTORS("Selectors"),
    STREAM("Stream"),
    WEBSOCKET("WebSocket"),
    WORKER("Worker")
}