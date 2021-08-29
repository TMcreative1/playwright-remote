package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.options.NewContextOptions
import com.playwright.remote.engine.options.RecordVideoSize
import com.playwright.remote.engine.options.ViewportSize
import com.playwright.remote.engine.page.api.IPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestScreencast : BaseTest() {

    @Test
    fun `check to save as video`(@TempDir videosDir: Path) {
        val pg: IPage
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
            pg = it.newPage()
            pg.evaluate("() => document.body.style.backgroundColor = 'red'")
            pg.waitForTimeout(1000.0)
        }
        val video = pg.video()
        assertNotNull(video)
        val videoPath = videosDir.resolve("video.webm")
        video.saveAs(videoPath)
        assertTrue(Files.exists(videoPath))
    }
}