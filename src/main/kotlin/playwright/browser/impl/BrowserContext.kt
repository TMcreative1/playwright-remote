package playwright.browser.impl

import com.google.gson.JsonObject
import core.enums.EventType
import core.enums.EventType.CLOSE
import core.exceptions.PlaywrightException
import playwright.browser.api.IBrowserContext
import playwright.listener.ListenerCollection
import playwright.page.impl.Page
import playwright.processor.ChannelOwner
import java.nio.file.Path

class BrowserContext(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBrowserContext {
    val browser: Browser? = if (parent is Browser) parent else null
    var ownerPage: Page? = null
    var videosDir: Path? = null
    val pages = arrayListOf<Page>()
    private val listeners = ListenerCollection<EventType>()

    override fun newPage(): Page {
        if (ownerPage != null) {
            throw PlaywrightException("Please use browser.newContext()")
        }
        val jsonObject = sendMessage("newPage").asJsonObject
        return messageProcessor.getExistingObject(jsonObject.getAsJsonObject("page").get("guid").asString)
    }


    override fun close() {
        TODO("Not yet implemented")
    }

    fun didClose() {
        browser?.contexts?.remove(this)
        listeners.notify(CLOSE, this)
    }

}