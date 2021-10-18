package io.github.tmcreative1.playwright.remote.core.enums

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
    FETCH_REQUEST("FetchRequest"),
    FRAME("Frame"),
    JS_HANDLE("JSHandle"),
    JSON_PIPE("JsonPipe"),
    PAGE("Page"),
    PLAYWRIGHT("Playwright"),
    REQUEST("Request"),
    RESPONSE("Response"),
    ROUTE("Route"),
    SELECTORS("Selectors"),
    STREAM("Stream"),
    WEBSOCKET("WebSocket"),
    WORKER("Worker")
}