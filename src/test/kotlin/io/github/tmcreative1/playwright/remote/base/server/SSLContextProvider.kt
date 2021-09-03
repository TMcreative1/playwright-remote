/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.playwright.remote.base.server

import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

internal class SSLContextProvider {

    companion object {
        private const val PASSWORD = "password"

        // @see http://rememberjava.com/http/2017/04/29/simple_https_server.html
        // Generate key via
        // keytool -genkey -keyalg RSA -validity 36500 -keysize 4096 -dname cn=Playwright,ou=Playwright,o=Playwright,c=US -keystore keystore.jks -storepass password -keypass password
        @JvmStatic
        fun createSSLContext(): SSLContext =
            try {
                KeyStore.getInstance("JKS")
                    .apply { load(FileInputStream("src/test/resources/keys/keystore.jks"), PASSWORD.toCharArray()) }
                    .run {
                        val kmf =
                            KeyManagerFactory.getInstance("SunX509").apply { init(this@run, PASSWORD.toCharArray()) }
                        val tmf = TrustManagerFactory.getInstance("SunX509").apply { init(this@run) }
                        SSLContext.getInstance("TLS").apply { init(kmf.keyManagers, tmf.trustManagers, null) }
                    }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

    }
}