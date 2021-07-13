package com.playwright.remote.page

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.Cookie
import com.playwright.remote.engine.options.FulfillOptions
import com.playwright.remote.engine.options.ResumeOptions
import com.playwright.remote.engine.route.api.IRoute
import com.playwright.remote.engine.route.request.api.IRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import java.io.OutputStreamWriter
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap
import kotlin.test.*

class TestPageRoute : BaseTest() {

    @Test
    fun `check to intercept`() {
        val intercept = arrayListOf(false)
        page.route("**/empty.html") {
            val request = it.request()
            assertTrue(request.url().contains("empty.html"))
            assertNotNull(request.headers()["user-agent"])
            assertEquals("GET", request.method())
            assertNull(request.postData())
            assertTrue(request.isNavigationRequest())
            assertEquals("document", request.resourceType())
            assertEquals(request.frame(), page.mainFrame())
            assertEquals("about:blank", request.frame().url())
            it.resume()
            intercept[0] = true
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertTrue(response.ok())
        assertTrue(intercept[0])
    }

    @Test
    fun `check to unroute`() {
        val intercepted = arrayListOf<Int>()
        val handler1: (IRoute) -> Unit = {
            intercepted.add(1)
            it.resume()
        }
        page.route("**/empty.html", handler1)
        page.route("**/empty.html") {
            intercepted.add(2)
            it.resume()
        }
        page.route("**/empty.html") {
            intercepted.add(3)
            it.resume()
        }
        page.route("**/*") {
            intercepted.add(4)
            it.resume()
        }
        page.navigate(httpServer.emptyPage)
        assertEquals(arrayListOf(1), intercepted)

        intercepted.clear()
        page.unroute("**/empty.html", handler1)
        page.navigate(httpServer.emptyPage)
        assertEquals(arrayListOf(2), intercepted)

        intercepted.clear()
        page.unroute("**/empty.html")
        page.navigate(httpServer.emptyPage)
        assertEquals(arrayListOf(4), intercepted)
    }

    @Test
    fun `check to correct work when post is redirected with 302`() {
        httpServer.setRedirect("/rredirect", "/empty.html")
        page.navigate(httpServer.emptyPage)
        page.route("**/*") { it.resume() }
        val content = """<form action='/rredirect' method='post'>
            |   <input type='hidden' id='foo' name='foo' value='FOOBAR'>
            |</form>
        """.trimMargin()
        page.setContent(content)
        page.waitForNavigation { page.evalOnSelector("form", "form => form.submit()") }
    }

    @Test
    fun `check correct work when header manipulation headers with redirect`() {
        httpServer.setRedirect("/rrredict", "/empty.html")
        page.route("**/*") {
            val headers = it.request().headers() as HashMap
            headers["age"] = "23"
            it.resume(ResumeOptions { opt -> opt.headers = headers })
        }
        page.navigate("${httpServer.prefixWithDomain}/rrredirect")
    }

    @Test
    fun `check to remove headers`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/*") {
            val headers = it.request().headers() as HashMap
            headers.remove("age")
            it.resume(ResumeOptions { opt -> opt.headers = headers })
        }

        val serverRequest = httpServer.futureRequest("/title.html")
        page.evaluate("url => fetch(url, { headers: { age: '23'} })", "${httpServer.prefixWithDomain}/title.html")
        assertFalse(serverRequest.get().headers.containsKey("age"))
    }

    @Test
    fun `check to contain referer header`() {
        val requests = arrayListOf<IRequest>()
        page.route("**/*") {
            requests.add(it.request())
            it.resume()
        }
        page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        assertTrue(requests[1].url().contains("/one-style.css"))
        assertTrue(requests[1].headers().containsKey("referer"))
        assertTrue(requests[1].headers()["referer"]!!.contains("/page-with-one-style.html"))
    }

    @Test
    fun `check to return navigation reponse when url has cookies`() {
        page.navigate(httpServer.emptyPage)
        browserContext.addCookies(Collections.singletonList(Cookie {
            it.name = "age"
            it.value = "23"
            it.url = httpServer.emptyPage
        }))
        page.route("**/*") { it.resume() }
        val response = page.reload()
        assertNotNull(response)
        assertEquals(200, response.status())
    }

    @Test
    fun `check to show custom http headers`() {
        page.setExtraHTTPHeaders(mapOf("age" to "23"))
        page.route("**/*") {
            assertEquals("23", it.request().headers()["age"])
            it.resume()
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertTrue(response.ok())
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check correct work route with redirect inside sync xhr`() {
        page.navigate(httpServer.emptyPage)
        httpServer.setRedirect("/logo.png", "/playwright.png")
        page.route("**/*") { it.resume() }
        val jsScript = """async () => {
            |   const request = new XMLHttpRequest();
            |   request.open('GET', '/logo.png', false);
            |   request.send(null);
            |   return request.status;
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(200, result)
    }

    @Test
    fun `check correct work route with custom referer headers`() {
        page.setExtraHTTPHeaders(mapOf("referer" to httpServer.emptyPage))
        page.route("**/*") {
            assertEquals(httpServer.emptyPage, it.request().headers()["referer"])
            it.resume()
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertTrue(response.ok())
    }

    @Test
    fun `check to be aborted`() {
        page.route(Pattern.compile(".*\\.css$")) { it.abort() }
        val failed = arrayListOf(false)
        page.onRequestFailed {
            if (it.url().contains(".css"))
                failed[0] = true
        }
        val response = page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        assertNotNull(response)
        assertTrue(response.ok())
        assertTrue(response.request().failure().isEmpty())
        assertTrue(failed[0])
    }

    @Test
    fun `check to abort with custom error codes`() {
        page.route("**/*") {
            it.abort("internetdisconnected")
        }
        val failedRequest = arrayListOf<IRequest?>(null)
        page.onRequestFailed {
            failedRequest[0] = it
        }

        try {
            page.navigate(httpServer.emptyPage)
            fail("navigate should throw")
        } catch (e: PlaywrightException) {
            assertNotNull(failedRequest[0])

            when {
                isWebkit() -> assertEquals("Request intercepted", failedRequest[0]!!.failure())
                isFirefox() -> assertEquals("NS_ERROR_OFFLINE", failedRequest[0]!!.failure())
                else -> assertEquals("net::ERR_INTERNET_DISCONNECTED", failedRequest[0]!!.failure())
            }
        }
    }

    @Test
    fun `check to send referer`() {
        page.setExtraHTTPHeaders(mapOf("referer" to "http://google.com"))
        page.route("**/*") {
            it.resume()
        }
        val request = httpServer.futureRequest("/grid.html")
        page.navigate("${httpServer.prefixWithDomain}/grid.html")
        assertEquals(Collections.singletonList("http://google.com"), request.get().headers["referer"])
    }

    @Test
    fun `check fail navigation when aborting the main resource`() {
        page.route("**/*") {
            it.abort()
        }
        try {
            page.navigate(httpServer.emptyPage)
            fail("navigate should throw")
        } catch (e: PlaywrightException) {
            when {
                isWebkit() -> assertTrue(e.message!!.contains("Request intercepted"))
                isFirefox() -> assertTrue(e.message!!.contains("NS_ERROR_FAILURE"))
                else -> assertTrue(e.message!!.contains("net::ERR_FAILED"))
            }
        }
    }

    @Test
    fun `check correct work with redirects`() {
        val intercepted = arrayListOf<IRequest>()
        page.route("**/*") {
            it.resume()
            intercepted.add(it.request())
        }

        httpServer.setRedirect("/not-found-page.html", "/not-found-page-2.html")
        httpServer.setRedirect("/not-found-page-2.html", "/not-found-page-3.html")
        httpServer.setRedirect("/not-found-page-3.html", "/not-found-page-4.html")
        httpServer.setRedirect("/not-found-page-4.html", "/empty.html")

        val response = page.navigate("${httpServer.prefixWithDomain}/not-found-page.html")
        assertNotNull(response)
        assertEquals(200, response.status())
        assertTrue(response.url().contains("empty.html"))

        assertEquals(1, intercepted.size)
        assertEquals("document", intercepted[0].resourceType())
        assertTrue(intercepted[0].isNavigationRequest())
        assertTrue(intercepted[0].url().contains("/not-found-page.html"))

        val chain = arrayListOf<IRequest>()
        var r: IRequest? = response.request()
        while (r != null) {
            chain.add(r)
            assertTrue(r.isNavigationRequest())
            r = r.redirectedFrom()
        }
        assertEquals(5, chain.size)
        var indx = 0
        for (url: String in arrayListOf(
            "/empty.html",
            "/not-found-page-4.html",
            "/not-found-page-3.html",
            "/not-found-page-2.html",
            "/not-found-page.html"
        )) {
            assertTrue(chain[indx++].url().contains(url))
        }
        chain.forEachIndexed { index, _ ->
            assertEquals(if (index != 0) chain[index - 1] else null, chain[index].redirectedTo())
        }
    }

    @Test
    fun `check correct work with redirects for sub-resources`() {
        val intercepted = arrayListOf<IRequest>()
        page.route("**/*") {
            it.resume()
            intercepted.add(it.request())
        }

        httpServer.setRedirect("/one-style.css", "/two-style.css")
        httpServer.setRedirect("/two-style.css", "/three-style.css")
        httpServer.setRedirect("/three-style.css", "/four-style.css")
        httpServer.setRoute("/four-style.css") {
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { writer ->
                writer.write("body { box-sizing: border-box; }")
            }
        }

        val response = page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        assertNotNull(response)
        assertEquals(200, response.status())
        assertTrue(response.url().contains("page-with-one-style.html"))

        assertEquals(2, intercepted.size)
        assertEquals("document", intercepted[0].resourceType())
        assertTrue(intercepted[0].url().contains("page-with-one-style.html"))

        var r: IRequest? = intercepted[1]
        for (url: String in arrayListOf("/one-style.css", "/two-style.css", "/three-style.css", "/four-style.css")) {
            assertNotNull(r)
            assertEquals("stylesheet", r.resourceType())
            assertTrue(r.url().contains(url))
            r = r.redirectedTo()
        }
        assertNull(r)
    }

    @Test
    fun `check correct work with equal requests`() {
        page.navigate(httpServer.emptyPage)
        var responseCount = 1
        httpServer.setRoute("/zzz") {
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { writer ->
                writer.write((responseCount++ * 11).toString())
            }
        }

        val spinner = arrayListOf(false)
        page.route("**/*") {
            if (spinner[0]) {
                it.abort()
            } else {
                it.resume()
            }
            spinner[0] = !spinner[0]
        }
        val results = arrayListOf<String>()
        for (index: Int in 0..2) {
            results.add(page.evaluate("() => fetch('/zzz').then(response => response.text()).catch(e => 'FAILED')") as String)
        }
        assertEquals(arrayListOf("11", "FAILED", "22"), results)
    }

    @Test
    fun `check to navigate with data URL and not fire data URL requests`() {
        val requests = arrayListOf<IRequest>()
        page.route("**/*") {
            requests.add(it.request())
            it.resume()
        }
        val dataUrl = "data:text/html,<div>some text</div>"
        val response = page.navigate(dataUrl)
        assertNull(response)
        assertEquals(0, requests.size)
    }

    @Test
    fun `check to fetch data url and not fire data url requests`() {
        page.navigate(httpServer.emptyPage)
        val requests = arrayListOf<IRequest>()
        page.route("**/*") {
            requests.add(it.request())
            it.resume()
        }

        val dataUrl = "data:text/html,<div>some text</div>"
        val result = page.evaluate("url => fetch(url).then(r => r.text())", dataUrl)
        assertEquals("<div>some text</div>", result)
        assertEquals(0, requests.size)
    }

    @Test
    fun `check to navigate to url with hash and fire requests without hash`() {
        val requests = arrayListOf<IRequest>()
        page.route("**/*") {
            requests.add(it.request())
            it.resume()
        }
        val response = page.navigate("${httpServer.emptyPage}#hash")
        assertNotNull(response)
        assertEquals(200, response.status())
        assertEquals(httpServer.emptyPage, response.url())
        assertEquals(1, requests.size)
        assertEquals(httpServer.emptyPage, requests[0].url())
    }

    @Test
    fun `check correct work with encoded server`() {
        page.route("**/*") {
            it.resume()
        }
        val response = page.navigate("${httpServer.prefixWithDomain}/nonexisting page")
        assertNotNull(response)
        assertEquals(404, response.status())
    }

    @Test
    fun `check correct work with badly encoded server`() {
        httpServer.setRoute("/malformed") {
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        page.route("**/*") {
            it.resume()
        }
        val response = page.navigate("${httpServer.prefixWithDomain}/malformed?rnd=%911")
        assertNotNull(response)
        assertEquals(200, response.status())
    }

    @Test
    fun `check correct work encoded server with bad request`() {
        val requests = arrayListOf<IRequest>()
        page.route("**/*") {
            it.resume()
            requests.add(it.request())
        }
        val response =
            page.navigate("data:text/html,<link rel='stylesheet' href='${httpServer.prefixWithDomain}/fonts?helvetica|arial'/>")
        assertNull(response)
        assertEquals(1, requests.size)
        assertEquals(400, requests[0].response()!!.status())
    }

    @Test
    fun `check to intercept main resource during cross process navigation`() {
        page.navigate(httpServer.emptyPage)
        val intercepted = arrayListOf(false)
        page.route("${httpServer.prefixWithIP}/empty.html") {
            intercepted[0] = true
            it.resume()
        }
        val response = page.navigate("${httpServer.prefixWithIP}/empty.html")
        assertNotNull(response)
        assertTrue(response.ok())
        assertTrue(intercepted[0])
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check to fulfill with redirect status`() {
        page.navigate("${httpServer.prefixWithDomain}/title.html")
        httpServer.setRoute("/final") {
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { writer ->
                writer.write("text")
            }
        }
        page.route("**/*") { route ->
            if (route.request().url() != "${httpServer.prefixWithDomain}/redirect_this") {
                route.resume()
                return@route
            }
            route.fulfill(FulfillOptions {
                it.status = 301
                it.headers = mapOf("location" to "/blank.html")
            })
        }
        val jsScript = """async url => {
            |   const data = await fetch(url);
            |   return data.text();
            |}
        """.trimMargin()
        val text = page.evaluate(jsScript, "${httpServer.prefixWithDomain}/redirect_this")
        assertEquals("", text)
    }

    @Test
    fun `check to support cors with GET`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/cars*") { route ->
            val headers = hashMapOf<String, String>()
            if (route.request().url().endsWith("allow")) {
                headers["access-control-allow-origin"] = "*"
            }
            route.fulfill(FulfillOptions {
                it.status = 200
                it.contentType = "application/json"
                it.headers = headers
                it.body = "[\"electric\",\"gas\"]"
            })
        }
        var jsScript = """async () => {
            |   const response = await fetch('https://example.com/cars?allow', { mode: 'cors' });
            |   return response.json();
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(arrayListOf("electric", "gas"), result)

        try {
            jsScript = """async () => {
                |   const response = await fetch('https://example.com/cars?reject', { mode: 'cors' });
                |   return response.json();
                |}
            """.trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("failed"))
        }
    }

    @Test
    fun `check to support cors with POST`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/cars") { route ->
            route.fulfill(FulfillOptions {
                it.status = 200
                it.contentType = "application/json"
                it.headers = mapOf("Access-Control-Allow-Origin" to "*")
                it.body = "[\"electric\",\"gas\"]"
            })
        }
        val jsScript = """async () => {
            |   const response = await fetch('https://example.com/cars', {
            |       method: 'POST',
            |       headers: { 'Content-Type': 'application/json' },
            |       mode: 'cors',
            |       body: JSON.stringify({ 'number': 1 })
            |  });
            |  return response.json();
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(arrayListOf("electric", "gas"), result)
    }

    @Test
    fun `check to support cors with credentials`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/cars") { route ->
            route.fulfill(FulfillOptions {
                it.status = 200
                it.contentType = "application/json"
                it.headers = mapOf(
                    "Access-Control-Allow-Origin" to httpServer.prefixWithDomain,
                    "Access-Control-Allow-Credentials" to "true"
                )
                it.body = "[\"electric\",\"gas\"]"
            })
        }
        val jsScript = """async () => {
            |   const response = await fetch('https://example.com/cars', {
            |       method: 'POST',
            |       headers: { 'Content-Type': 'application/json' },
            |       mode: 'cors',
            |       body: JSON.stringify({ 'number': 1 }),
            |       credentials: 'include'
            |  });
            |  return response.json();
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(arrayListOf("electric", "gas"), result)
    }

    @Test
    fun `check to reject cors with disallowed credentials`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/cars") { route ->
            route.fulfill(FulfillOptions {
                it.status = 200
                it.contentType = "application/json"
                it.headers = mapOf("Access-Control-Allow-Origin" to httpServer.prefixWithDomain)
                it.body = "[\"electric\",\"gas\"]"
            })
        }
        try {
            val jsScript = """async () => {
                |   const response = await fetch('https://example.com/cars', {
                |       method: 'POST',
                |       header: { 'Content-Type': 'application/json' },
                |       mode: 'cors',
                |       body: JSON.stringify({ 'number': 1 }),
                |       credentials: 'include'
                |   });
                |   return response.json();
                |}
            """.trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            when {
                isWebkit() -> assertTrue(e.message!!.contains("Credentials flag is true, but Access-Control-Allow-Credentials is not \"true\""))
                isChromium() -> assertTrue(e.message!!.contains("Failed to fetch"))
                isFirefox() -> assertTrue(e.message!!.contains("NetworkError when attempting to fetch resource."))
            }
        }
    }

    @Test
    fun `check to support cors for different methods`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/cars") { route ->
            route.fulfill(FulfillOptions {
                it.status = 200
                it.contentType = "application/json"
                it.headers = mapOf("Access-Control-Allow-Origin" to "*")
                it.body = "[\"${route.request().method()}\",\"electric\",\"gas\"]"
            })
        }
        val jsScript = """async () => {
            |   const response = await fetch('https://example.com/cars', {
            |       method: 'POST',
            |       headers: { 'Content-Type': 'application/json' },
            |       mode: 'cors',
            |       body: JSON.stringify({ 'number': 1 })
            |   });
            |   return response.json();
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(arrayListOf("POST", "electric", "gas"), result)
    }
}