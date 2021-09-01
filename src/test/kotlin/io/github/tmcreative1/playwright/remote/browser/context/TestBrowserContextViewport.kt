package io.github.tmcreative1.playwright.remote.browser.context

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.ViewportSize
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestBrowserContextViewport : BaseTest() {

    @Test
    fun `check to get the proper default view port size`() {
        verifyViewport(page, 1280, 720)
    }

    @Test
    fun `check to set the proper viewport size`() {
        verifyViewport(page, 1280, 720)
        page.setViewportSize(123, 456)
        verifyViewport(page, 123, 456)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `check to return correct outer width and outer height`() {
        val jsScript = """() => {
            |   return {
            |       innerWidth: window.innerWidth,
            |       innerHeight: window.innerHeight,
            |       outerWidth: window.outerWidth,
            |       outerHeight: window.outerHeight,
            |   };
            |}
        """.trimMargin()
        val size = page.evaluate(jsScript) as Map<String, Int>
        assertEquals(1280, size["innerWidth"])
        assertEquals(720, size["innerHeight"])
        assertTrue(size["outerWidth"] as Int >= size["innerWidth"] as Int)
        assertTrue(size["outerHeight"] as Int >= size["innerHeight"] as Int)
    }

    @Test
    fun `check to emulate device width`() {
        verifyViewport(page, 1280, 720)
        page.setViewportSize(200, 200)
        assertEquals(200, page.evaluate("() => window.screen.width"))
        assertEquals(true, page.evaluate("() => matchMedia('(min-device-width: 100px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(min-device-width: 300px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(max-device-width: 100px)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(max-device-width: 300px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(device-width: 500px)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(device-width: 200px)').matches"))

        page.setViewportSize(500, 500)
        assertEquals(500, page.evaluate("() => window.screen.width"))
        assertEquals(true, page.evaluate("() => matchMedia('(min-device-width: 400px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(min-device-width: 600px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(max-device-width: 400px)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(max-device-width: 600px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(device-width: 200px)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(device-width: 500px)').matches"))
    }

    @Test
    fun `check to emulate device height`() {
        verifyViewport(page, 1280, 720)
        page.setViewportSize(200, 200)
        assertEquals(200, page.evaluate("() => window.screen.height"))
        assertEquals(true, page.evaluate("() => matchMedia('(min-device-height: 100px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(min-device-height: 300px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(max-device-height: 100px)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(max-device-height: 300px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(device-height: 500px)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(device-height: 200px)').matches"))

        page.setViewportSize(500, 500)
        assertEquals(500, page.evaluate("() => window.screen.height"))
        assertEquals(true, page.evaluate("() => matchMedia('(min-device-height: 400px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(min-device-height: 600px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(max-device-height: 400px)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(max-device-height: 600px)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(device-height: 200px)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(device-height: 500px)').matches"))
    }

    @Test
    fun `check to emulate avail width and avail height`() {
        page.setViewportSize(500, 600)
        assertEquals(500, page.evaluate("() => window.screen.availWidth"))
        assertEquals(600, page.evaluate("() => window.screen.availHeight"))
    }

    @Test
    fun `check to not have touch by default`() {
        page.navigate("${httpServer.prefixWithDomain}/mobile.html")
        assertEquals(false, page.evaluate("() => 'ontouchstart' in window"))
        page.navigate("${httpServer.prefixWithDomain}/detect-touch.html")
        assertEquals("NO", page.evaluate("() => document.body.textContent.trim()"))
    }

    @Test
    fun `check to support touch with null viewport`() {
        browser.newContext(NewContextOptions {
            it.hasTouch = true
            it.viewportSize = null
        }).use {
            val pg = it.newPage()
            pg.navigate("${httpServer.prefixWithDomain}/mobile.html")
            assertEquals(true, pg.evaluate("() => 'ontouchstart' in window"))
        }
    }

    @Test
    fun `check to report null viewport size when given null viewport`() {
        browser.newContext(NewContextOptions { it.viewportSize = null }).use {
            val pg = it.newPage()
            assertEquals(ViewportSize {}, pg.viewportSize())
        }
    }
}