package io.github.tmcreative1.playwright.remote.engine.server.impl

import io.github.tmcreative1.playwright.remote.engine.server.api.IServerProvider
import io.github.tmcreative1.playwright.remote.core.enums.BrowserType
import io.github.tmcreative1.playwright.remote.core.enums.Platform
import io.github.tmcreative1.playwright.remote.engine.logger.CustomLogger
import java.nio.file.Paths

class ServerProvider : IServerProvider {

    private lateinit var process: Process
    private val logger = CustomLogger()

    override fun launchServer(platform: Platform, browserType: BrowserType): String? {
        val pb = ProcessBuilder().apply {
            val driverPath = Paths.get("drivers/${platform.platformType}")
            val node = driverPath.resolve(platform.nodeProcess).toString()
            val cli = driverPath.resolve("package/lib/cli/cli.js").toString()
            val config = driverPath.resolve("config.json").toString()
            command(node, cli, "launch-server", browserType.browserName, config)
        }

        logger.logInfo("Playwright server is starting")
        process = pb.start()
        val firstLine = process.inputStream.bufferedReader().useLines {
            it.firstOrNull()
        }
        logger.logInfo("Playwright server started with url $firstLine")
        return firstLine
    }


    override fun stopServer() {
        logger.logInfo("Playwright server is stopping")
        process.runCatching {
            destroy()
            waitFor()
        }.getOrThrow()
        logger.logInfo("Playwright server was stopped")
    }
}