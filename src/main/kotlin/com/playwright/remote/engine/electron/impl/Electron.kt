package com.playwright.remote.engine.electron.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.processor.ChannelOwner

class Electron(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer)