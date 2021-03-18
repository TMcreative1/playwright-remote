package com.playwright.remote.playwright.browser.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.playwright.options.RegisterOptions
import com.playwright.remote.playwright.processor.ChannelOwner

class Selectors(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer) {

    fun register(name: String, script: String, options: RegisterOptions?) {
        val params = Gson().toJsonTree(options ?: RegisterOptions{}).asJsonObject
        params.addProperty("name", name)
        params.addProperty("sourse", script)
        sendMessage("register", params)
    }
}