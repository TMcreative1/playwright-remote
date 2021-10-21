package io.github.tmcreative1.playwright.remote.locator

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.enums.WaitForSelectorState
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestLocatorMisc : BaseTest() {

    @Test
    fun `check to check the box`() {
        page.setContent("<input id='checkbox' type='checkbox'></input>")
        val input = page.locator("input")
        input.setChecked(true)
        assertEquals(true, page.evaluate("checkbox.checked"))
        input.setChecked(false)
        assertEquals(false, page.evaluate("checkbox.checked"))
    }

    @Test
    fun `check correct work of wait for`() {
        page.setContent("<div></div>")
        val locator = page.locator("span")
        page.evalOnSelector("div", "div => setTimeout(() => div.innerHTML = '<span>target</span>', 500)")
        locator.waitFor()
        assertTrue(locator.textContent().contains("target"))
    }

    @Test
    fun `check to wait for hidden`() {
        page.setContent("<div><span>target</span></div>")
        val locator = page.locator("span")
        page.evalOnSelector("div", "div => setTimeout(() => div.innerHTML = '', 500)")
        locator.waitFor(WaitForOptions { it.state = WaitForSelectorState.HIDDEN })
    }
}