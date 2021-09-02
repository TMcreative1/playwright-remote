package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.RecordVideoSize
import io.github.tmcreative1.playwright.remote.engine.options.ViewportSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestScreencast : BaseTest() {

    @Test
    fun `check to expose video path`(@TempDir videosDir: Path) {
        val path: Path
        browser.newContext(NewContextOptions {
            it.recordVideoDir = videosDir
            it.recordVideoSize = RecordVideoSize { opt ->
                opt.width = 320
                opt.height = 240
            }
            it.viewportSize = ViewportSize { opt ->
                opt.width = 320
                opt.height = 240
            }
        }).use {
            val pg = it.newPage()
            pg.evaluate("() => document.body.style.backgroundColor = 'red'")
            val video = pg.video()
            assertNotNull(video)
            path = video.path()!!
            assertTrue(path.startsWith(videosDir))
        }
        assertTrue(Files.exists(path))
    }
}