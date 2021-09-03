package com.playwright.remote.engine.android.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.processor.ChannelOwner

class Android(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer)