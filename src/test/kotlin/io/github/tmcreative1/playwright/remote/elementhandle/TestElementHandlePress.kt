package io.github.tmcreative1.playwright.remote.elementhandle

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestElementHandlePress : BaseTest() {

    @Test
    fun `check correct work`() {
        page.setContent("<input type='text' />")
        page.press("input", "h")
        assertEquals("h", page.evalOnSelector("input", "input => input.value"))
    }

    @Test
    fun `check to not select existing value`() {
        page.setContent("<input type='text' value='hello' />")
        page.press("input", "w")
        assertEquals("whello", page.evalOnSelector("input", "input => input.value"))
    }

    @Test
    fun `check to reset selection when not focused`() {
        page.setContent("<input type='text' value='hello' /><div tabIndex=2>text</div>")
        val jsScript = """input => {
            |   input.selectionStart = 2;
            |   input.selectionEnd = 4;
            |   document.querySelector('div').focus();
            |}
        """.trimMargin()
        page.evalOnSelector("input", jsScript)
        page.press("input", "w")
        assertEquals("whello", page.evalOnSelector("input", "input => input.value"))
    }

    @Test
    fun `check to not modify selection when focused`() {
        page.setContent("<input type='text' value='hello' />")
        val jsScript = """input => {
            |   input.focus();
            |   input.selectionStart = 2;
            |   input.selectionEnd = 4;
            |}
        """.trimMargin()
        page.evalOnSelector("input", jsScript)
        page.press("input", "w")
        assertEquals("hewo", page.evalOnSelector("input", "input => input.value"))
    }

    @Test
    fun `check correct work with number input`() {
        page.setContent("<input type='number' value=2 />")
        page.press("input", "1")
        assertEquals("12", page.evalOnSelector("input", "input => input.value"))
    }
}