import com.playwright.remote.core.enums.BrowserType
import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.server.impl.ServerProvider
import com.playwright.remote.utils.PlatformUtils

fun main(args: Array<String>) {
    val currentPlatform = PlatformUtils.getCurrentPlatform()
    val browserType = BrowserType.CHROMIUM
    val server = ServerProvider()
    val wsEndpoint = server.launchServer(currentPlatform, browserType)
    val browser = RemoteBrowser.connectWs(wsEndpoint!!)
    browser.close()
    server.stopServer()
}