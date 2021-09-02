package io.github.tmcreative1.playwright.remote.utils

import io.github.tmcreative1.playwright.remote.core.enums.Platform
import io.github.tmcreative1.playwright.remote.core.exceptions.PlatformException

class PlatformUtils {

    companion object {
        @JvmStatic
        fun getCurrentPlatform(): Platform {
            val osName = System.getProperty("os.name").lowercase()
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