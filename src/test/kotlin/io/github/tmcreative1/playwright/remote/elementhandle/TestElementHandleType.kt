package io.github.tmcreative1.playwright.remote.elementhandle

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestElementHandleType : BaseTest() {

    @Test
    fun `check correct work`() {
        page.setContent("<input type='text' />")
        page.type("input", "hello")
        assertEquals("hello", page.evalOnSelector("input", "input => input.value"))
    }

    @Test
    fun `check to not select existing value`() {
        page.setContent("<input type='text' value='World!' />")
        page.type("input", "Hello, ")
        assertEquals("Hello, World!", page.evalOnSelector("input", "input => input.value"))
    }

    @Test
    fun `check to reset selection when not focus`() {
        page.setContent("<input type='text' value='World!' /><div tabIndex=2>text</div>")
        val jsScript = """input => {
            |   input.selectionStart = 2;
            |   input.selectionEnd = 4;
            |   document.querySelector('div').focus();
            |}
        """.trimMargin()
        page.evalOnSelector("input", jsScript)
        page.type("input", "Hello, ")
        assertEquals("Hello, World!", page.evalOnSelector("input", "input => input.value"))
    }

    @Test
    fun `check to not modify selection when focus`() {
        page.setContent("<input type='text' value='Hello,' />")
        val jsScript = """input => {
            |   input.focus();
            |   input.selectionStart = 2;
            |   input.selectionEnd = 4;
            |}
        """.trimMargin()
        page.evalOnSelector("input", jsScript)
        page.type("input", " World!")
        assertEquals("He World!o,", page.evalOnSelector("input", "input => input.value"))
    }

    @Test
    fun `check correct work with number`() {
        page.setContent("<input type='number' value=2 />")
        page.type("input", "23")
        assertEquals("232", page.evalOnSelector("input", "input => input.value"))
    }
}