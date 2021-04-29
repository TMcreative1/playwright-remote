package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder
import java.nio.file.Path

data class PdfOptions @JvmOverloads constructor(
    /**
     * Display header and footer. Defaults to {@code false}.
     */
    var displayHeaderFooter: Boolean? = null,
    /**
     * HTML template for the print footer. Should use the same format as the {@code headerTemplate}.
     */
    var footerTemplate: String? = null,
    /**
     * Paper format. If set, takes priority over {@code width} or {@code height} options. Defaults to 'Letter'.
     */
    var format: String? = null,
    /**
     * HTML template for the print header. Should be valid HTML markup with following classes used to inject printing values
     * into them:
     * <ul>
     * <li> {@code "date"} formatted print date</li>
     * <li> {@code "title"} document title</li>
     * <li> {@code "url"} document location</li>
     * <li> {@code "pageNumber"} current page number</li>
     * <li> {@code "totalPages"} total pages in the document</li>
     * </ul>
     */
    var headerTemplate: String? = null,
    /**
     * Paper height, accepts values labeled with units.
     */
    var height: String? = null,
    /**
     * Paper orientation. Defaults to {@code false}.
     */
    var landscape: Boolean? = null,
    /**
     * Paper margins, defaults to none.
     */
    var margin: Margin? = null,
    /**
     * Paper ranges to print, e.g., '1-5, 8, 11-13'. Defaults to the empty string, which means print all pages.
     */
    var pageRanges: String? = null,
    /**
     * The file path to save the PDF to. If {@code path} is a relative path, then it is resolved relative to the current working
     * directory. If no path is provided, the PDF won't be saved to the disk.
     */
    var path: Path? = null,
    /**
     * Give any CSS {@code @page} size declared in the page priority over what is declared in {@code width} and {@code height} or {@code format}
     * options. Defaults to {@code false}, which will scale the content to fit the paper size.
     */
    var preferCSSPageSize: Boolean? = null,
    /**
     * Print background graphics. Defaults to {@code false}.
     */
    var printBackground: Boolean? = null,
    /**
     * Scale of the webpage rendering. Defaults to {@code 1}. Scale amount must be between 0.1 and 2.
     */
    var scale: Double? = null,
    /**
     * Paper width, accepts values labeled with units.
     */
    var width: String? = null,
    @Transient private val builder: IBuilder<PdfOptions>
) {
    init {
        builder.build(this)
    }
}
