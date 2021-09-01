package com.playwright.remote.core.exceptions


open class PlaywrightException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}