package com.playwright.remote.engine.server.impl

import com.playwright.remote.core.enums.BrowserType
import com.playwright.remote.core.enums.Platform
import com.playwright.remote.engine.server.api.IServerProvider
import java.nio.file.Paths

class ServerProvider : IServerProvider {
    private lateinit var process: Process

    @JvmOverloads
    override fun launchServer(platform: Platform, browserType: BrowserType): String? {
        val pb = ProcessBuilder().apply {
            val driverPath = Paths.get("drivers/${platform.platformType}")
            val node = driverPath.resolve(platform.nodeProcess).toString()
            val cli = driverPath.resolve("package/lib/cli/cli.js").toString()
            command(node, cli, "launch-server", browserType.browserName)
        }

        process = pb.start()
        return process.inputStream.bufferedReader().useLines {
            it.firstOrNull()
        }
    }


    override fun stopServer(): Int = process.runCatching {
        destroy()
        waitFor()
    }.getOrThrow()
}