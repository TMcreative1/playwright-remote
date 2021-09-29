package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.core.enums.ForcedColors
import io.github.tmcreative1.playwright.remote.core.enums.ReducedMotion
import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder
import io.github.tmcreative1.playwright.remote.engine.options.enum.ColorScheme
import java.nio.file.Path

data class NewPageOptions @JvmOverloads constructor(
    /**
     * Whether to automatically download all the attachments. Defaults to {@code false} where all the downloads are canceled.
     */
    var acceptDownloads: Boolean? = null,
    /**
     * When using {@link Page#goto Page.goto()}, {@link Page#route Page.route()}, {@link Page#waitForURL Page.waitForURL()},
     * {@link Page#waitForRequest Page.waitForRequest()}, or {@link Page#waitForResponse Page.waitForResponse()} it takes the
     * base URL in consideration by using the <a href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code URL()}</a>
     * constructor for building the corresponding URL. Examples:
     * <ul>
     * <li> baseURL: {@code http://localhost:3000} and navigating to {@code /bar.html} results in {@code http://localhost:3000/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo/} and navigating to {@code ./bar.html} results in {@code http://localhost:3000/foo/bar.html}</li>
     * </ul>
     */
    var baseURL: String? = null,
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
    /**
     * Emulates {@code "prefers-reduced-motion"} media feature, supported values are {@code "reduce"}, {@code "no-preference"}. See {@link
     * Page#emulateMedia Page.emulateMedia()} for more details. Defaults to {@code "no-preference"}.
     */
    var reducedMotion: ReducedMotion? = null,
    /**
     * It specified, enables strict selectors mode for this context. In the strict selectors mode all operations on selectors
     * that imply single target DOM element will throw when more than one element matches the selector. See {@code Locator} to learn
     * more about the strict mode.
     */
    var strictSelectors: Boolean? = null,
    /**
     * Emulates {@code "forced-colors"} media feature, supported values are {@code "active"}, {@code "none"}. See {@link Page#emulateMedia
     * Page.emulateMedia()} for more details. Defaults to {@code "none"}.
     *
     * <p> <strong>NOTE:</strong> It's not supported in WebKit, see <a href="https://bugs.webkit.org/show_bug.cgi?id=225281">here</a> in their issue
     * tracker.
     */
    var forcedColors: ForcedColors? = null,
    @Transient private val builder: IBuilder<NewPageOptions>
) {
    init {
        builder.build(this)
    }
}