package com.playwright.remote.engine.serialize

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.playwright.remote.core.enums.*
import com.playwright.remote.engine.handle.js.impl.JSHandle
import com.playwright.remote.engine.options.enum.ColorScheme
import com.playwright.remote.engine.options.enum.MouseButton
import com.playwright.remote.engine.serialize.adapters.ColorSchemeAdapter
import com.playwright.remote.engine.serialize.adapters.SameSiteAttributeAdapter
import com.playwright.remote.engine.serialize.serializers.*
import java.nio.file.Path
import java.util.*

class CustomGson {
    companion object {
        private var gson: Gson? = null

        @JvmStatic
        fun gson(): Gson {
            if (gson == null) {
                gson = GsonBuilder()
                    .registerTypeAdapter(SameSiteAttribute::class.java, SameSiteAttributeAdapter().nullSafe())
                    .registerTypeAdapter(ColorScheme::class.java, ColorSchemeAdapter().nullSafe())
                    .registerTypeAdapter(Media::class.java, MediaSerializer())
                    .registerTypeAdapter(ScreenshotType::class.java, ToLowerCaseSerializer<ScreenshotType>())
                    .registerTypeAdapter(MouseButton::class.java, ToLowerCaseSerializer<MouseButton>())
                    .registerTypeAdapter(LoadState::class.java, ToLowerCaseSerializer<LoadState>())
                    .registerTypeAdapter(WaitUntilState::class.java, ToLowerCaseSerializer<WaitUntilState>())
                    .registerTypeAdapter(
                        WaitForSelectorState::class.java,
                        ToLowerCaseSerializer<WaitForSelectorState>()
                    )
                    .registerTypeAdapter(
                        getGenericType<List<KeyboardModifier>>(),
                        KeyboardModifiersSerializer()
                    )
                    .registerTypeAdapter(Optional::class.java, OptionalSerializer())
                    .registerTypeHierarchyAdapter(JSHandle::class.java, HandleSerializer())
                    .registerTypeAdapter(getGenericType<Map<String, String>>(), StringMapSerializer())
                    .registerTypeAdapter(getGenericType<Map<String, Any>>(), FirefoxUserPrefsSerializer())
                    .registerTypeHierarchyAdapter(Path::class.java, PathSerializer())
                    .create()
            }
            return gson!!
        }

        private inline fun <reified T> getGenericType() =
            object : TypeToken<T>() {}.type
    }
}