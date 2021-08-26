package com.playwright.remote.base.extension

import com.playwright.remote.core.enums.BrowserType.valueOf
import com.playwright.remote.engine.server.api.IServerProvider
import com.playwright.remote.engine.server.impl.ServerProvider
import com.playwright.remote.utils.PlatformUtils.Companion.getCurrentPlatform
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.locks.ReentrantLock

class ServerProviderExtension : BeforeAllCallback {
    companion object {
        private var server: IServerProvider? = null
        private val lock = ReentrantLock()
    }

    override fun beforeAll(context: ExtensionContext?) {
        lock.lock()
        if (server == null) {
            server = ServerProvider()
            val wsUrl = server!!.launchServer(
                getCurrentPlatform(),
                valueOf(System.getProperty("browser").ifEmpty { "webkit" }.uppercase())
            )
            System.setProperty("wsUrl", wsUrl!!)
            Runtime.getRuntime().addShutdownHook(Thread { server!!.stopServer() })
        }
        lock.unlock()
    }
}