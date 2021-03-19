package com.playwright.remote.core.exceptions

import com.playwright.remote.message.SerializedError

class DriverException(error: SerializedError.Error) : PlaywrightException(error.toString()) {
}