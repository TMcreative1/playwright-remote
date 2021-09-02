package io.github.tmcreative1.playwright.remote

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.enums.LoadState
import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowserContext
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.parser.IParser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestHar : BaseTest() {
    private val pageWithHarThreadLocal = ThreadLocal<PageWithHar>()

    private var pageWithHar: PageWithHar
        get() = pageWithHarThreadLocal.get()
        set(value) = pageWithHarThreadLocal.set(value)

    private class PageWithHar {
        val harFile: Path = Files.createTempFile("test", ".har")
        val context: IBrowserContext = browser.newContext(NewContextOptions {
            it.recordHarPath = harFile
            it.ignoreHTTPSErrors = true
        })
        val pg: IPage = context.newPage()

        fun log(): JsonObject {
            context.close()
            FileReader(harFile.toFile()).use {
                return IParser.fromJson(it, JsonObject::class.java)["log"].asJsonObject
            }
        }

        fun dispose() {
            context.close()
            Files.deleteIfExists(harFile)
        }
    }

    @BeforeEach
    fun createPageHar() {
        pageWithHar = PageWithHar()
    }

    @AfterEach
    fun deletePageWithHar() {
        pageWithHar.dispose()
    }

    @Test
    fun `check to have version and creator`() {
        pageWithHar.pg.navigate(httpServer.emptyPage)
        val log = pageWithHar.log()
        assertEquals("1.2", log["version"].asString)
        assertEquals("Playwright", log["creator"].asJsonObject["name"].asString)
    }

    @Test
    fun `check to have browser`() {
        pageWithHar.pg.navigate(httpServer.emptyPage)
        val log = pageWithHar.log()
        assertEquals(browser.name(), log["browser"].asJsonObject["name"].asString.lowercase())
        assertEquals(browser.version(), log["browser"].asJsonObject["version"].asString)
    }

    @Test
    fun `check to have pages`() {
        pageWithHar.pg.navigate("data:text/html,<title>Hello</title>")
        pageWithHar.pg.waitForLoadState(LoadState.DOMCONTENTLOADED)
        val log = pageWithHar.log()

        assertEquals(1, log["pages"].asJsonArray.size())
        val pageEntry = log["pages"].asJsonArray[0].asJsonObject
        assertEquals("page_0", pageEntry["id"].asString)
        assertEquals("Hello", pageEntry["title"].asString)
        assertTrue(pageEntry["pageTimings"].asJsonObject["onContentLoad"].asDouble > 0)
        assertTrue(pageEntry["pageTimings"].asJsonObject["onLoad"].asDouble > 0)
    }

    @Test
    fun `check to include request`() {
        pageWithHar.pg.navigate(httpServer.emptyPage)
        val log = pageWithHar.log()
        assertEquals(1, log["entries"].asJsonArray.size())

        val entry = log["entries"].asJsonArray[0].asJsonObject
        assertEquals("page_0", entry["pageref"].asString)
        assertEquals(httpServer.emptyPage, entry["request"].asJsonObject["url"].asString)
        assertEquals("GET", entry["request"].asJsonObject["method"].asString)
        assertEquals("HTTP/1.1", entry["request"].asJsonObject["httpVersion"].asString)
        assertTrue(entry["request"].asJsonObject["headers"].asJsonArray.size() > 1)
        var isUserAgentHeaderExist = false
        for (item in entry["request"].asJsonObject["headers"].asJsonArray) {
            if ("user-agent" == item.asJsonObject["name"].asString.lowercase()) {
                isUserAgentHeaderExist = true
                break
            }
        }
        assertTrue(isUserAgentHeaderExist)
    }

    @Test
    fun `check to include response`() {
        pageWithHar.pg.navigate(httpServer.emptyPage)
        val log = pageWithHar.log()
        val entry = log["entries"].asJsonArray[0].asJsonObject
        assertEquals(200, entry["response"].asJsonObject["status"].asInt)
        assertEquals("OK", entry["response"].asJsonObject["statusText"].asString)
        assertEquals("HTTP/1.1", entry["response"].asJsonObject["httpVersion"].asString)
        assertTrue(entry["response"].asJsonObject["headers"].asJsonArray.size() > 1)

        var isUserContentType = false
        for (item in entry["response"].asJsonObject["headers"].asJsonArray) {
            if ("content-type" == item.asJsonObject["name"].asString.lowercase()) {
                isUserContentType = true
                assertEquals("text/html", item.asJsonObject["value"].asString)
                break
            }
        }
        assertTrue(isUserContentType)
    }
}