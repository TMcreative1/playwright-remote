package playwright.browser.api

import playwright.browser.impl.IBrowser
import java.util.function.Consumer

class Browser : IBrowser {
    override fun onDisconnected(handler: Consumer<IBrowser>) {
        TODO("Not yet implemented")
    }

    override fun offDisconnected(handler: Consumer<IBrowser>) {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}