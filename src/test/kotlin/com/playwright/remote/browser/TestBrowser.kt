package com.playwright.remote.browser

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TestBrowser : BaseTest() {

    @Test
    fun `check to create new page`() {
        browserContext.close()
        val pg1 = browser.newPage()
        assertEquals(1, browser.contexts().size)

        val pg2 = browser.newPage()
        assertEquals(2, browser.contexts().size)

        pg1.close()
        assertEquals(1, browser.contexts().size)

        pg2.close()
        assertEquals(0, browser.contexts().size)
    }

    @Test
    fun `check to throw upon second create new page`() {
        val pg = browser.newPage()
        try {
            pg.context().newPage()
            fail("newPage should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Please use browser.newContext()"))
        }
    }
}