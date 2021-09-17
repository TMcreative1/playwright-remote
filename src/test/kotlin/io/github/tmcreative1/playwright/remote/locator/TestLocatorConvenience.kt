package io.github.tmcreative1.playwright.remote.locator

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Test
import kotlin.test.*

class TestLocatorConvenience : BaseTest() {

    @Test
    fun `check to have a nice preview`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val outer = page.locator("#outer")
        val inner = outer.locator("#inner")
        val check = page.locator("#check")
        val text = inner.evaluateHandle("e => e.firstChild")
        page.evaluate("() => 1")
        assertEquals("Locator@#outer", outer.toString())
        assertEquals("Locator@#outer >> #inner", inner.toString())
        assertEquals("JSHandle@#text=Text,↵        more text↵    ", text.toString())
        assertEquals("Locator@#check", check.toString())
    }

    @Test
    fun `check to get attribute should work`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val locator = page.locator("#outer")
        assertEquals("value", locator.getAttribute("name"))
        assertNull(locator.getAttribute("foo"))
        assertEquals("value", page.getAttribute("#outer", "name"))
        assertNull(page.getAttribute("#outer", "foo"))
    }

    @Test
    fun `check to input value`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")

        page.selectOption("#select", "foo")
        assertEquals("foo", page.inputValue("#select"))

        page.fill("#textarea", "text value")
        assertEquals("text value", page.inputValue("#textarea"))

        page.fill("#input", "input value")
        assertEquals("input value", page.inputValue("#input"))

        val locator = page.locator("#input")
        assertEquals("input value", locator.inputValue())

        try {
            page.inputValue("#inner")
            fail("inputValue should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Node is not an HTMLInputElement or HTMLTextAreaElement or HTMLSelectElement"))
        }

        try {
            val locator2 = page.locator("#inner")
            locator2.inputValue()
            fail("inputValue should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Node is not an HTMLInputElement or HTMLTextAreaElement or HTMLSelectElement"))
        }
    }

    @Test
    fun `check correct work of innerHTML`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val locator = page.locator("#outer")
        assertEquals(page.innerHTML("#outer"), locator.innerHTML())
    }

    @Test
    fun `check correct work of innerText`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val locator = page.locator("#inner")
        assertEquals("Text, more text", locator.innerText())
        assertEquals("Text, more text", page.innerText("#inner"))
    }

    @Test
    fun `check to throw error innerText`() {
        page.setContent("<svg>text</svg>")
        try {
            page.innerText("svg")
            fail("innerText should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Not an HTMLElement"))
        }
        val locator = page.locator("svg")
        try {
            locator.innerText()
            fail("innerText should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Not an HTMLElement"))
        }
    }

    @Test
    fun `check correct work of text content`() {
        page.navigate("${httpServer.prefixWithDomain}/dom.html")
        val locator = page.locator("#inner")
        assertEquals(page.textContent("#inner"), locator.textContent())
    }

    @Test
    fun `check correct work of is visible and is hidden methods`() {
        page.setContent("<div>Hi</div><span></span>")

        val div = page.locator("div")
        assertTrue(div.isVisible())
        assertFalse(div.isHidden())
        assertTrue(page.isVisible("div"))
        assertFalse(page.isHidden("div"))

        val span = page.locator("span")
        assertFalse(span.isVisible())
        assertTrue(span.isHidden())
        assertFalse(page.isVisible("span"))
        assertTrue(page.isHidden("span"))

        assertFalse(page.isVisible("no-such-element"))
        assertTrue(page.isHidden("no-such-element"))
    }

    @Test
    fun `check correct work of is enabled and is disabled methods`() {
        val content = """<button disabled>button1</button>
            |<button>button2</button>
            |<div>div</div>
        """.trimMargin()
        page.setContent(content)

        val div = page.locator("div")
        assertTrue(div.isEnabled())
        assertFalse(div.isDisabled())
        assertTrue(page.isEnabled("div"))
        assertFalse(page.isDisabled("div"))

        val button1 = page.locator(":text('button1')")
        assertFalse(button1.isEnabled())
        assertTrue(button1.isDisabled())
        assertFalse(page.isEnabled(":text('button1')"))
        assertTrue(page.isDisabled(":text('button1')"))

        val button2 = page.locator(":text('button2')")
        assertTrue(button2.isEnabled())
        assertFalse(button2.isDisabled())
        assertTrue(page.isEnabled(":text('button2')"))
        assertFalse(page.isDisabled(":text('button2')"))
    }

    @Test
    fun `check correct work of is editable method`() {
        page.setContent("<input id=input1 disabled><textarea></textarea><input id=input2>")
        page.evalOnSelector("textarea", "t => t.readOnly = true")

        val input1 = page.locator("#input1")
        assertFalse(input1.isEditable())
        assertFalse(page.isEditable("#input1"))

        val input2 = page.locator("#input2")
        assertTrue(input2.isEditable())
        assertTrue(page.isEditable("#input2"))

        val textarea = page.locator("textarea")
        assertFalse(textarea.isEditable())
        assertFalse(page.isEditable("textarea"))
    }

    @Test
    fun `check correct work of is checked method`() {
        page.setContent("<input type='checkbox' checked><div>Not a checkbox</div>")

        val element = page.locator("input")
        assertTrue(element.isChecked())
        assertTrue(page.isChecked("input"))

        element.evaluate("input => input.checked = false")
        assertFalse(element.isChecked())
        assertFalse(page.isChecked("input"))

        try {
            page.isChecked("div")
            fail("isChecked should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Not a checkbox or radio button"))
        }
    }

    @Test
    fun `check correct work of all text contents method`() {
        page.setContent("<div>A</div><div>B</div><div>C</div>")
        assertEquals(listOf("A", "B", "C"), page.locator("div").allTextContents())
    }

    @Test
    fun `check correct work of all inner texts method`() {
        page.setContent("<div>A</div><div>B</div><div>C</div>")
        assertEquals(listOf("A", "B", "C"), page.locator("div").allInnerTexts())
    }
}