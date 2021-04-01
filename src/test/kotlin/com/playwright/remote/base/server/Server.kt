/*
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.playwright.remote.base.server

import com.sun.net.httpserver.*
import java.io.*
import java.net.InetSocketAddress
import java.nio.file.FileSystems
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.zip.GZIPOutputStream

class Server private constructor(port: Int, https: Boolean) : HttpHandler {
    private var server: HttpServer? = null
    val prefixWithDomain: String = "http${if (https) "s" else ""}://localhost:$port"
    val prefixWithIP: String = "http${if (https) "s" else ""}://127.0.0.1:$port"
    val emptyPage: String = "$prefixWithDomain/empty.html"
    val serverPort = port
    private val resourcesDir: File
    private val requestSubscribers = Collections.synchronizedMap(HashMap<String, CompletableFuture<Request>>())
    private val auths = Collections.synchronizedMap(HashMap<String, Auth>())
    private val csp = Collections.synchronizedMap(HashMap<String, String>())
    private val routes = Collections.synchronizedMap(HashMap<String, HttpHandler>())
    private val gzipRoutes = Collections.synchronizedSet(HashSet<String>())

    private data class Auth(val user: String, val password: String)

    init {
        if (https) {
            val httpsServer = HttpsServer.create(InetSocketAddress("localhost", port), 0)
            httpsServer.httpsConfigurator = HttpsConfigurator(SSLContextProvider.createSSLContext())
            server = httpsServer
        } else {
            server = HttpServer.create(InetSocketAddress("localhost", port), 0)
        }
        server!!.createContext("/", this)
        server!!.executor = Executors.newWorkStealingPool() // creates a default executor
        val cwd = FileSystems.getDefault().getPath(".").toFile()
        resourcesDir = File(cwd, "src/test/resources")
        server!!.start()
    }

    companion object {

        @JvmStatic
        @Throws(IOException::class)
        fun createHttp(port: Int): Server {
            return Server(port, false)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun createHttps(port: Int): Server {
            return Server(port, true)
        }

    }

    fun stop() {
        server!!.stop(0)
    }

    fun setAuth(path: String, user: String, password: String) {
        auths[path] = Auth(user, password)
    }

    fun setCSP(path: String, csp: String) {
        this.csp[path] = csp
    }

    fun enableGzip(path: String) {
        gzipRoutes.add(path)
    }

    class Request(exchange: HttpExchange) {
        val method: String = exchange.requestMethod
        val headers: Headers = exchange.requestHeaders
        val postBody: ByteArray

        init {
            val out = ByteArrayOutputStream()
            exchange.requestBody.copyTo(out, 8196)
            postBody = out.toByteArray()
        }
    }

    fun futureRequest(path: String): Future<Request> {
        var future = requestSubscribers[path]
        if (future == null) {
            future = CompletableFuture()
            requestSubscribers[path] = future
        }
        return future
    }

    fun setRoute(path: String, handler: HttpHandler) {
        routes[path] = handler
    }

    fun setRedirect(from: String, to: String) {
        setRoute(from) { exchange: HttpExchange ->
            exchange.responseHeaders["location"] = listOf(to)
            exchange.sendResponseHeaders(302, -1)
            exchange.responseBody.close()
        }
    }

    fun reset() {
        requestSubscribers.clear()
        auths.clear()
        csp.clear()
        routes.clear()
        gzipRoutes.clear()
    }

    @Throws(IOException::class)
    override fun handle(exchange: HttpExchange) {
        val path = exchange.requestURI.path
        if (auths.containsKey(path)) {
            val header = exchange.requestHeaders["authorization"]
            var authorized = false
            if (header != null) {
                val v = header[0]
                val splits = v.split(" ").toTypedArray()
                if (splits.size == 2) {
                    val credentials = String(Base64.getDecoder().decode(splits[1]))
                    val auth = auths[path]
                    authorized = credentials == auth!!.user + ":" + auth.password
                }
            }
            if (!authorized) {
                exchange.responseHeaders.add("WWW-Authenticate", "Basic realm=\"Secure Area\"")
                exchange.sendResponseHeaders(401, 0)
                OutputStreamWriter(exchange.responseBody).use { writer ->
                    writer.write("HTTP Error 401 Unauthorized: Access is denied")
                    exchange.responseBody.close()
                }
                return
            }
        }
        synchronized(requestSubscribers) {
            val subscriber = requestSubscribers[path]
            if (subscriber != null) {
                requestSubscribers.remove(path)
                subscriber.complete(Request(exchange))
            }
        }
        val handler = routes[path]
        if (handler != null) {
            handler.handle(exchange)
            return
        }
        if (csp.containsKey(path)) {
            exchange.responseHeaders.add("Content-Security-Policy", csp[path])
        }
        val file = File(resourcesDir, path.substring(1))
        if (!file.exists()) {
            exchange.sendResponseHeaders(404, 0)
            OutputStreamWriter(exchange.responseBody).use { writer -> writer.write("File not found: " + file.canonicalPath) }
            return
        }
        exchange.responseHeaders.add("Content-Type", mimeType(file))
        var output = exchange.responseBody
        if (gzipRoutes.contains(path)) {
            exchange.responseHeaders.add("Content-Encoding", "gzip")
        }
        try {
            FileInputStream(file).use { input ->
                exchange.sendResponseHeaders(200, 0)
                if (gzipRoutes.contains(path)) {
                    output = GZIPOutputStream(output)
                }
                input.copyTo(output)
            }
        } catch (e: IOException) {
            OutputStreamWriter(exchange.responseBody).use { writer -> writer.write("Exception: $e") }
            return
        }
        output.close()
    }

    private fun mimeType(file: File) = extensionToMime[file.extension] ?: "application/octet-stream"

    private val extensionToMime: Map<String, String> = mapOf(
        "ai" to "application/postscript",
        "apng" to "image/apng",
        "appcache" to "text/cache-manifest",
        "au" to "audio/basic",
        "bmp" to "image/bmp",
        "cer" to "application/pkix-cert",
        "cgm" to "image/cgm",
        "coffee" to "text/coffeescript",
        "conf" to "text/plain",
        "crl" to "application/pkix-crl",
        "css" to "text/css",
        "csv" to "text/csv",
        "def" to "text/plain",
        "doc" to "application/msword",
        "dot" to "application/msword",
        "drle" to "image/dicom-rle",
        "dtd" to "application/xml-dtd",
        "ear" to "application/java-archive",
        "emf" to "image/emf",
        "eps" to "application/postscript",
        "exr" to "image/aces",
        "fits" to "image/fits",
        "g3" to "image/g3fax",
        "gbr" to "application/rpki-ghostbusters",
        "gif" to "image/gif",
        "glb" to "model/gltf-binary",
        "gltf" to "model/gltf+json",
        "gz" to "application/gzip",
        "h261" to "video/h261",
        "h263" to "video/h263",
        "h264" to "video/h264",
        "heic" to "image/heic",
        "heics" to "image/heic-sequence",
        "heif" to "image/heif",
        "heifs" to "image/heif-sequence",
        "htm" to "text/html",
        "html" to "text/html",
        "ics" to "text/calendar",
        "ief" to "image/ief",
        "ifb" to "text/calendar",
        "iges" to "model/iges",
        "igs" to "model/iges",
        "in" to "text/plain",
        "ini" to "text/plain",
        "jade" to "text/jade",
        "jar" to "application/java-archive",
        "jls" to "image/jls",
        "jp2" to "image/jp2",
        "jpe" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "jpf" to "image/jpx",
        "jpg" to "image/jpeg",
        "jpg2" to "image/jp2",
        "jpgm" to "video/jpm",
        "jpgv" to "video/jpeg",
        "jpm" to "image/jpm",
        "jpx" to "image/jpx",
        "js" to "application/javascript",
        "json" to "application/json",
        "json5" to "application/json5",
        "jsx" to "text/jsx",
        "jxr" to "image/jxr",
        "kar" to "audio/midi",
        "ktx" to "image/ktx",
        "less" to "text/less",
        "list" to "text/plain",
        "litcoffee" to "text/coffeescript",
        "log" to "text/plain",
        "m1v" to "video/mpeg",
        "m21" to "application/mp21",
        "m2a" to "audio/mpeg",
        "m2v" to "video/mpeg",
        "m3a" to "audio/mpeg",
        "m4a" to "audio/mp4",
        "m4p" to "application/mp4",
        "man" to "text/troff",
        "manifest" to "text/cache-manifest",
        "markdown" to "text/markdown",
        "mathml" to "application/mathml+xml",
        "md" to "text/markdown",
        "mdx" to "text/mdx",
        "me" to "text/troff",
        "mesh" to "model/mesh",
        "mft" to "application/rpki-manifest",
        "mid" to "audio/midi",
        "midi" to "audio/midi",
        "mj2" to "video/mj2",
        "mjp2" to "video/mj2",
        "mjs" to "application/javascript",
        "mml" to "text/mathml",
        "mov" to "video/quicktime",
        "mp2" to "audio/mpeg",
        "mp21" to "application/mp21",
        "mp2a" to "audio/mpeg",
        "mp3" to "audio/mpeg",
        "mp4" to "video/mp4",
        "mp4a" to "audio/mp4",
        "mp4s" to "application/mp4",
        "mp4v" to "video/mp4",
        "mpe" to "video/mpeg",
        "mpeg" to "video/mpeg",
        "mpg" to "video/mpeg",
        "mpg4" to "video/mp4",
        "mpga" to "audio/mpeg",
        "mrc" to "application/marc",
        "ms" to "text/troff",
        "msh" to "model/mesh",
        "n3" to "text/n3",
        "oga" to "audio/ogg",
        "ogg" to "audio/ogg",
        "ogv" to "video/ogg",
        "ogx" to "application/ogg",
        "otf" to "font/otf",
        "p10" to "application/pkcs10",
        "p7c" to "application/pkcs7-mime",
        "p7m" to "application/pkcs7-mime",
        "p7s" to "application/pkcs7-signature",
        "p8" to "application/pkcs8",
        "pdf" to "application/pdf",
        "pki" to "application/pkixcmp",
        "pkipath" to "application/pkix-pkipath",
        "png" to "image/png",
        "ps" to "application/postscript",
        "pskcxml" to "application/pskc+xml",
        "qt" to "video/quicktime",
        "rmi" to "audio/midi",
        "rng" to "application/xml",
        "roa" to "application/rpki-roa",
        "roff" to "text/troff",
        "rsd" to "application/rsd+xml",
        "rss" to "application/rss+xml",
        "rtf" to "application/rtf",
        "rtx" to "text/richtext",
        "s3m" to "audio/s3m",
        "sgi" to "image/sgi",
        "sgm" to "text/sgml",
        "sgml" to "text/sgml",
        "shex" to "text/shex",
        "shtml" to "text/html",
        "sil" to "audio/silk",
        "silo" to "model/mesh",
        "slim" to "text/slim",
        "slm" to "text/slim",
        "snd" to "audio/basic",
        "spx" to "audio/ogg",
        "stl" to "model/stl",
        "styl" to "text/stylus",
        "stylus" to "text/stylus",
        "svg" to "image/svg+xml",
        "svgz" to "image/svg+xml",
        "t" to "text/troff",
        "t38" to "image/t38",
        "text" to "text/plain",
        "tfx" to "image/tiff-fx",
        "tif" to "image/tiff",
        "tiff" to "image/tiff",
        "tr" to "text/troff",
        "ts" to "video/mp2t",
        "tsv" to "text/tab-se,parated-values",
        "ttc" to "font/collection",
        "ttf" to "font/ttf",
        "ttl" to "text/turtle",
        "txt" to "text/plain",
        "uri" to "text/uri-list",
        "uris" to "text/uri-list",
        "urls" to "text/uri-list",
        "vcard" to "text/vcard",
        "vrml" to "model/vrml",
        "vtt" to "text/vtt",
        "war" to "application/java-archive",
        "wasm" to "application/wasm",
        "wav" to "audio/wav",
        "weba" to "audio/webm",
        "webm" to "video/webm",
        "webmanifest" to "application/manifest+json",
        "webp" to "image/webp",
        "wmf" to "image/wmf",
        "woff" to "font/woff",
        "woff2" to "font/woff2",
        "wrl" to "model/vrml",
        "x3d" to "model/x3d+xml",
        "x3db" to "model/x3d+fastinfoset",
        "x3dbz" to "model/x3d+binary",
        "x3dv" to "model/x3d-vrml",
        "x3dvz" to "model/x3d+vrml",
        "x3dz" to "model/x3d+xml",
        "xaml" to "application/xaml+xml",
        "xht" to "application/xhtml+xml",
        "xhtml" to "application/xhtml+xml",
        "xm" to "audio/xm",
        "xml" to "text/xml",
        "xsd" to "application/xml",
        "xsl" to "application/xml",
        "xslt" to "application/xslt+xml",
        "yaml" to "text/yaml",
        "yml" to "text/yaml",
        "zip" to "application/zip"
    )

}