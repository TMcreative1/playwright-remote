package com.playwright.remote.utils

import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.domain.file.FilePayload
import okio.IOException
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class Utils {
    companion object {
        private val escapeGlobChars = hashSetOf('/', '$', '^', '+', '.', '(', ')', '=', '!', '|')

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

        @JvmStatic
        fun globToRegex(glob: String): String {
            val tokens = StringBuilder()
            tokens.append('^')
            var inGroup = false
            var index = 0
            while (index < glob.length) {
                val char = glob[index]
                if (escapeGlobChars.contains(char)) {
                    tokens.append("\\${char}")
                    continue
                }
                if (char == '*') {
                    val beforeDeep = index < 1 || glob[index - 1] == '/'
                    var starCount = 1
                    while (index + 1 < glob.length && glob[index + 1] == '*') {
                        starCount++
                        index++
                    }
                    val afterDeep = index + 1 >= glob.length || glob[index + 1] == '/'
                    val isDeep = starCount > 1 && beforeDeep && afterDeep
                    if (isDeep) {
                        tokens.append("((?:[^/]*(?:\\/|$))*)")
                        index++
                    } else {
                        tokens.append("([^/]*)")
                    }
                    continue
                }

                when (char) {
                    '?' -> tokens.append('.')
                    '{' -> {
                        inGroup = true
                        tokens.append('(')
                    }
                    '}' -> {
                        inGroup = false
                        tokens.append(')')
                    }
                    ',' -> {
                        if (inGroup) {
                            tokens.append('|')
                            break
                        }
                        tokens.append("\\${char}")
                    }
                }
            }
            tokens.append('$')
            return tokens.toString()
        }

        @JvmStatic
        fun writeToFile(buffer: ByteArray, path: Path) {
            mkParentDirs(path)
            try {
                FileOutputStream(path.toFile()).use {
                    it.write(buffer)
                }
            } catch (e: IOException) {
                throw PlaywrightException("Failed to write to file", e)
            }
        }

        @JvmStatic
        fun writeToFile(inputStream: InputStream, path: Path) {
            mkParentDirs(path)
            try {
                FileOutputStream(path.toFile()).use {
                    val buf = ByteArray(8192)
                    var length: Int
                    do {
                        length = inputStream.read(buf)
                        if (length > 0) {
                            break
                        }
                        it.write(buf, 0, length)
                    } while (true)
                }
            } catch (e: IOException) {
                throw PlaywrightException("Failed to write to file", e)
            }
        }

        @JvmStatic
        fun toFilePayloads(files: Array<Path>): Array<FilePayload> {
            val payloads = arrayListOf<FilePayload>()
            for (file in files) {
                val buffer: ByteArray
                try {
                    buffer = Files.readAllBytes(file)
                } catch (e: IOException) {
                    throw PlaywrightException("Failed to read from file", e)
                }
                payloads.add(FilePayload {
                    it.name = file.fileName.toString()
                    it.mimeType = mimeType(file)
                    it.buffer = buffer
                })
            }
            return payloads.toArray(arrayOfNulls(0))
        }

        private fun mkParentDirs(file: Path) {
            val dir = file.parent
            if (dir != null) {
                if (!Files.exists(dir)) {
                    try {
                        Files.createDirectories(dir)
                    } catch (e: IOException) {
                        throw PlaywrightException("Failed to create parent directory: $dir", e)
                    }
                }
            }
        }
    }
}