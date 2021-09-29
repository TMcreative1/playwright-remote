package io.github.tmcreative1.playwright.remote.browser.context

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.TextContentOptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TestBrowserContextStrict : BaseTest() {

    @BeforeEach
    fun createContext() {
        browserContext = browser.newContext(NewContextOptions { it.strictSelectors = true })
        page = browserContext.newPage()
    }

    @Test
    fun `check to not fail page text content in non strict mode`() {
        browser.newContext().use {
            val pg = it.newPage()
            pg.setContent("<span>span1</span><div><span>target</span></div>")
            assertEquals("span1", pg.textContent("span"))
        }
    }

    @Test
    fun `check to fail page text content in strict mode`() {
        page.setContent("<span>span1</span><div><span>target</span></div>")
        try {
            page.textContent("span")
            fail("textContent should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("strict mode violation"))
        }
    }

    @Test
    fun `check correct work`() {
        page.setContent("<span>span1</span><div><span>target</span></div>")
        assertEquals("span1", page.textContent("span", TextContentOptions { it.strict = false }))
    }
}