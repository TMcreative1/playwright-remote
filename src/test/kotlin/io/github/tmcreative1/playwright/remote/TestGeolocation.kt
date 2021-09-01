package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.Geolocation
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestGeolocation : BaseTest() {

    private val jsScriptForCurrentPosition =
        """() => new Promise(resolve => navigator.geolocation.getCurrentPosition(position => {
            |   resolve({latitude: position.coords.latitude, longitude: position.coords.longitude});
            |}))
        """.trimMargin()

    @Test
    fun `check correct work`() {
        browserContext.grantPermissions(arrayListOf("geolocation"))
        page.navigate(httpServer.emptyPage)
        browserContext.setGeolocation(Geolocation {
            it.latitude = 10.0
            it.longitude = 10.0
        })

        val geolocation = page.evaluate(jsScriptForCurrentPosition)
        assertEquals(mapOf("latitude" to 10, "longitude" to 10), geolocation)
    }

    @Test
    fun `check to throw when invalid longitude`() {
        try {
            browserContext.setGeolocation(Geolocation {
                it.latitude = 10.0
                it.longitude = 200.0
            })
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("geolocation.longitude: precondition -180 <= LONGITUDE <= 180 failed."))
        }
    }

    @Test
    fun `check to isolate contexts`() {
        browserContext.grantPermissions(arrayListOf("geolocation"))
        browserContext.setGeolocation(Geolocation {
            it.latitude = 10.0
            it.longitude = 10.0
        })
        page.navigate(httpServer.emptyPage)

        val context = browser.newContext(NewContextOptions {
            it.permissions = arrayListOf("geolocation")
            it.geolocation = Geolocation { geo ->
                geo.latitude = 20.0
                geo.longitude = 20.0
            }
        })
        val pg = context.newPage()
        pg.navigate(httpServer.emptyPage)

        val geolocation = page.evaluate(jsScriptForCurrentPosition)
        assertEquals(mapOf("latitude" to 10, "longitude" to 10), geolocation)

        val geolocation2 = pg.evaluate(jsScriptForCurrentPosition)
        assertEquals(mapOf("latitude" to 20, "longitude" to 20), geolocation2)
        context.close()
    }

    @Test
    fun `check to not modify passed default options object`() {
        val geolocation = Geolocation {
            it.latitude = 10.0
            it.longitude = 10.0
        }
        val options = NewContextOptions { it.geolocation = geolocation }
        val context = browser.newContext(options)
        assertEquals(geolocation, options.geolocation)
        context.clearCookies()
    }

    @Test
    fun `check to use context options`() {
        val options = NewContextOptions {
            it.geolocation = Geolocation { geo ->
                geo.latitude = 10.0
                geo.longitude = 10.0
            }
            it.permissions = arrayListOf("geolocation")
        }
        val context = browser.newContext(options)
        val pg = context.newPage()
        pg.navigate(httpServer.emptyPage)
        val geolocation = pg.evaluate(jsScriptForCurrentPosition)
        assertEquals(mapOf("latitude" to 10, "longitude" to 10), geolocation)
        context.close()
    }

    @Test
    fun `check to notify of watched position`() {
        browserContext.grantPermissions(arrayListOf("geolocation"))
        page.navigate(httpServer.emptyPage)
        val messages = arrayListOf<String>()
        page.onConsoleMessage { messages.add(it.text()) }
        browserContext.setGeolocation(Geolocation {
            it.latitude = 0.0
            it.longitude = 0.0
        })
        val dollar = "$";
        val jsScript = """() => {
            |   navigator.geolocation.watchPosition(pos => {
            |       const coords = pos.coords;
            |       console.log(`lat=$dollar{coords.latitude} lng=$dollar{coords.longitude}`);
            |   }, err => {});
            |}
        """.trimMargin()
        page.evaluate(jsScript)

        var message = page.waitForConsoleMessage {
            browserContext.setGeolocation(Geolocation {
                it.latitude = 0.0
                it.longitude = 10.0
            })
        }
        while (!message!!.text().contains("lat=0 lng=10")) {
            message = page.waitForConsoleMessage { }
        }
        assertNotNull(message)
        assertTrue(message.text().contains("lat=0 lng=10"))

        message = page.waitForConsoleMessage {
            browserContext.setGeolocation(Geolocation {
                it.latitude = 20.0
                it.longitude = 30.0
            })
        }
        while (!message!!.text().contains("lat=20 lng=30")) {
            message = page.waitForConsoleMessage { }
        }
        assertTrue(message.text().contains("lat=20 lng=30"))

        message = page.waitForConsoleMessage {
            browserContext.setGeolocation(Geolocation {
                it.latitude = 40.0
                it.longitude = 50.0
            })
        }
        while (!message!!.text().contains("lat=40 lng=50")) {
            message = page.waitForConsoleMessage { }
        }
        assertTrue(message.text().contains("lat=40 lng=50"))

        assertTrue(messages.contains("lat=0 lng=10"))
        assertTrue(messages.contains("lat=20 lng=30"))
        assertTrue(messages.contains("lat=40 lng=50"))
    }

    @Test
    fun `check to use context options for popup`() {
        browserContext.grantPermissions(arrayListOf("geolocation"))
        browserContext.setGeolocation(Geolocation {
            it.latitude = 10.0
            it.longitude = 10.0
        })
        val popup = page.waitForPopup {
            page.evaluate(
                "url => window['_popup'] = window.open(url)",
                "${httpServer.prefixWithDomain}/geolocation.html"
            )
        }
        assertNotNull(popup)
        popup.waitForLoadState()
        val geolocation = popup.evaluate("window['geolocationPromise']")
        assertEquals(mapOf("longitude" to 10, "latitude" to 10), geolocation)
    }
}