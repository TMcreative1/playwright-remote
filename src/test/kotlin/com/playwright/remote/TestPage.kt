package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.CloseOptions
import com.playwright.remote.engine.options.NewPageOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class TestPage : BaseTest() {

    private fun createPage(
        options: NewPageOptions? = NewPageOptions { it.ignoreHTTPSErrors = true }
    ) = browser.newPage(options)

    @Test
    fun `check all promises are rejected after close page via browser`() {
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
    fun `check all promises are rejected after close page via browser context`() {
        val page = browserContext.newPage()
        page.close()
        try {
            page.evaluate("() => new Promise(r => {})")
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Protocol error"))
        }
    }

    @Test
    fun `closed page should not be visible in context`() {
        val page = browserContext.newPage()
        assertTrue(browserContext.pages().contains(page))
        page.close()
        assertFalse(browserContext.pages().contains(page))
    }

    @Test
    fun `check run beforeunload if asked for`() {
        val page = browserContext.newPage()
        page.navigate("$HTTP_PREFIX/beforeunload.html")

        page.click("body")
        val didShowDialog = arrayOf(false)
        page.onDialog {
            didShowDialog[0] = true
            assertEquals("beforeunload", it.type())
            assertEquals("", it.defaultValue())
            when {
                isChromium() -> {
                    assertEquals("", it.message())
                }
                isWebkit() -> {
                    assertEquals("Leave?", it.message())
                }
                isFirefox() -> {
                    assertEquals(
                        "This page is asking you to confirm that you want to leave - data you have entered may not be saved.",
                        it.message()
                    )
                }
                else -> {
                    assertEquals(
                        "This page is asking you to confirm that you want to leave — information you’ve entered may not be saved.",
                        it.message()
                    )
                }
            }
            it.accept()
        }
        page.close(CloseOptions { it.runBeforeUnload = true })
        for (index in 0..300) {
            if (didShowDialog[0]) {
                break
            }
            page.waitForTimeout(100.0)
        }
        assertTrue(didShowDialog[0])
    }

    @Test
    fun `should not run beforeunload by default`() {
        val page = browserContext.newPage()
        page.navigate("$HTTP_PREFIX/beforeunload.html")
        page.click("body")
        val didShowDialog = arrayOf(false)
        page.onDialog { didShowDialog[0] = true }
        page.close()
        assertFalse(didShowDialog[0])
    }

    @Test
    fun `should set the page close state`() {
        val page = browserContext.newPage()
        assertFalse(page.isClosed())
        page.close()
        assertTrue(page.isClosed())
    }

    @Test
    fun `should terminate network waiters`() {
        val page = browserContext.newPage()
        try {
            page.waitForResponse("**") {
                try {
                    page.waitForRequest("$HTTP_PREFIX/empty.html") { page.close() }
                    fail("waitForRequest() should throw")
                } catch (e: PlaywrightException) {
                    assertTrue(e.message!!.contains("Page closed"))
                    assertFalse(e.message!!.contains("Timeout"))
                }
            }
            fail("waitForResponse() should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Page closed"))
            assertFalse(e.message!!.contains("Timeout"))
        }
    }

    @Test
    fun `check that close should be callable several times`() {
        val page = browserContext.newPage()
        try {
            page.close()
            page.close()
            page.close()
        } catch (e: PlaywrightException) {
            fail("Error should not be thrown")
        }
    }

    @Test
    fun `check that page url should include hashes`() {
        var expectedUrl = "$HTTP_PREFIX/empty.html#hash"
        val page = browserContext.newPage()
        page.navigate(expectedUrl)
        assertEquals(expectedUrl, page.url())
        page.evaluate("() => { window.location.hash = 'dynamic'; }")
        expectedUrl = "$HTTP_PREFIX/empty.html#dynamic"
        assertEquals(expectedUrl, page.url())
    }

    @Test
    fun `check navigate method in browser`() {
        val navigatedUrl = "$HTTPS_PREFIX/empty.html"
        val page = createPage()
        val response = page.navigate(navigatedUrl)
        assert(response != null)
        assertEquals(navigatedUrl, response?.url())
    }
}