package io.github.tmcreative1.playwright.remote.core.enums

enum class Platform(val platformType: String, val nodeProcess: String) {
    WINDOWS32("win32", "node.exe"),
    WINDOWS64("win32_x64", "node.exe"),
    MAC("mac", "node"),
    LINUX("linux", "node")
}