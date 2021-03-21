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

    protected fun launchChromeBrowser(): String {
        return server.launchServer(getCurrentPlatform(), CHROMIUM)!!
    }

    protected fun launchFirefoxBrowser(): String {
        return server.launchServer(getCurrentPlatform(), FIREFOX)!!
    }

    protected fun launchSafariBrowser(): String {
        return server.launchServer(getCurrentPlatform(), WEBKIT)!!
    }

    protected fun stopServer() {
        server.stopServer()
    }

}