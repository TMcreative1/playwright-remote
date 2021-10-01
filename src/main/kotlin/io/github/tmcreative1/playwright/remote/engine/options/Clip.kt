package io.github.tmcreative1.playwright.remote.engine.options

data class Clip(
    /**
     * x-coordinate of top-left corner of clip area
     */
    val x: Double,
    /**
     * y-coordinate of top-left corner of clip area
     */
    val y: Double,
    /**
     * width of clipping area
     */
    val width: Double,
    /**
     * height of clipping area
     */
    val height: Double
)
