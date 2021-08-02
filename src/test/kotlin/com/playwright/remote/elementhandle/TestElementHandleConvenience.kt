package com.playwright.remote.elementhandle

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Test
import kotlin.test.*

class TestElementHandleConvenience : BaseTest() {

    @Test
    fun `check to have a nice preview`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val outer = page.querySelector("#outer")
        val inner = page.querySelector("#inner")
        val check = page.querySelector("#check")

        assertNotNull(outer)
        assertNotNull(inner)
        assertNotNull(check)

        val text = inner.evaluateHandle("e => e.firstChild")
        assertNotNull(text)
        page.evaluate("() => 1")

        assertEquals("JSHandle@<div id=\"outer\" name=\"value\">…</div>", outer.toString())
        assertEquals("JSHandle@<div id=\"inner\">Text,↵more text</div>", inner.toString())
        assertEquals("JSHandle@#text=Text,↵more text", text.toString())
        assertEquals("JSHandle@<input checked id=\"check\" foo=\"bar\"\" type=\"checkbox\"/>", check.toString())
    }

    @Test
    fun `check correct work of get attribute`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val handle = page.querySelector("#outer")
        assertNotNull(handle)
        assertEquals("value", handle.getAttribute("name"))
        assertNull(handle.getAttribute("foo"))
        assertEquals("value", page.getAttribute("#outer", "name"))
        assertNull(page.getAttribute("#outer", "null"))
    }

    @Test
    fun `check to input value`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")

        page.fill("#textarea", "textarea value")
        assertEquals("textarea value", page.inputValue("#textarea"))

        page.fill("#input", "input value")
        assertEquals("input value", page.inputValue("#input"))
        val handle = page.querySelector("#input")
        assertNotNull(handle)
        assertEquals("input value", handle.inputValue())

        try {
            page.inputValue("#inner")
            fail("inputValue should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Node is not an HTMLInputElement or HTMLTextAreaElement"), e.message)
        }
        val handle2 = page.querySelector("#inner")
        assertNotNull(handle2)
        try {
            handle2.inputValue()
            fail("inputValue should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Node is not an HTMLInputElement or HTMLTextAreaElement"), e.message)
        }
    }
}