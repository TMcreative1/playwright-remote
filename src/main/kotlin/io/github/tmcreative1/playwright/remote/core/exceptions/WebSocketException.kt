package io.github.tmcreative1.playwright.remote.core.exceptions

class WebSocketException : RuntimeException {

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}