package com.playwright.remote.elementhandle

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.element.SelectTextOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestElementSelectText : BaseTest() {

    @Test
    fun `check to select textarea`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        textarea.evaluate("textarea => textarea.value = 'my value'")
        textarea.selectText()
        if (isFirefox()) {
            assertEquals(0, textarea.evaluate("el => el.selectionStart"))
            assertEquals(8, textarea.evaluate("el => el.selectionEnd"))
        } else {
            assertEquals("my value", page.evaluate("() => window.getSelection().toString()"))
        }
    }

    @Test
    fun `check to select input`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val input = page.querySelector("input")
        assertNotNull(input)
        input.evaluate("input => input.value = 'my value'")
        input.selectText()
        if (isFirefox()) {
            assertEquals(0, input.evaluate("el => el.selectionStart"))
            assertEquals(8, input.evaluate("el => el.selectionEnd"))
        } else {
            assertEquals("my value", page.evaluate("() => window.getSelection().toString()"))
        }
    }

    @Test
    fun `check to select plain div`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val div = page.querySelector("div.plain")
        assertNotNull(div)
        div.selectText()
        assertEquals("Plain div", page.evaluate("() => window.getSelection().toString()"))
    }

    @Test
    fun `check to throw error for invisible element with timeout`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textArea = page.querySelector("textarea")
        assertNotNull(textArea)
        textArea.evaluate("e => e.style.display = 'none'")
        try {
            textArea.selectText(SelectTextOptions{it.timeout = 3000.0})
            fail("selectText should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("element is not visible"))
        }
    }
}