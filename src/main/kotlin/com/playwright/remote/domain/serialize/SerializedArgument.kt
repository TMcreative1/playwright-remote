package com.playwright.remote.domain.serialize

import com.playwright.remote.domain.serialize.SerializedError.SerializedValue

class SerializedArgument(
    var value: SerializedValue? = null,
    var handles: Array<Channel>? = null
) {
    data class Channel(val guid: String)
}