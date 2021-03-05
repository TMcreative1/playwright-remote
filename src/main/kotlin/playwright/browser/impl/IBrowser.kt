package playwright.browser.impl

import java.util.function.Consumer

interface IBrowser : AutoCloseable {

    fun onDisconnected(handler: Consumer<IBrowser>)
    fun offDisconnected(handler: Consumer<IBrowser>)

}