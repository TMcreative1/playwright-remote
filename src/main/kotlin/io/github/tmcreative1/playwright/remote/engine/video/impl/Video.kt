package io.github.tmcreative1.playwright.remote.engine.video.impl

import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.download.impl.Artifact
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.page.impl.Page
import io.github.tmcreative1.playwright.remote.engine.video.api.IVideo
import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait
import io.github.tmcreative1.playwright.remote.engine.waits.impl.WaitRace
import io.github.tmcreative1.playwright.remote.engine.waits.impl.WaitResult
import java.nio.file.Path

class Video(private val page: IPage) : IVideo {
    private val waitArtifact = WaitResult<Artifact>()

    override fun delete() {
        try {
            waitForArtifact()!!.delete()
        } catch (e: PlaywrightException) {
        }
    }

    override fun saveAs(path: Path) {
        try {
            waitForArtifact()!!.saveAs(path)
        } catch (e: PlaywrightException) {
            throw PlaywrightException("Page did not produce any video frames", e)
        }
    }

    override fun setArtifact(artifact: Artifact) {
        waitArtifact.complete(artifact)
    }

    @Suppress("UNCHECKED_CAST")
    private fun waitForArtifact(): Artifact? {
        val wait = WaitRace(listOf(waitArtifact, ((page as Page).waitClosedOrCrashed) as IWait<Artifact?>))
        return page.runUtil(wait) {}
    }
}