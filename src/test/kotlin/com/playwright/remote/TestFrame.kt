package com.playwright.remote

import com.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
}