package com.playwright.remote.page

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.options.SelectOption
import com.playwright.remote.engine.options.element.SelectOptionOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestPageSelectOption : BaseTest() {
    @Test
    fun `check to select single option`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.selectOption("select", "blue")
        val expectedValue = arrayListOf("blue")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onChange"))
    }

    @Test
    fun `check to select option by value`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.selectOption("select", SelectOption { it.value = "blue" })
        val expectedValue = arrayListOf("blue")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onChange"))
    }

    @Test
    fun `check to select single option by label`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.selectOption("select", SelectOption { it.label = "Indigo" })
        val expectedValue = arrayListOf("indigo")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onChange"))
    }

    @Test
    fun `check to select single option by handle`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        val handle = page.querySelector("[id=whiteOption]")
        assertNotNull(handle)
        page.selectOption("select", handle)
        val expectedValue = arrayListOf("white")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onChange"))
    }

    @Test
    fun `check to select single option by index`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.selectOption("select", SelectOption { it.index = 2 })
        val expectedValue = arrayListOf("brown")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onChange"))
    }

    @Test
    fun `check to select option by multiple attributes`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.selectOption("select", SelectOption {
            it.value = "green"
            it.label = "Green"
        })
        val expectedValue = arrayListOf("green")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onChange"))
    }

    @Test
    fun `check to not select single option when some attributes do not match`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.evalOnSelector("select", "s => s.value = undefined")
        try {
            page.selectOption("select", SelectOption {
                it.value = "green"
                it.label = "Brown"
            }, SelectOptionOptions { it.timeout = 300.0 })
            fail("select option should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Timeout"))
        }
        assertEquals("", page.evaluate("() => document.querySelector('select').value"))
    }

    @Test
    fun `check to select only first option`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.selectOption("select", arrayOf("blue", "green", "red"))
        val expectedValue = arrayListOf("blue")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onChange"))
    }

    @Test
    fun `check to not throw when select causes navigation`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.evalOnSelector(
            "select",
            "select => select.addEventListener('input', () => window.location.href = '/empty.html')"
        )
        page.waitForNavigation {
            page.selectOption("select", "blue")
        }
        assertTrue(page.url().contains("empty.html"))
    }

    @Test
    fun `check to select multiple options`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.evaluate("() => window['makeMultiple']()")
        page.selectOption("select", arrayOf("blue", "green", "red"))
        val expectedValue = arrayListOf("blue", "green", "red")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onChange"))
    }

    @Test
    fun `check to select multiple options with attributes`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.evaluate("() => window['makeMultiple']()")
        page.selectOption("select", arrayOf(
            SelectOption { it.value = "blue" },
            SelectOption { it.label = "Green" },
            SelectOption { it.index = 4 }
        ))
        val expectedValue = arrayListOf("blue", "gray", "green")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onChange"))
    }

    @Test
    fun `check correct work of event bubbling`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.selectOption("select", "blue")
        val expectedValue = arrayListOf("blue")
        assertEquals(expectedValue, page.evaluate("() => window['result'].onBubblingInput"))
        assertEquals(expectedValue, page.evaluate("() => window['result'].onBubblingChange"))
    }

    @Test
    fun `check to throw error when element is not a select`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        try {
            page.selectOption("body", "")
            fail("select option should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is not a <select> element."))
        }
    }

    @Test
    fun `check to return on matched values`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        val result = page.selectOption("select", emptyArray<String>())
        assertEquals(emptyList(), result)
    }

    @Test
    fun `check to return an array of matched values`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.evaluate("() => window['makeMultiple']()")
        val result = page.selectOption("select", arrayOf("blue", "black", "magenta"))
        val expectedList = arrayListOf("blue", "black", "magenta")
        assertEquals(expectedList.sorted(), result.sorted())
    }

    @Test
    fun `check to return an array of one element when multiple is not set`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        val result = page.selectOption("select", arrayOf("42", "blue", "black", "magenta"))
        assertEquals(1, result.size)
    }

    @Test
    @Suppress("CAST_NEVER_SUCCEEDS")
    fun `check to unselect with null`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.evaluate("() => window['makeMultiple']()")
        val result = page.selectOption("select", arrayOf("blue", "black", "magenta"))
        val expectedList = arrayOf("blue", "black", "magenta")
        assertEquals(expectedList.sorted(), result.sorted())

        val elementValue: Array<IElementHandle>? = null
        page.selectOption("select", elementValue)
        assertEquals(
            true,
            page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)")
        )

        val stringValue: Array<String>? = null
        page.selectOption("select", stringValue)
        assertEquals(
            true,
            page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)")
        )

        val optionValue: Array<SelectOption>? = null
        page.selectOption("select", optionValue)
        assertEquals(
            true,
            page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)")
        )
    }

    @Test
    fun `check to deselect all options when passed no values for a multiple select`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.evaluate("() => window['makeMultiple']()")
        page.selectOption("select", arrayOf("blue", "black", "magenta"))
        page.selectOption("select", arrayOf<String>())
        assertEquals(
            true,
            page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)")
        )
    }

    @Test
    fun `check to deselect all options when passed no values for a select without multiple`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.selectOption("select", arrayOf("blue", "black", "magenta"))
        page.selectOption("select", arrayOf<String>())
        assertEquals(
            true,
            page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)")
        )
    }

    @Test
    fun `check correct work when redefining top level event class`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        page.evaluate("() => window.Event = null")
        page.selectOption("select", "blue")
        val expectedResult = arrayListOf("blue")
        assertEquals(expectedResult, page.evaluate("() => window['result'].onInput"))
        assertEquals(expectedResult, page.evaluate("() => window['result'].onChange"))
    }
}