package com.playwright.remote.base

import com.playwright.remote.base.server.Server
import com.playwright.remote.core.enums.BrowserType.*
import com.playwright.remote.engine.server.api.IServerProvider
import com.playwright.remote.engine.server.impl.ServerProvider
import com.playwright.remote.utils.PlatformUtils.Companion.getCurrentPlatform
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll


open class BaseTest {

    private val server: IServerProvider

    companion object {
        @JvmStatic
        lateinit var httpServer: Server
        @JvmStatic
        lateinit var httpsServer: Server

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

    }

    init {
        server = ServerProvider()
    }

    protected fun launchChromeBrowserServer(): String {
        return server.launchServer(getCurrentPlatform(), CHROMIUM)!!
    }

    protected fun launchFirefoxBrowserServer(): String {
        return server.launchServer(getCurrentPlatform(), FIREFOX)!!
    }

    protected fun launchSafariBrowserServer(): String {
        return server.launchServer(getCurrentPlatform(), WEBKIT)!!
    }

    protected fun stopServer() {
        server.stopServer()
    }

}