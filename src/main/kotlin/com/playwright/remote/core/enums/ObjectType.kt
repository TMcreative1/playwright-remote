package com.playwright.remote.core.enums

enum class ObjectType(val type: String) {
    ARTIFACT("Artifact"),
    BINDING_CALL("BindingCall"),
    BROWSER("Browser"),
    BROWSER_CONTEXT("BrowserContext"),
    CONSOLE_MESSAGE("ConsoleMessage"),
    DIALOG("Dialog"),
    FRAME("Frame"),
    JS_HANDLE("JSHandle"),
    PAGE("Page"),
    REMOTE_BROWSER("RemoteBrowser"),
    REQUEST("Request"),
    RESPONSE("Response"),
    ROUTE("Route"),
    SELECTORS("Selectors"),
    STREAM("Stream"),
    WEBSOCKET("WebSocket")
}