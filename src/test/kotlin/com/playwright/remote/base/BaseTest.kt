package com.playwright.remote.base

import com.playwright.remote.base.server.Server
import com.playwright.remote.core.enums.BrowserType.valueOf
import com.playwright.remote.engine.server.api.IServerProvider
import com.playwright.remote.engine.server.impl.ServerProvider
import com.playwright.remote.utils.PlatformUtils.Companion.getCurrentPlatform
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll


open class BaseTest {

    companion object {
        @JvmStatic
        lateinit var httpServer: Server

        @JvmStatic
        lateinit var httpsServer: Server

        @JvmStatic
        lateinit var wsUrl: String

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
                valueOf(System.getProperty("browser").ifEmpty { "chromium" }.toUpperCase())
            )!!
        }

        @JvmStatic
        @AfterAll
        private fun stopServer() {
            server.stopServer()
        }

    }

}