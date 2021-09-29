package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.Clip
import io.github.tmcreative1.playwright.remote.engine.options.ScreenshotOptions
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.test.assertEquals

class TestPageScreenshot : BaseTest() {
    @Test
    fun `check correct work of screenshot`() {
        page.setViewportSize(500, 500)
        page.navigate("${httpServer.prefixWithDomain}/grid.html")
        val screenshot = page.screenshot()
        val image = ImageIO.read(ByteArrayInputStream(screenshot))
        assertEquals(500, image.width)
        assertEquals(500, image.height)
    }

    @Test
    fun `check correct work of screenshot with clip`() {
        page.setViewportSize(500, 500)
        page.navigate("${httpServer.prefixWithDomain}/grid.html")
        val screenshot = page.screenshot(ScreenshotOptions { it.clip = Clip(50.0, 100.0, 150.0, 100.0) })
        val image = ImageIO.read(ByteArrayInputStream(screenshot))
        assertEquals(150, image.width)
        assertEquals(100, image.height)
    }
}