package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.options.NewPageOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class TestPage : BaseTest() {

    private fun createPage(
        options: NewPageOptions? = NewPageOptions { it.ignoreHTTPSErrors = true }
    ) = RemoteBrowser.connectWs(wsUrl).newPage(options)

    private fun createContext() = RemoteBrowser.connectWs(wsUrl).newContext()

    @Test
    fun `check all promises are rejected after close page`() {
        val page = createPage()
        page.close()
        try {
            page.evaluate("() => new Promise(r => {})")
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Target page, context or browser has been closed"))
        }
    }

    @Test
    fun `closed page should not be visible in context`() {
        val context = createContext()
        val page = context.newPage()
        assertTrue(context.pages().contains(page))
        page.close()
        assertFalse(context.pages().contains(page))
    }

    @Test
    fun `check navigate method in browser`() {
        val navigatedUrl = "https://localhost:8443/empty.html"
        val page = createPage()
        val response = page.navigate(navigatedUrl)
        assert(response != null)
        assertEquals(navigatedUrl, response?.url())
    }
}