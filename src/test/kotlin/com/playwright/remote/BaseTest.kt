package com.playwright.remote

import com.playwright.remote.core.enums.BrowserType.*
import com.playwright.remote.engine.server.api.IServerProvider
import com.playwright.remote.engine.server.impl.ServerProvider
import com.playwright.remote.utils.PlatformUtils.Companion.getCurrentPlatform


open class BaseTest {

    private val server: IServerProvider

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