package com.playwright.remote.engine.serialize.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.enum.ColorScheme
import com.playwright.remote.engine.options.enum.ColorScheme.*

class ColorSchemeAdapter : TypeAdapter<ColorScheme>() {
    override fun write(out: JsonWriter?, value: ColorScheme?) {
        out?.value(
            when (value) {
                DARK -> "dark"
                LIGHT -> "light"
                NO_PREFERENCE -> "no-preference"
                else -> throw PlaywrightException("Unexpected value: $value")
            }
        )
    }

    override fun read(`in`: JsonReader?): ColorScheme =
        when (val value = `in`!!.nextString()) {
            "dark" -> DARK
            "light" -> LIGHT
            "no-preference" -> NO_PREFERENCE
            else -> throw PlaywrightException("Unexpected value: $value")
        }
}