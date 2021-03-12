package playwright.browser

import com.google.gson.JsonObject
import playwright.processor.ChannelOwner

class Browser(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer) {
}