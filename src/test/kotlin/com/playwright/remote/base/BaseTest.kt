package com.playwright.remote.base

import com.playwright.remote.base.server.Server
import com.playwright.remote.core.enums.BrowserType
import com.playwright.remote.core.enums.BrowserType.valueOf
import com.playwright.remote.core.enums.Platform.*
import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.browser.api.IBrowser
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.server.api.IServerProvider
import com.playwright.remote.engine.server.impl.ServerProvider
import com.playwright.remote.utils.PlatformUtils.Companion.getCurrentPlatform
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import java.util.concurrent.atomic.AtomicInteger


open class BaseTest {

    companion object {

        private val httpPort = AtomicInteger(9000)
        private val httpsPort = AtomicInteger(4000)
        private val browserThreadLocal = ThreadLocal<IBrowser>()
        private val browserContextThreadLocal = ThreadLocal<IBrowserContext>()
        private val pageThreadLocal = ThreadLocal<IPage>()
        private val httpServerThreadLocal = ThreadLocal<Server>()
        private val httpsServerThreadLocal = ThreadLocal<Server>()
        private val serverProviderThreadLocal = ThreadLocal<IServerProvider>()
        private val wsUrlThreadLocal = ThreadLocal<String>()

        @JvmStatic
        var httpServer: Server
            get() = httpServerThreadLocal.get()
            set(value) = httpServerThreadLocal.set(value)

        @JvmStatic
        var httpsServer: Server
            get() = httpsServerThreadLocal.get()
            set(value) = httpsServerThreadLocal.set(value)

        @JvmStatic
        var wsUrl: String
            get() = wsUrlThreadLocal.get()
            set(value) = wsUrlThreadLocal.set(value)

        @JvmStatic
        var browser: IBrowser
            get() = browserThreadLocal.get()
            set(value) = browserThreadLocal.set(value)

        @JvmStatic
        var browserContext: IBrowserContext
            get() = browserContextThreadLocal.get()
            set(value) = browserContextThreadLocal.set(value)

        @JvmStatic
        var page: IPage
            get() = pageThreadLocal.get()
            set(value) = pageThreadLocal.set(value)

        @JvmStatic
        private var server: IServerProvider
            get() = serverProviderThreadLocal.get()
            set(value) = serverProviderThreadLocal.set(value)

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            launchBrowserServer()
            createHttpServers()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            stopServerBrowserServer()
            stopHttpServers()
        }

        private fun createHttpServers() {
            httpServer = Server.createHttp(httpPort.getAndIncrement())
            httpsServer = Server.createHttps(httpsPort.getAndIncrement())
        }

        private fun stopHttpServers() {
            httpServer.stop()
            httpsServer.stop()
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
        httpServer.reset()
        httpsServer.reset()
        createBrowser()
    }

    @AfterEach
    private fun afterEach() {
        destroyBrowser()
    }

    private fun createBrowser() {
        browser = RemoteBrowser.connectWs(wsUrl)
        browserContext = browser.newContext()
        page = browserContext.newPage()
    }

    private fun destroyBrowser() {
        page.close()
        browserContext.close()
        browser.close()
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

    protected fun isMac(): Boolean {
        return getCurrentPlatform() == MAC
    }

    protected fun isWindows(): Boolean {
        return getCurrentPlatform() == WINDOWS32 || getCurrentPlatform() == WINDOWS64
    }

    protected fun isLinux(): Boolean {
        return getCurrentPlatform() == LINUX
    }
}