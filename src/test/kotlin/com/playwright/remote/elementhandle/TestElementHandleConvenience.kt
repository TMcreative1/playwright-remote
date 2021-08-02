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

    @Test
    fun `check correct work of inner html`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val handle = page.querySelector("#outer")
        assertNotNull(handle)
        assertEquals("\n<div id=\"inner\">Text,\nmore text</div>\n", handle.innerHTML())
        assertEquals("\n<div id=\"inner\">Text,\nmore text</div>\n", page.innerHTML("#outer"))
    }

    @Test
    fun `check correct work of inner text`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val handle = page.querySelector("#inner")
        assertNotNull(handle)
        assertEquals("Text, more text", handle.innerText())
        assertEquals("Text, more text", page.innerText("#inner"))
    }

    @Test
    fun `check to throw in inner text`() {
        page.setContent("<svg>text</svg>")
        try {
            page.innerText("svg")
            fail("innerText should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Not an HTMLElement"))
        }
        val handle = page.querySelector("svg")
        assertNotNull(handle)
        try {
            handle.innerText()
            fail("innerText should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Not an HTMLElement"))
        }
    }

    @Test
    fun `check correct work of text content`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val handle = page.querySelector("#inner")
        assertNotNull(handle)
        assertEquals("Text,\nmore text", handle.textContent())
        assertEquals("Text,\nmore text", page.textContent("#inner"))
    }

    @Test
    fun `check correct work of is visible and is hidden`() {
        page.setContent("<div>Hi</div><span></span>")
        val div = page.querySelector("div")
        assertNotNull(div)
        assertTrue(div.isVisible())
        assertFalse(div.isHidden())
        assertTrue(page.isVisible("div"))
        assertFalse(page.isHidden("div"))

        val span = page.querySelector("span")
        assertNotNull(span)
        assertFalse(span.isVisible())
        assertTrue(span.isHidden())
        assertFalse(page.isVisible("span"))
        assertTrue(page.isHidden("span"))
    }

    @Test
    fun `check correct work of is enabled and is disabled`() {
        val content = """<button disabled>button1</button>
            |<button>button2</button>
            |<div>div</div>
        """.trimMargin()
        page.setContent(content)

        val div = page.querySelector("div")
        assertNotNull(div)
        assertTrue(div.isEnabled())
        assertFalse(div.isDisabled())
        assertTrue(page.isEnabled("div"))
        assertFalse(page.isDisabled("div"))

        val button1 = page.querySelector(":text('button1')")
        assertNotNull(button1)
        assertFalse(button1.isEnabled())
        assertTrue(button1.isDisabled())
        assertFalse(page.isEnabled(":text('button1')"))
        assertTrue(page.isDisabled(":text('button1')"))

        val button2 = page.querySelector(":text('button2')")
        assertNotNull(button2)
        assertTrue(button2.isEnabled())
        assertFalse(button2.isDisabled())
        assertTrue(page.isEnabled(":text('button2')"))
        assertFalse(page.isDisabled(":text('button2')"))
    }

    @Test
    fun `check correct work of is editable`() {
        page.setContent("<input id=input1 disabled><textarea></textarea><input id=input2>")
        page.evalOnSelector("textarea", "t => t.readOnly = true")

        val input1 = page.querySelector("#input1")
        assertNotNull(input1)
        assertFalse(input1.isEditable())
        assertFalse(page.isEditable("#input1"))

        val input2 = page.querySelector("#input2")
        assertNotNull(input2)
        assertTrue(input2.isEditable())
        assertTrue(page.isEditable("#input2"))

        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        assertFalse(textarea.isEditable())
        assertFalse(page.isEditable("textarea"))
    }

    @Test
    fun `check correct work of is checked`() {
        page.setContent("<input type='checkbox' checked><div>Not a checkbox</div>")

        val handle = page.querySelector("input")
        assertNotNull(handle)
        assertTrue(handle.isChecked())
        assertTrue(page.isChecked("input"))

        handle.evaluate("input => input.checked = false")
        assertFalse(handle.isChecked())
        assertFalse(page.isChecked("input"))

        try {
            page.isChecked("div")
            fail("isChecked should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Not a checkbox or radio button"))
        }
    }
}