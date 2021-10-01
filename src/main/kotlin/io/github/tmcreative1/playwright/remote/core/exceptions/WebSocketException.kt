package io.github.tmcreative1.playwright.remote.core.exceptions

import java.lang.RuntimeException

class WebSocketException : RuntimeException {

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}