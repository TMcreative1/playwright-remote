package io.github.tmcreative1.playwright.remote.engine.browser.selector.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.browser.selector.api.ISelectors
import io.github.tmcreative1.playwright.remote.engine.options.RegisterOptions
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
import java.nio.file.Path

class Selectors(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), ISelectors {

    override fun register(name: String, script: String, options: RegisterOptions?) {
        val params = gson().toJsonTree(options ?: RegisterOptions {}).asJsonObject
        params.addProperty("name", name)
        params.addProperty("source", script)
        sendMessage("register", params)
    }
}