package com.playwright.remote.engine.browser.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.engine.browser.api.ISelectors
import com.playwright.remote.engine.options.RegisterOptions
import com.playwright.remote.engine.processor.ChannelOwner

class Selectors(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), ISelectors {

    @JvmOverloads
    override fun register(name: String, script: String, options: RegisterOptions?) {
        val params = Gson().toJsonTree(options ?: RegisterOptions {}).asJsonObject
        params.addProperty("name", name)
        params.addProperty("sourse", script)
        sendMessage("register", params)
    }
}