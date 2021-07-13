package com.playwright.remote.page

import com.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestPageDialog : BaseTest() {

    @Test
    fun `check dialog with type and value`() {
        page.onDialog {
            assertEquals("alert", it.type())
            assertEquals("", it.defaultValue())
            assertEquals("test", it.message())
            it.accept()
        }
        page.evaluate("alert('test')")
    }

    @Test
    fun `check dialog allow accepting prompts`() {
        page.onDialog {
            assertEquals("prompt", it.type())
            assertEquals("yes", it.defaultValue())
            assertEquals("question?", it.message())
            it.accept("answer")
        }
        val result = page.evaluate("prompt('question?', 'yes')")
        assertEquals("answer", result)
    }

    @Test
    fun `check dialog dismiss the prompt`() {
        page.onDialog {
            it.dismiss()
        }
        val result = page.evaluate("prompt('question?')")
        assertNull(result)
    }

    @Test
    fun `check dialog accept the confirm prompt`() {
        page.onDialog {
            it.accept()
        }
        val result = page.evaluate("confirm('boolean?')")
        assertEquals(true, result)
    }

    @Test
    fun `check dialog dismiss the confirm prompt`() {
        page.onDialog {
            it.dismiss()
        }
        val result = page.evaluate("() => confirm('boolean?')")
        assertEquals(false, result)
    }

    @Test
    fun `check closing with open alert`() {
        val didShowDialog = arrayListOf(false)
        page.onDialog {
            didShowDialog[0] = true
        }
        page.evaluate("() => setTimeout(() => alert('hello'), 0)")
        while (!didShowDialog[0]) {
            page.waitForTimeout(100.0)
        }
    }

    @Test
    fun `check handling of multiple alerts`() {
        page.onDialog {
            it.accept()
        }
        page.setContent(
            """
        <p>Test</p>
        <script>
            alert('Please dismiss this dialog');
            alert('Please dismiss this dialog');
            alert('Please dismiss this dialog');
        </script>
        """.trimIndent()
        )
        assertEquals("Test", page.textContent("p"))
    }

    @Test
    fun `check handling of multiple confirms`() {
        page.onDialog {
            it.accept()
        }
        page.setContent(
            """
        <p>Test</p>
        <script>
            confirm('Please confirm me');
            confirm('Please confirm me');
            confirm('Please confirm me');
        </script>
        """.trimIndent()
        )
        assertEquals("Test", page.textContent("p"))
    }

    @Test
    fun `check to dismiss the prompt without listeners`() {
        val result = page.evaluate("() => prompt('question?')")
        assertNull(result)
    }

    @Test
    fun `check to dismiss the alert without listeners`() {
        page.setContent("<div onclick='window.alert(123); window._clicked=true'>Click me</div>")
        page.click("div")
        assertEquals(true, page.evaluate("window._clicked"))
    }
}