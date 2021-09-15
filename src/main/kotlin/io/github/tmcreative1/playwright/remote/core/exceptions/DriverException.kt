package io.github.tmcreative1.playwright.remote.core.exceptions

import io.github.tmcreative1.playwright.remote.domain.serialize.SerializedError

class DriverException(error: SerializedError.Error) : PlaywrightException(error.toString())