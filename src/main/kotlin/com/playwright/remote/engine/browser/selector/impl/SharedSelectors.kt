package com.playwright.remote.engine.browser.selector.impl

import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.domain.selector.Registration
import com.playwright.remote.engine.browser.selector.api.ISelectors
import com.playwright.remote.engine.browser.selector.api.ISharedSelectors
import com.playwright.remote.engine.options.RegisterOptions
import okio.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class SharedSelectors : ISharedSelectors {
    private val channels = arrayListOf<ISelectors>()
    private val registrations = arrayListOf<Registration>()

    override fun register(name: String, script: String, options: RegisterOptions?) {
        channels.forEach { it.register(name, script, options) }
        registrations.add(Registration(name, script, options))
    }

    override fun register(name: String, path: Path, options: RegisterOptions?) {
        val buffer: ByteArray
        try {
            buffer = Files.readAllBytes(path)
        } catch (e: IOException) {
            throw PlaywrightException("Failed to read selector from file: $path", e)
        }
        register(name, String(buffer, StandardCharsets.UTF_8), options)
    }

    override fun addChannel(channel: ISelectors) {
        registrations.forEach { channel.register(it.name, it.script, it.options) }
        channels.add(channel)
    }

    override fun removeChannel(channel: ISelectors) {
        channels.remove(channel)
    }
}