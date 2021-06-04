package com.playwright.remote.engine.console.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.processor.ChannelOwner

class ConsoleMessage(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IConsoleMessage {

    override fun args(): List<IJSHandle> {
        val result = arrayListOf<IJSHandle>()
        for (item in initializer["args"].asJsonArray) {
            result.add(messageProcessor.getExistingObject(item.asJsonObject["guid"].asString))
        }
        return result
    }

    override fun location(): String {
        val location = initializer["location"].asJsonObject
        return "${location["url"].asString}:${location["lineNumber"].asNumber}:${location["columnNumber"].asNumber}"
    }

    override fun text(): String = initializer["text"].asString

    override fun type(): String = initializer["type"].asString
}