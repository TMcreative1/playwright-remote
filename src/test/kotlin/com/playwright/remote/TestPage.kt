package com.playwright.remote

import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.options.NavigateOptions
import com.playwright.remote.engine.options.NewPageOptions
import com.playwright.remote.engine.page.api.IPage
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestPage : BaseTest() {

    private fun createPage(wsEndpoint: String, options: NewPageOptions? = null): IPage {
        return RemoteBrowser.connectWs(wsEndpoint).newPage(options)
    }

    @Test
    fun `check navigate method in chrome browser`() {
        try {
            val navigatedUrl = "https://www.youtube.com/"
            val page = createPage(launchChromeBrowserServer())
            val response = page.navigate(navigatedUrl)
            assert(response != null)
            assertEquals(navigatedUrl, response?.url())
        } finally {
            stopServer()
        }
    }

    @Test
    fun `check navigate method in firefox browser`() {
        try {
            val navigatedUrl = "https://www.youtube.com/"
            val page = createPage(launchFirefoxBrowserServer())
            val response = page.navigate(navigatedUrl)
            assert(response != null)
            assertEquals(navigatedUrl, response?.url())
        } finally {
            stopServer()
        }
    }

    @Test
    fun `check navigate method in safari browser`() {
        try {
            val navigatedUrl = "https://www.youtube.com/"
            val page = createPage(launchSafariBrowserServer())
            val response = page.navigate(navigatedUrl, NavigateOptions { timeout = 40000.0 })
            assert(response != null)
            assertEquals(navigatedUrl, response?.url())
        } finally {
            stopServer()
        }
    }
}