package io.github.tmcreative1.playwright.remote.engine.download.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.download.api.IArtifact
import io.github.tmcreative1.playwright.remote.engine.download.api.IDownload
import java.io.InputStream
import java.nio.file.Path

class Download(
    private val artifact: IArtifact,
    private val initializer: JsonObject
) : IDownload {

    override fun createReadStream(): InputStream? {
        return artifact.createReadStream()
    }

    override fun delete() {
        artifact.delete()
    }

    override fun failure(): String? {
        return artifact.failure()
    }

    override fun saveAs(path: Path) {
        artifact.saveAs(path)
    }

    override fun suggestedFilename(): String {
        return initializer["suggestedFilename"].asString
    }

    override fun url(): String {
        return initializer["url"].asString
    }
}