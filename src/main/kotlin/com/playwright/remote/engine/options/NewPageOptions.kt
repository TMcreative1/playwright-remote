package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder
import com.playwright.remote.engine.options.enum.ColorScheme
import java.nio.file.Path

data class NewPageOptions @JvmOverloads constructor(
    /**
     * Whether to automatically download all the attachments. Defaults to {@code false} where all the downloads are canceled.
     */
    var acceptDownloads: Boolean? = null,
    /**
     * Toggles bypassing page's Content-Security-Policy.
     */
    var bypassCSP: Boolean? = null,
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code "no-preference"}. See
     * {@link Page#emulateMedia Page.emulateMedia()} for more details. Defaults to {@code "light"}.
     */
    var colorScheme: ColorScheme? = null,
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}.
     */
    var deviceScaleFactor: Double? = null,
    /**
     * An object containing additional HTTP headers to be sent with every request. All header values must be strings.
     */
    var extraHTTPHeaders: Map<String, String>? = null,
    var geolocation: Geolocation? = null,
    /**
     * Specifies if viewport supports touch events. Defaults to false.
     */
    var hasTouch: Boolean? = null,
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>.
     */
    var httpCredentials: HttpCredentials? = null,
    /**
     * Whether to ignore HTTPS errors during navigation. Defaults to {@code false}.
     */
    var ignoreHTTPSErrors: Boolean? = null,
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. Defaults to {@code false}. Not supported
     * in Firefox.
     */
    var isMobile: Boolean? = null,
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}.
     */
    var javaScriptEnabled: Boolean? = null,
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value, {@code Accept-Language}
     * request header value as well as number and date formatting rules.
     */
    var locale: String? = null,
    /**
     * Whether to emulate network being offline. Defaults to {@code false}.
     */
    var offline: Boolean? = null,
    /**
     * A list of permissions to grant to all pages in this context. See {@link BrowserContext#grantPermissions
     * BrowserContext.grantPermissions()} for more details.
     */
    var permissions: List<String>? = null,
    /**
     * Network proxy settings to use with this context. Note that browser needs to be launched with the global proxy for this
     * option to work. If all contexts override the proxy, global proxy will be never used and can be any string, for example
     * {@code launch({ proxy: { server: 'per-context' } })}.
     */
    var proxy: Proxy? = null,
    /**
     * Optional setting to control whether to omit request content from the HAR. Defaults to {@code false}.
     */
    var recordHarOmitContent: Boolean? = null,
    /**
     * Path on the filesystem to write the HAR file to.
     */
    var recordHarPath: Path? = null,
    /**
     * Path to the directory to put videos into.
     */
    var recordVideoDir: Path? = null,
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each page will
     * be scaled down if necessary to fit the specified size.
     */
    var recordVideoSize: RecordVideoSize? = null,
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}.
     */
    var storageState: String? = null,
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}. Path to the file with saved storage
     * state.
     */
    var storageStatePath: Path? = null,
    /**
     * Changes the timezone of the context. See <a
     * href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">ICU's
     * metaZones.txt</a> for a list of supported timezone IDs.
     */
    var timezoneId: String? = null,
    /**
     * Specific user agent to use in this context.
     */
    var userAgent: String? = null,
    /**
     * Sets a consistent viewport for each page. Defaults to an 1280x720 viewport. {@code null} disables the default viewport.
     */
    var viewportSize: ViewportSize? = null,
    @Transient private val builder: IBuilder<NewPageOptions>
) {
    init {
        builder.build(this)
    }
}