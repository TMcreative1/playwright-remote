package com.playwright.remote.browser.context

import com.google.gson.JsonObject
import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.options.FulfillOptions
import com.playwright.remote.engine.options.NewContextOptions
import com.playwright.remote.engine.options.StorageStateOptions
import com.playwright.remote.engine.parser.IParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.test.assertEquals

class TestBrowserContextStorageState : BaseTest() {

    @Test
    fun `check to capture local storage`() {
        page.route("**/*") { route ->
            route.fulfill(FulfillOptions { opt ->
                opt.body = "<html></html>"
            })
        }
        page.navigate("https://www.example.com")
        page.evaluate("localStorage['name1'] = 'value1';")
        page.navigate("https://www.domain.com")
        page.evaluate("localStorage['name2'] = 'value2';")
        val storageState = browserContext.storageState()
        val expectedState = """{
              "cookies": [],
              "origins": [
                {
                  "origin": "https://www.example.com",
                  "localStorage": [
                    {
                      "name": "name1",
                      "value": "value1"
                    }
                  ]
                },
                {
                  "origin": "https://www.domain.com",
                  "localStorage": [
                    {
                      "name": "name2",
                      "value": "value2"
                    }
                  ]
                }
              ]
        }"""
        assertJsonEquals(expectedState, IParser.fromJson(storageState, JsonObject::class.java))
    }

    @Test
    fun `check to set local storage`() {
        val storageState = """{
              origins: [
                {
                  origin: 'https://www.example.com',
                  localStorage: [{
                    name: 'name1',
                    value: 'value1'
                  }]
                }
              ]
        }"""
        browser.newContext(NewContextOptions { it.storageState = storageState }).use {
            val pg = it.newPage()
            pg.route("**/*") { route ->
                route.fulfill(FulfillOptions { opt ->
                    opt.body = "<html></html>"
                })
            }
            pg.navigate("https://www.example.com")
            val localStorage = pg.evaluate("window.localStorage")
            assertEquals(mapOf("name1" to "value1"), localStorage)
        }
    }

    @Test
    fun `check to round trip through the file`(@TempDir tempDir: Path) {
        val pg1 = browserContext.newPage()
        pg1.route("**/*") {
            it.fulfill(FulfillOptions { opt ->
                opt.body = "<html></html>"
            })
        }
        pg1.navigate("https://www.example.com")
        val jsScript = """() => {
            |  localStorage['name1'] = 'value1'; 
            |  document.cookie = 'username=John Doe';
            |  return document.cookie;
            |}
        """.trimMargin()
        pg1.evaluate(jsScript)
        val path = tempDir.resolve("storage-state.json")
        browserContext.storageState(StorageStateOptions { it.path = path })
        val expectedState = """{
              'cookies':[
                { 
                  'name':'username',
                  'value':'John Doe',
                  'domain':'www.example.com',
                  'path':'/',
                  'expires':-1,
                  'httpOnly':false,
                  'secure':false,
                  'sameSite':'None'
                }],
              'origins':[
                {
                  'origin':'https://www.example.com',
                  'localStorage':[
                    {
                      'name':'name1',
                      'value':'value1'
                    }]
                }]
        }"""
        InputStreamReader(FileInputStream(path.toFile()), StandardCharsets.UTF_8).use {
            assertJsonEquals(expectedState, IParser.fromJson(it, JsonObject::class.java))
        }
        browser.newContext(NewContextOptions { it.storageStatePath = path }).use {
            val pg2 = it.newPage()
            pg2.route("**/*") { route ->
                route.fulfill(FulfillOptions { opt ->
                    opt.body = "<html></html>"
                })
            }
            pg2.navigate("https://www.example.com")
            val localStorage = pg2.evaluate("window.localStorage")
            assertEquals(mapOf("name1" to "value1"), localStorage)
            val cookie = pg2.evaluate("document.cookie")
            assertEquals("username=John Doe", cookie)
        }
    }
}