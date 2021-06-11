package com.playwright.remote.engine.playwright.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.processor.ChannelOwner

class Playwright(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer)