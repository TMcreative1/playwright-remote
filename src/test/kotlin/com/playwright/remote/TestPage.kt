package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.options.NewPageOptions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestPage : BaseTest() {

    private fun createPage(
        wsEndpoint: String,
        options: NewPageOptions? = NewPageOptions { it.ignoreHTTPSErrors = true }
    ) = RemoteBrowser.connectWs(wsEndpoint).newPage(options)

    companion object {

        @JvmStatic
        @BeforeAll
        fun startHttpServer() {
            httpsServer.setRoute("/test") { exchange ->
                exchange.sendResponseHeaders(200, 0)
                exchange.responseBody.writer().use { writer ->
                    writer.write("<html><head><title>SunHTTP</title></head></html>")
                }
            }
        }

    }

    @AfterEach
    fun stopPlaywrightServer() {
        stopServer()
    }

    @Test
    fun `check navigate method in chrome browser`() {
        val navigatedUrl = "https://localhost:8443/test"
        val page = createPage(launchChromeBrowserServer())
        val response = page.navigate(navigatedUrl)
        assert(response != null)
        assertEquals(navigatedUrl, response?.url())
    }

    @Test
    fun `check navigate method in firefox browser`() {
        val navigatedUrl = "https://localhost:8443/test"
        val page = createPage(launchFirefoxBrowserServer())
        val response = page.navigate(navigatedUrl)
        assert(response != null)
        assertEquals(navigatedUrl, response?.url())
    }

    @Test
    fun `check navigate method in safari browser`() {
        val navigatedUrl = "https://localhost:8443/test"
        val page = createPage(launchSafariBrowserServer())
        val response = page.navigate(navigatedUrl)
        assert(response != null)
        assertEquals(navigatedUrl, response?.url())
    }

}