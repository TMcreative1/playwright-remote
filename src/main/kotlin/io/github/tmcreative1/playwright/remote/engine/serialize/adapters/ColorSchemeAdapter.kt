package io.github.tmcreative1.playwright.remote.engine.serialize.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.options.enum.ColorScheme
import io.github.tmcreative1.playwright.remote.engine.options.enum.ColorScheme.*

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