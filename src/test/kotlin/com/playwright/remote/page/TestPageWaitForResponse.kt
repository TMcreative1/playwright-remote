package com.playwright.remote.page

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.wait.WaitForResponseOptions
import com.playwright.remote.engine.route.response.api.IResponse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestPageWaitForResponse : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate(httpServer.emptyPage)
        val response = page.waitForResponse("${httpServer.prefixWithDomain}/digits/2.png") {
            val jsScript = """() => {
                |   fetch('/digits/1.png');
                |   fetch('/digits/2.png');
                |   fetch('/digits/3.png');
                |}
            """.trimMargin()
            page.evaluate(jsScript)
        }
        assertNotNull(response)
        assertEquals("${httpServer.prefixWithDomain}/digits/2.png", response.url())
    }

    @Test
    fun `check correct work with respect timeout`() {
        page.navigate(httpServer.emptyPage)
        val predicate: (IResponse) -> Boolean = { r -> r.url() == "${httpServer.prefixWithDomain}/digits/2.png" }
        val response = page.waitForResponse(predicate) {
            val jsScript = """() => {
                |   fetch('/digits/1.png');
                |   fetch('/digits/2.png');
                |   fetch('/digits/3.png');
                |}
            """.trimMargin()
            page.evaluate(jsScript)
        }
        assertNotNull(response)
        assertEquals("${httpServer.prefixWithDomain}/digits/2.png", response.url())
    }

    @Test
    fun `check correct work with default timeout`() {
        page.setDefaultTimeout(1.0)
        try {
            page.waitForResponse({ false }) {}
            fail("waitForResponse should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Timeout"), e.message)
        }
    }

    @Test
    fun `check correct work without timeout`() {
        page.navigate(httpServer.emptyPage)
        val response = page.waitForResponse(
            "${httpServer.prefixWithDomain}/digits/2.png",
            WaitForResponseOptions { it.timeout = 0.0 }) {
            val jsScript = """() => setTimeout (()=> {
                |   fetch('/digits/1.png');
                |   fetch('/digits/2.png');
                |   fetch('/digits/3.png');
                |}, 50)
            """.trimMargin()
            page.evaluate(jsScript)
        }
        assertNotNull(response)
        assertEquals("${httpServer.prefixWithDomain}/digits/2.png", response.url())
    }
}