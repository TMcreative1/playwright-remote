package com.playwright.remote.core.exceptions

import com.playwright.remote.domain.serialize.SerializedError

class DriverException(error: SerializedError.Error) : PlaywrightException(error.toString())