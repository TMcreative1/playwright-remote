package com.playwright.remote.utils

import com.playwright.remote.core.enums.Platform
import com.playwright.remote.core.exceptions.PlatformException

class PlatformUtils {

    companion object {
        @JvmStatic
        fun getCurrentPlatform(): Platform {
            val osName = System.getProperty("os.name").toLowerCase()
            val archType = System.getProperty("sun.arch.data.model")
            return when {
                osName.contains("win") && archType.equals("32") -> {
                    Platform.WINDOWS32
                }
                osName.contains("win") && archType.equals("64") -> {
                    Platform.WINDOWS64
                }
                osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> {
                    Platform.LINUX
                }
                osName.contains("mac") -> {
                    Platform.MAC
                }
                else -> throw PlatformException("Unknown platform")
            }
        }
    }
}