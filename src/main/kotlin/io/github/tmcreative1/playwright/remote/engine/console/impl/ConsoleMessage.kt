package io.github.tmcreative1.playwright.remote.engine.console.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.console.api.IConsoleMessage
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner

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