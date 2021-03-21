package com.playwright.remote.engine.server.impl

import com.playwright.remote.core.enums.BrowserType
import com.playwright.remote.core.enums.Platform
import com.playwright.remote.engine.server.api.IServerProvider
import java.nio.file.Paths

class ServerProvider : IServerProvider {
    private lateinit var process: Process

    override fun launchServer(platform: Platform, browserType: BrowserType): String? {
        val pb = ProcessBuilder().apply {
            val driverPath = Paths.get(String.format("drivers/%s", platform.platformType))
            val node = driverPath.resolve(platform.nodeProcess).toString()
            val cli = driverPath.resolve("package/lib/cli/cli.js").toString()
            command(node, cli, "launch-server", browserType.browserName)
        }

        process = pb.start()
        return process.inputStream.bufferedReader().lineSequence().firstOrNull()

    }

    override fun stopServer() {
        try {
            process.destroy()
            process.waitFor()
        } catch (e: InterruptedException) {
            throw RuntimeException(e.message, e.cause)
        }
    }
}