package com.playwright.remote.engine.playwright.api

import com.google.gson.JsonObject
import com.playwright.remote.engine.playwright.impl.IPlaywright
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.selector.api.ISelectors
import com.playwright.remote.engine.selector.api.ISharedSelectors
import com.playwright.remote.engine.selector.impl.SharedSelectors

class Playwright(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IPlaywright {
    private var sharedSelectors: ISharedSelectors? = null
    private val selectors =
        messageProcessor.getExistingObject<ISelectors>(initializer["selectors"].asJsonObject["guid"].asString)

    override fun initSharedSelectors(parent: IPlaywright?) {
        assert(sharedSelectors == null)
        sharedSelectors = if (parent == null) SharedSelectors() else (parent as Playwright).sharedSelectors
        sharedSelectors!!.addSelector(selectors)
    }

    override fun unregisterSelectors() {
        sharedSelectors!!.removeSelector(selectors)
    }

    override fun selectors(): ISharedSelectors? {
        return sharedSelectors
    }

    fun initializer(): JsonObject {
        return initializer
    }
}