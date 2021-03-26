package com.playwright.remote.engine.keyboard.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.engine.keyboard.api.IKeyboard
import com.playwright.remote.engine.options.PressOptions
import com.playwright.remote.engine.options.TypeOptions
import com.playwright.remote.engine.processor.ChannelOwner

class Keyboard(val page: ChannelOwner) : IKeyboard {

    override fun down(key: String) {
        val params = JsonObject()
        params.addProperty("key", key)
        page.sendMessage("keyboardDown", params)
    }

    override fun insertText(text: String) {
        val params = JsonObject()
        params.addProperty("text", text)
        page.sendMessage("keyboardInsertText", params)
    }

    override fun press(key: String, options: PressOptions) {
        val params = Gson().toJsonTree(options).asJsonObject
        params.addProperty("key", key)
        page.sendMessage("keyboardPress", params)
    }

    override fun type(text: String, options: TypeOptions) {
        val params = Gson().toJsonTree(options).asJsonObject
        params.addProperty("text", text)
        page.sendMessage("keyboardType", params)
    }

    override fun up(key: String) {
        val params = JsonObject()
        params.addProperty("key", key)
        page.sendMessage("keyboardUp", params)
    }
}