package com.playwright.remote.elementhandle

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.enums.ElementState.*
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.core.exceptions.TimeoutException
import com.playwright.remote.engine.options.element.WaitForElementStateOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestElementHandleWaitForElementState : BaseTest() {

    @Test
    fun `check to wait for visible`() {
        page.setContent("<div style='display:none'>content</div>")
        val div = page.querySelector("div")
        assertNotNull(div)
        div.waitForElementState(HIDDEN)
        div.evaluate("div => div.style.display = 'block'")
        div.waitForElementState(VISIBLE)
    }

    @Test
    fun `check to wait for already visible element`() {
        page.setContent("<div>content</div>")
        val div = page.querySelector("div")
        assertNotNull(div)
        div.waitForElementState(VISIBLE)
    }

    @Test
    fun `check correct work with timeout`() {
        page.setContent("<div style='display:none'>content</div>")
        val div = page.querySelector("div")
        try {
            assertNotNull(div)
            div.waitForElementState(VISIBLE, WaitForElementStateOptions { it.timeout = 1000.0 })
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 1000ms exceeded"))
        }
    }

    @Test
    fun `check to throw waiting for visible element when it detached`() {
        page.setContent("<div style='display:none'>content</div>")
        val div = page.querySelector("div")
        assertNotNull(div)
        div.evaluate("div => div.remove()")
        try {
            div.waitForElementState(VISIBLE)
            fail("waitForElementState should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is not attached to the DOM"))
        }
    }

    @Test
    fun `check to wait for hidden element`() {
        page.setContent("<div>content</div>")
        val div = page.querySelector("div")
        assertNotNull(div)
        div.waitForElementState(VISIBLE)
        div.evaluate("div => div.style.display = 'none'")
        div.waitForElementState(HIDDEN)
    }

    @Test
    fun `check to wait for already hidden element`() {
        page.setContent("<div></div>")
        val div = page.querySelector("div")
        assertNotNull(div)
        div.waitForElementState(HIDDEN)
    }

    @Test
    fun `check to wait for hidden element when it detached`() {
        page.setContent("<div>content</div>")
        val div = page.querySelector("div")
        assertNotNull(div)
        div.waitForElementState(VISIBLE)
        div.evaluate("div => div.remove()")
        div.waitForElementState(HIDDEN)
    }

    @Test
    fun `check to wait for enabled button`() {
        page.setContent("<button disabled><span>Target</span></button>")
        val span = page.querySelector("text=Target")
        assertNotNull(span)
        span.evaluate("span => span.parentElement.disabled = false")
        span.waitForElementState(ENABLED)
    }

    @Test
    fun `check to throw waiting for enabled element when it detached`() {
        page.setContent("<button disabled>Target</button>")
        val button = page.querySelector("button")
        assertNotNull(button)
        button.evaluate("button => button.remove()")
        try {
            button.waitForElementState(ENABLED)
            fail("waitForElementState should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is not attached to the DOM"))
        }
    }

    @Test
    fun `check to wait for disabled button`() {
        page.setContent("<button><span>Target</span></button>")
        val span = page.querySelector("text=Target")
        assertNotNull(span)
        span.evaluate("span => span.parentElement.disabled = true")
        span.waitForElementState(DISABLED)
    }

    @Test
    fun `check to wait for stable position`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val button = page.querySelector("button")
        assertNotNull(button)
        val jsScript = """button => {
            |   button.style.transition = 'margin 10000ms linear 0s';
            |   button.style.marginLeft = '20000px';
            |}
        """.trimMargin()
        page.evalOnSelector("button", jsScript)
        button.evaluate("button => button.style.transition = ''")
        button.waitForElementState(STABLE)
    }
}