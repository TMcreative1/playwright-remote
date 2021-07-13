package com.playwright.remote.base

import com.playwright.remote.base.server.Server
import com.playwright.remote.core.enums.BrowserType
import com.playwright.remote.core.enums.BrowserType.valueOf
import com.playwright.remote.core.enums.Platform.*
import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.browser.api.IBrowser
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.server.api.IServerProvider
import com.playwright.remote.engine.server.impl.ServerProvider
import com.playwright.remote.utils.PlatformUtils.Companion.getCurrentPlatform
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach


open class BaseTest {

    companion object {

        @JvmStatic
        lateinit var httpServer: Server

        @JvmStatic
        lateinit var httpsServer: Server

        @JvmStatic
        lateinit var wsUrl: String

        @JvmStatic
        lateinit var browserContext: IBrowserContext

        @JvmStatic
        lateinit var browser: IBrowser

        @JvmStatic
        lateinit var page: IPage

        @JvmStatic
        private lateinit var server: IServerProvider

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            launchBrowserServer()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            stopServerBrowserServer()
        }

        private fun launchBrowserServer() {
            server = ServerProvider()
            wsUrl = server.launchServer(
                getCurrentPlatform(),
                getBrowserType()
            )!!
        }

        private fun stopServerBrowserServer() {
            server.stopServer()
        }

        private fun getBrowserType(): BrowserType =
            valueOf(System.getProperty("browser").ifEmpty { "webkit" }.uppercase())

        @JvmStatic
        fun isChromium() = getBrowserType().browserName == "chromium"

        @JvmStatic
        fun isWebkit() = getBrowserType().browserName == "webkit"

        @JvmStatic
        fun isFirefox() = getBrowserType().browserName == "firefox"
    }

    @BeforeEach
    private fun beforeEach() {
        createBrowser()
        createHttpServers()
    }

    @AfterEach
    private fun afterEach() {
        stopHttpServers()
    }

    private fun createBrowser() {
        browser = RemoteBrowser.connectWs(wsUrl)
        browserContext = browser.newContext()
        page = browserContext.newPage()
    }

    private fun createHttpServers() {
        httpServer = Server.createHttp(8080)
        httpsServer = Server.createHttps(8443)
    }

    private fun stopHttpServers() {
        httpServer.stop()
        httpsServer.stop()
    }

    protected fun attachFrame(page: IPage, name: String, url: String): IFrame? {
        val handle = page.evaluateHandle(
            """
            async ({frameId, url}) => {
                const frame = document.createElement('iframe');
                frame.src = url;
                frame.id = frameId;
                document.body.appendChild(frame)
                await new Promise(x => frame.onload = x);
                return frame;
            }
            """.trimIndent(), mapOf(Pair("frameId", name), Pair("url", url))
        )
        return handle.asElement()?.contentFrame()
    }

    protected fun captureLastKeyDown(): IJSHandle {
        val jsScript = """() => {
            |   const lastEvent = {
            |       repeat: false,
            |       location: -1,
            |       code: '',
            |       key: '',
            |       metaKey: false,
            |       keyIdentifier: 'unsupported'
            |   };
            |   document.addEventListener('keydown', e => {
            |       lastEvent.repeat = e.repeat;
            |       lastEvent.location = e.location;
            |       lastEvent.key = e.key;
            |       lastEvent.code = e.code;
            |       lastEvent.metaKey = e.metaKey;
            |       // keyIdentifier only exists in WebKit, and isn't in TypeScript's lib.
            |       lastEvent.keyIdentifier = 'keyIdentifier' in e && e['keyIdentifier'];
            |   }, true);
            |   return lastEvent;
            |}
        """.trimMargin()
        return page.evaluateHandle(jsScript)
    }

    protected fun isMac(): Boolean {
        return getCurrentPlatform() == MAC
    }

    protected fun isWindows(): Boolean {
        return getCurrentPlatform() == WINDOWS32 || getCurrentPlatform() == WINDOWS64
    }
}