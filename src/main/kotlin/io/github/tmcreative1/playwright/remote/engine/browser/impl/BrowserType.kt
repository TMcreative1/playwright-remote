package com.playwright.remote.engine.browser.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.processor.ChannelOwner

class BrowserType(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer)