package com.playwright.remote.domain

data class BoundingBox(
    /**
     * the x coordinate of the element in pixels.
     */
    val x: Double,
    /**
     * the y coordinate of the element in pixels.
     */
    val y: Double,
    /**
     * the width of the element in pixels.
     */
    val width: Double,
    /**
     * the height of the element in pixels.
     */
    val height: Double
)
