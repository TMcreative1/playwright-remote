package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.NavigateOptions
import io.github.tmcreative1.playwright.remote.core.enums.LoadState
import io.github.tmcreative1.playwright.remote.core.exceptions.TimeoutException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestFrame : BaseTest() {

    @Test
    fun `frame press should work`() {
        page.setContent("<iframe name=inner src='${httpServer.prefixWithDomain}/input/textarea.html'></iframe>")
        val pressButton = "q"
        val frame = page.frame("inner")
        assertNotNull(frame)
        frame.press("textarea", pressButton)
        assertEquals(pressButton, frame.evaluate("() => document.querySelector('textarea').value"))
    }

    @Test
    fun `frame focus should work multiple times`() {
        val pageOne = browserContext.newPage()
        val pageTwo = browserContext.newPage()
        arrayListOf(pageOne, pageTwo).forEach {
            it.setContent("<button id='foo' onfocus='window.goFocus=true'></button>")
            it.focus("#foo")
            assertEquals(true, it.evaluate("() => !!window['goFocus']"))
        }
    }

    @Test
    fun `check to navigate sub-frames`() {
        page.navigate("${httpServer.prefixWithDomain}/frames/one-frame.html")
        assertTrue(page.frames()[0].url().contains("/frames/one-frame.html"))
        assertTrue(page.frames()[1].url().contains("/frames/frame.html"))

        val response = page.frames()[1].navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertTrue(response.ok())
        assertEquals(page.frames()[1], response.frame())
    }

    @Test
    fun `check to continue after client redirect`() {
        httpServer.setRoute("/scripts/frame.js") {}
        val url = "${httpServer.prefixWithDomain}/frames/child-redirect.html"
        try {
            page.navigate(url, NavigateOptions {
                it.timeout = 5000.0
                it.waitUntil = LoadState.NETWORKIDLE.value
            })
            fail("navigate should throw")
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 5000ms exceeded."))
            assertTrue(e.message!!.contains("navigating to \"${url}\", waiting until \"networkidle\""))
        }
    }
}