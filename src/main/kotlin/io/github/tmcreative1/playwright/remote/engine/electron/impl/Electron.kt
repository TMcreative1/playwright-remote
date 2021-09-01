package io.github.tmcreative1.playwright.remote.engine.electron.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner

class Electron(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer)