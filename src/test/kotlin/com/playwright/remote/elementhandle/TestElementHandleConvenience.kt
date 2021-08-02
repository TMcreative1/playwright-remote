package com.playwright.remote.elementhandle

import com.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

        page.fill("#textarea", "my value")
        assertEquals("my value", page.inputValue("#textarea"))
    }
}