package com.playwright.remote.engine.selector.impl

import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.domain.selector.SelectorRegistration
import com.playwright.remote.engine.options.RegisterOptions
import com.playwright.remote.engine.selector.api.ISelectors
import com.playwright.remote.engine.selector.api.ISharedSelectors
import okio.IOException
import java.nio.file.Files
import java.nio.file.Path

class SharedSelectors : ISharedSelectors {
    private val selectors = arrayListOf<ISelectors>()
    private val registrations = arrayListOf<SelectorRegistration>()

    override fun register(name: String, script: String, options: RegisterOptions?) {
        selectors.forEach { it.register(name, script, options) }
        registrations.add(SelectorRegistration(name, script, options))
    }

    override fun register(name: String, path: Path, options: RegisterOptions?) {
        val buffer: ByteArray
        try {
            buffer = Files.readAllBytes(path)
        } catch (e: IOException) {
            throw PlaywrightException("Failed to read selector from file: $path", e)
        }
        register(name, String(buffer, Charsets.UTF_8), options)
    }

    override fun addSelector(selector: ISelectors) {
        registrations.forEach { selector.register(it.name, it.script, it.options) }
        selectors.add(selector)
    }

    override fun removeSelector(selector: ISelectors) {
        selectors.remove(selector)
    }
}