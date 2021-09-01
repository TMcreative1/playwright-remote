package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestCheck : BaseTest() {

    @Test
    fun `check correct work of checkbox`() {
        page.setContent("<input id='checkbox' type='checkbox'></input>")
        page.check("input")
        assertTrue(page.evaluate("() => window['checkbox'].checked") as Boolean)
    }

    @Test
    fun `should not check the checked checkbox`() {
        page.setContent("<input id='checkbox' type='checkbox' checked></input>")
        page.check("input")
        assertTrue(page.evaluate("() => window['checkbox'].checked") as Boolean)
    }

    @Test
    fun `check to uncheck the checkbox`() {
        page.setContent("<input id='checkbox' type='checkbox' checked></input>")
        page.uncheck("input")
        assertFalse(page.evaluate("() => window['checkbox'].checked") as Boolean)
    }

    @Test
    fun `should not uncheck the unchecked checkbox`() {
        page.setContent("<input id='checkbox' type='checkbox'></input>")
        page.uncheck("input")
        assertFalse(page.evaluate("() => window['checkbox'].checked") as Boolean)
    }

    @Test
    fun `check to check the checkbox by label`() {
        page.setContent("<label for='checkbox'><input id='checkbox' type='checkbox'></input></label>")
        page.check("label")
        assertTrue(page.evaluate("() => window['checkbox'].checked") as Boolean)
    }

    @Test
    fun `check to check the checkbox outside label`() {
        page.setContent("<label for='checkbox'>Text</label><div><input id='checkbox' type='checkbox'></input></div>")
        page.check("label")
        assertTrue(page.evaluate("() => window['checkbox'].checked") as Boolean)
    }

    @Test
    fun `check to check the checkbox inside complicated tags`() {
        page.setContent("<label>Text<span><input id='checkbox' type='checkbox'></input></span></label>")
        page.check("label")
        assertTrue(page.evaluate("() => window['checkbox'].checked") as Boolean)
    }

    @Test
    fun `check to check radio button`() {
        val content = """<input type='radio'>one</input>
            |<input id='two' type='radio'>two</input>
            |<input type='radio'>three</input>
        """.trimMargin()
        page.setContent(content)
        page.check("#two")
        assertTrue(page.evaluate("() => window['two'].checked") as Boolean)
    }

    @Test
    fun `check to check the checkbox by aria role`() {
        val content = """<div role='checkbox' id='checkbox'>CHECKBOX</div>
            |   <script>
            |      checkbox.addEventListener('click', () => checkbox.setAttribute('aria-checked', 'true'));
            |   </script>
        """.trimMargin()
        page.setContent(content)
        page.check("div")
        assertEquals("true", page.evaluate("() => window['checkbox'].getAttribute('aria-checked')"))
    }
}