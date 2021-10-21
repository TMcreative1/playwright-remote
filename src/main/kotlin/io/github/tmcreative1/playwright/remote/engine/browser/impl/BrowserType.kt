package io.github.tmcreative1.playwright.remote.engine.browser.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowserType
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner

class BrowserType(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBrowserType {
    override fun name(): String = initializer["name"].asString
}