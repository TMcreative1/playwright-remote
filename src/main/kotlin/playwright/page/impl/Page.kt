package playwright.page.impl

import com.google.gson.JsonObject
import playwright.browser.api.IBrowserContext
import playwright.page.api.IPage
import playwright.processor.ChannelOwner

class Page(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IPage {
    var ownedContext: IBrowserContext? = null
}