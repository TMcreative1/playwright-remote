package com.playwright.remote.engine.dialog.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.dialog.api.IDialog
import com.playwright.remote.engine.processor.ChannelOwner

class Dialog(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IDialog {

    override fun accept(promptText: String?) {
        val params = JsonObject()
        if (promptText != null) {
            params.addProperty("promptText", promptText)
        }
        sendMessage("accept", params)
    }

    override fun defaultValue(): String {
        return initializer["defaultValue"].asString
    }

    override fun dismiss() {
        sendMessage("dismiss")
    }

    override fun message(): String {
        return initializer["message"].asString
    }

    override fun type(): String {
        return initializer["type"].asString
    }
}