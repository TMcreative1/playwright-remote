package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestPageExtraHttpHeaders : BaseTest() {
    @Test
    fun `check correct work of http headers`() {
        page.setExtraHTTPHeaders(mapOf("foo" to "bar"))
        val request = httpServer.futureRequest("/empty.html")
        page.navigate(httpServer.emptyPage)
        assertEquals(listOf("bar"), request.get().headers["foo"])
        assertNull(request.get().headers["baz"])
    }

    @Test
    fun `check correct work of http headers with redirects`() {
        httpServer.setRedirect("/foo.html", "/empty.html")
        page.setExtraHTTPHeaders(mapOf("foo" to "bar"))
        val request = httpServer.futureRequest("/empty.html")
        page.navigate("${httpServer.prefixWithDomain}/foo.html")
        assertEquals(listOf("bar"), request.get().headers["foo"])
    }

    @Test
    fun `check correct work of http headers from browser context`() {
        browserContext.setExtraHttpHeaders(mapOf("foo" to "bar"))
        val newPage = browserContext.newPage()
        val request = httpServer.futureRequest("/empty.html")
        newPage.navigate(httpServer.emptyPage)
        browserContext.close()
        assertEquals(listOf("bar"), request.get().headers["foo"])
    }

    @Test
    fun `check to override extra headers from browser context`() {
        val context =
            browser.newContext(NewContextOptions { it.extraHTTPHeaders = mapOf("fOo" to "bAr", "baR" to "foO") })
        val newPage = context.newPage()
        newPage.setExtraHTTPHeaders(mapOf("Foo" to "Bar"))
        val request = httpServer.futureRequest("/empty.html")
        newPage.navigate(httpServer.emptyPage)
        context.close()
        assertEquals(listOf("Bar"), request.get().headers["foo"])
        assertEquals(listOf("foO"), request.get().headers["bar"])
    }

    @Test
    fun `check to throw for non string header value`() {
        try {
            browser.newContext(NewContextOptions { it.extraHTTPHeaders = mapOf("foo" to null) })
            fail("newContext should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("expected string, got undefined"))
        }
    }
}