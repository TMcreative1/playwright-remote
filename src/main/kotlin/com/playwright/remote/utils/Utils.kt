package com.playwright.remote.utils

import com.playwright.remote.core.exceptions.PlaywrightException
import okio.IOException
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
    }
}