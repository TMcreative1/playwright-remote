package io.github.tmcreative1.playwright.remote.browser.context

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.HttpCredentials
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestBrowserContextCredentials : BaseTest() {

    @Test
    fun `check to fail without credentials`() {
        httpServer.setAuth("/empty.html", "user", "pass")
        browser.newContext().use {
            val pg = it.newPage()
            val response = pg.navigate(httpServer.emptyPage)
            assertNotNull(response)
            assertEquals(401, response.status())
        }
    }

    @Test
    fun `check correct work with valid credentials`() {
        httpServer.setAuth("/empty.html", "user", "pass")
        browser.newContext(NewContextOptions {
            it.httpCredentials = HttpCredentials { credentials ->
                credentials.username = "user"
                credentials.password = "pass"
            }
        }).use {
            val pg = it.newPage()
            val response = pg.navigate(httpServer.emptyPage)
            assertNotNull(response)
            assertEquals(200, response.status())
        }
    }

    @Test
    fun `check correct work with invalid credentials`() {
        httpServer.setAuth("/empty.html", "user", "pass")
        browser.newContext(NewContextOptions {
            it.httpCredentials = HttpCredentials { credentials ->
                credentials.username = "foo"
                credentials.password = "bar"
            }
        }).use {
            val pg = it.newPage()
            val response = pg.navigate(httpServer.emptyPage)
            assertNotNull(response)
            assertEquals(401, response.status())
        }
    }

    @Test
    fun `check to return resource body`() {
        httpServer.setAuth("/playground.html", "user", "pass")
        browser.newContext(NewContextOptions {
            it.httpCredentials = HttpCredentials { credentials ->
                credentials.username = "user"
                credentials.password = "pass"
            }
        }).use {
            val pg = it.newPage()
            val response = pg.navigate("${httpServer.prefixWithDomain}/playground.html")
            assertNotNull(response)
            assertEquals(200, response.status())
            assertEquals("Playground", pg.title())
            assertTrue(String(response.body()).contains("Playground"))
        }
    }
}