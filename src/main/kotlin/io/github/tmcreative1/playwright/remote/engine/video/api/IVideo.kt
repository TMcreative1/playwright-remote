package io.github.tmcreative1.playwright.remote.engine.video.api

import io.github.tmcreative1.playwright.remote.engine.download.impl.Artifact
import java.nio.file.Path

/**
 * When browser context is created with the {@code recordVideo} option, each page has a video object associated with it.
 * <pre>{@code
 * System.out.println(page.video().path());
 * }</pre>
 */
interface IVideo {
    /**
     * Deletes the video file. Will wait for the video to finish if necessary.
     */
    fun delete()

    /**
     * Saves the video to a user-specified path. It is safe to call this method while the video is still in progress, or after
     * the page has closed. This method waits until the page is closed and the video is fully saved.
     *
     * @param path Path where the video should be saved.
     */
    fun saveAs(path: Path)

    fun setArtifact(artifact: Artifact)
}