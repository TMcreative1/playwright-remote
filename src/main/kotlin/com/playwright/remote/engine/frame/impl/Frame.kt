package com.playwright.remote.engine.frame.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.core.enums.LoadState
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.options.NavigateOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.response.api.IResponse

class Frame(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IFrame {
    private val name: String = initializer["name"].asString
    private val url: String = initializer["url"].asString
    private var parentFrame: IFrame? = null
    private val childFrames = linkedSetOf<IFrame>()
    private val loadStates = hashSetOf<LoadState>()
    private var page: IPage? = null

    init {
        if (initializer.has("parentFrame")) {
            parentFrame = messageProcessor.getExistingObject(initializer["parentFrame"].asJsonObject["guid"].asString)
            (parentFrame as Frame).childFrames.add(this)
        }
        for (item in initializer["loadStates"].asJsonArray) {
            loadStates.add(LoadState.valueOf(item.asString.toUpperCase()))
        }
    }

    override fun page(): IPage? {
        return page
    }

    override fun navigate(url: String, options: NavigateOptions): IResponse? {
        val params = Gson().toJsonTree(options).asJsonObject
        params.addProperty("url", url)
        val result = sendMessage("goto", params)
        val jsonResponse = result.asJsonObject["response"].asJsonObject ?: return null
        return messageProcessor.getExistingObject(jsonResponse["guid"].asString)
    }
}