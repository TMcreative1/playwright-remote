package com.playwright.remote.base

import com.playwright.remote.base.server.Server
import com.playwright.remote.core.enums.BrowserType
import com.playwright.remote.core.enums.BrowserType.valueOf
import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.browser.api.IBrowser
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.server.api.IServerProvider
import com.playwright.remote.engine.server.impl.ServerProvider
import com.playwright.remote.utils.PlatformUtils.Companion.getCurrentPlatform
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach


open class BaseTest {

    companion object {

        @JvmStatic
        val HTTPS_PREFIX = "https://localhost:8443"

        @JvmStatic
        val HTTP_PREFIX = "http://localhost:8080"

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
        private lateinit var server: IServerProvider

        @JvmStatic
        @BeforeAll
        fun startHttpServers() {
            httpServer = Server.createHttp(8080)
            httpsServer = Server.createHttps(8443)
        }

        @JvmStatic
        @AfterAll
        fun stopHttpServers() {
            httpServer.stop()
            httpsServer.stop()
        }

        @JvmStatic
        @BeforeAll
        private fun launchBrowserServer() {
            server = ServerProvider()
            wsUrl = server.launchServer(
                getCurrentPlatform(),
                getBrowserType()
            )!!
        }

        @JvmStatic
        @AfterAll
        private fun stopServer() {
            server.stopServer()
        }

        @JvmStatic
        private fun getBrowserType(): BrowserType =
            valueOf(System.getProperty("browser").ifEmpty { "webkit" }.toUpperCase())

        @JvmStatic
        fun isChromium() = getBrowserType().browserName == "chromium"

        @JvmStatic
        fun isWebkit() = getBrowserType().browserName == "webkit"

        @JvmStatic
        fun isFirefox() = getBrowserType().browserName == "firefox"
    }

    @BeforeEach
    private fun createBrowser() {
        browser = RemoteBrowser.connectWs(wsUrl)
        browserContext = browser.newContext()
    }

}