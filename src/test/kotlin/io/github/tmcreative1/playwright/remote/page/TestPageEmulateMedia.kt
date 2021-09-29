package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.enums.Media
import io.github.tmcreative1.playwright.remote.engine.options.EmulateMediaOptions
import io.github.tmcreative1.playwright.remote.engine.options.NewPageOptions
import io.github.tmcreative1.playwright.remote.engine.options.enum.ColorScheme
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestPageEmulateMedia : BaseTest() {

    @Test
    fun `check emulate type`() {
        assertEquals(true, page.evaluate("() => matchMedia('screen').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('print').matches"))

        page.emulateMedia(EmulateMediaOptions { it.media = Media.PRINT })
        assertEquals(false, page.evaluate("() => matchMedia('screen').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('print').matches"))

        page.emulateMedia(EmulateMediaOptions {})
        assertEquals(false, page.evaluate("() => matchMedia('screen').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('print').matches"))

        page.emulateMedia(EmulateMediaOptions { it.media = null })
        assertEquals(true, page.evaluate("() => matchMedia('screen').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('print').matches"))
    }

    @Test
    fun `check emulate scheme work`() {
        page.emulateMedia(EmulateMediaOptions { it.colorScheme = ColorScheme.LIGHT })
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = ColorScheme.DARK })
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))
    }

    @Test
    fun `check default scheme is light`() {
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = ColorScheme.DARK })
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = null })
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))
    }

    @Test
    fun `check work in cross provess iframe`() {
        val page = browser.newPage(NewPageOptions { it.colorScheme = ColorScheme.DARK })
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
        val frame = page.frames()[1]
        assertEquals(true, frame.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))
        page.close()
    }

    @Test
    fun `check changing the actual colors in css`() {
        val content = """
            <style>
                @media (prefers-color-scheme: dark) {
                    div {
                        background: black;
                        color: white;
                    }
                }

                @media (prefers-color-scheme: light) {
                    div {
                        background: white;
                        color: black;
                    }
                }
            </style>
            <div>Test</div>
        """.trimIndent()
        page.setContent(content)
        val backgroundColor = { page.evalOnSelector("div", "div => window.getComputedStyle(div).backgroundColor") }

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = ColorScheme.LIGHT })
        assertEquals("rgb(255, 255, 255)", backgroundColor())

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = ColorScheme.DARK })
        assertEquals("rgb(0, 0, 0)", backgroundColor())

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = ColorScheme.LIGHT })
        assertEquals("rgb(255, 255, 255)", backgroundColor())
    }
}