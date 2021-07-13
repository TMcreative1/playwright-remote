package com.playwright.remote.page

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TestPageFill : BaseTest() {
    @Test
    fun `check to fill textarea`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.fill("textarea", "my value")
        val result = page.evaluate("() => window['result']")
        assertEquals("my value", result)
    }

    @Test
    fun `check to fill input`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.fill("input", "my value")
        val result = page.evaluate("() => window['result']")
        assertEquals("my value", result)
    }

    @Test
    fun `check to throw unsupported inputs exception`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        arrayListOf("button", "checkbox", "file", "image", "radio", "range", "reset", "submit").forEach {
            page.evalOnSelector("input", "(input, type) => input.setAttribute('type', type)", it)
            try {
                page.fill("input", "")
                fail("fill should throw")
            } catch (e: PlaywrightException) {
                assertTrue(e.message!!.contains("input of type \"$it\" cannot be filled"), e.message)
            }
        }
    }

    @Test
    fun `check to fill different input types`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        arrayListOf("password", "search", "tel", "text", "url").forEach {
            page.evalOnSelector("input", "(input, type) => input.setAttribute('type', type)", it)
            val expectedValue = "text $it"
            page.fill("input", expectedValue)
            val result = page.evaluate("() => window['result']")
            assertEquals(expectedValue, result)
        }
    }

    @Test
    fun `check to fill date input after clicking`() {
        page.setContent("<input type=date>")
        page.click("input")
        val expectedDate = "2021-06-23"
        page.fill("input", expectedDate)
        val result = page.evalOnSelector("input", "input => input.value")
        assertEquals(expectedDate, result)
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check to throw exception on incorrect date`() {
        page.setContent("<input type=date>")
        try {
            page.fill("input", "2021-13-23")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Malformed value"))
        }
    }

    @Test
    fun `check to fill time input`() {
        page.setContent("<input type=time>")
        val expectedTime = "14:32"
        page.fill("input", expectedTime)
        val result = page.evalOnSelector("input", "input => input.value")
        assertEquals(expectedTime, result)
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check to throw exception on incorrect time`() {
        page.setContent("<input type=time>")
        try {
            page.fill("input", "26:10")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Malformed value"))
        }
    }

    @Test
    fun `check to fill datetime local input`() {
        page.setContent("<input type=datetime-local>")
        val expectedDateTime = "2021-06-23T15:25"
        page.fill("input", expectedDateTime)
        val result = page.evalOnSelector("input", "input => input.value", expectedDateTime)
        assertEquals(expectedDateTime, result)
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check to throw exception on incorrect datetime local`() {
        page.setContent("<input type=datetime-local>")
        try {
            page.fill("input", "word")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Malformed value"))
        }
    }

    @Test
    fun `check to fill content editable`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val expectedValue = "my value"
        page.fill("div[contenteditable]", expectedValue)
        val result = page.evalOnSelector("div[contenteditable]", "div => div.textContent")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill elements with existing value and selection`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")

        page.evalOnSelector("input", "input => input.value = 'value one'")
        var expectedValue = "another value"
        page.fill("input", expectedValue)
        var result = page.evaluate("() => window['result']")
        assertEquals(expectedValue, result)

        page.evalOnSelector(
            "input", """input => {
            |   input.selectionStart = 1;
            |   input.selectionEnd = 2;
            |}
        """.trimMargin()
        )

        expectedValue = "or this value"
        page.fill("input", expectedValue)
        result = page.evaluate("() => window['result']")
        assertEquals(expectedValue, result)

        page.evalOnSelector(
            "div[contenteditable]", """div => {
            |   div.innerHTML = 'some text <span>some more text<span> and even more text';
            |   const range = document.createRange();
            |   range.selectNodeContents(div.querySelector('span'));
            |   const selection = window.getSelection();
            |   selection.removeAllRanges();
            |   selection.addRange(range);
            |}
        """.trimMargin()
        )
        expectedValue = "replace with this"
        page.fill("div[contenteditable]", expectedValue)
        result = page.evalOnSelector("div[contenteditable]", "div => div.textContent")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to throw exception when element is not an input textarea or content editable`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        try {
            page.fill("body", "")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is not an <input>"))
        }
    }

    @Test
    fun `check to fill the body`() {
        page.setContent("<body contentEditable='true'></body>")
        val expectedValue = "my value"
        page.fill("body", expectedValue)
        val result = page.evaluate("() => document.body.textContent")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill fixed position input`() {
        page.setContent("<input style='position: fixed;'/>")
        val expectedValue = "my value"
        page.fill("input", expectedValue)
        val result = page.evaluate("() => document.querySelector('input').value")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill when focus in the wrong frame`() {
        page.setContent(
            """<div contentEditable='true'></div>
            |<iframe></iframe>
        """.trimMargin()
        )

        page.focus("iframe")
        val expectedValue = "my value"
        page.fill("div", expectedValue)
        val result = page.evalOnSelector("div", "d => d.textContent")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill the input type number`() {
        page.setContent("<input id='input' type='number'></input>")
        val expectedValue = "23"
        page.fill("input", expectedValue)
        val result = page.evaluate("() => window['input'].value")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill exponent into the input type number`() {
        page.setContent("<input id='input' type='number'></input>")
        val expectedValue = "-10e5"
        page.fill("input", expectedValue)
        val result = page.evaluate("() => window['input'].value")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill input type number with empty string`() {
        page.setContent("<input id='input' type='number' value='123'></input>")
        page.fill("input", "")
        val result = page.evaluate("() => window['input'].value")
        assertEquals("", result)
    }

    @Test
    fun `check to fill text into the input type number`() {
        page.setContent("<input id='input' type='number'></input>")
        try {
            page.fill("input", "word")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Cannot type text into input[type=number]"))
        }
    }

    @Test
    fun `check to clear input`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        var expectedValue = "my value"
        page.fill("input", expectedValue)
        var result = page.evaluate("() => window['result']")
        assertEquals(expectedValue, result)

        expectedValue = ""
        page.fill("input", expectedValue)
        result = page.evaluate("() => window['result']")
        assertEquals(expectedValue, result)
    }
}