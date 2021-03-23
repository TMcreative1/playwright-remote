package com.playwright.remote.utils

import com.playwright.remote.core.exceptions.PlaywrightException
import okio.IOException
import java.nio.file.Files
import java.nio.file.Path

class Utils {
    companion object {
        @JvmStatic
        fun mimeType(path: Path): String {
            val mimeType: String?
            try {
                mimeType = Files.probeContentType(path)
            } catch (e: IOException) {
                throw PlaywrightException("Failed to determine mime type", e)
            }

            return mimeType ?: "application/octet-stream"
        }
    }
}