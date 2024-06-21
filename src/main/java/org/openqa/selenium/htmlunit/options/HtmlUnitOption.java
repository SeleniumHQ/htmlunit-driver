// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit.options;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.security.KeyStore;
import java.util.Map;

import org.htmlunit.BrowserVersion;
import org.htmlunit.Page;
import org.htmlunit.ProxyConfig;
import org.htmlunit.WebClientOptions;
import org.htmlunit.WebConnection;

/**
 * @author Scott Babcock
 */
public enum HtmlUnitOption implements HtmlUnitOptionNames, OptionEnum {
    WEB_CLIENT_VERSION(optWebClientVersion, BrowserVersion.class, BrowserVersion.BEST_SUPPORTED),

    /**
     * Enables/disables JavaScript support.
     * <p>
     * property: <b>webdriver.htmlunit.javaScriptEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code true}
     */
    JAVASCRIPT_ENABLED(optJavaScriptEnabled, boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setJavaScriptEnabled(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isJavaScriptEnabled();
        }
    },

    /**
     * Enables/disables CSS support.
     * If disabled, <b>HtmlUnit</b> will not download linked CSS files and also
     * not trigger the associated {@code onload}/{@code onerror} events.
     * <p>
     * property: <b>webdriver.htmlunit.cssEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code true}
     */
    CSS_ENABLED(optCssEnabled, boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setCssEnabled(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isCssEnabled();
        }
    },

    /**
     * Specifies whether or not the content of the resulting document will be
     * printed to the console in the event of a failing response code.
     * Successful response codes are in the range <b>200-299</b>.
     * <p>
     * property: <b>webdriver.htmlunit.printContentOnFailingStatusCode</b><br>
     * type: {@code boolean}<br>
     * default: {@code true}
     */
    PRINT_CONTENT_ON_FAILING_STATUS_CODE(optPrintContentOnFailingStatusCode, boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setPrintContentOnFailingStatusCode(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isPrintContentOnFailingStatusCode();
        }
    },

    /**
     * Specifies whether or not an exception will be thrown in the event of a
     * failing status code. Successful status codes are in the range <b>200-299</b>.
     * <p>
     * property: <b>webdriver.htmlunit.throwExceptionOnFailingStatusCode</b><br>
     * type: {@code boolean}<br>
     * default: {@code true}
     */
    THROW_EXCEPTION_ON_FAILING_STATUS_CODE(optThrowExceptionOnFailingStatusCode, boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setThrowExceptionOnFailingStatusCode(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isThrowExceptionOnFailingStatusCode();
        }
    },

    /**
     * Indicates if an exception should be thrown when a script execution fails
     * or if it should be caught and just logged to allow page execution to continue.
     * <p>
     * property: <b>webdriver.htmlunit.throwExceptionOnScriptError</b><br>
     * type: {@code boolean}<br>
     * default: {@code true}
     */
    THROW_EXCEPTION_ON_SCRIPT_ERROR(optThrowExceptionOnScriptError, boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setThrowExceptionOnScriptError(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isThrowExceptionOnScriptError();
        }
    },

    /**
     * Enable/disable the popup window blocker. By default, the popup blocker is disabled, and popup
     * windows are allowed. When set to {@code true}, the {@code window.open()} function has no effect
     * and returns {@code null}.
     * <p>
     * property: <b>webdriver.htmlunit.popupBlockerEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code false}
     */
    POPUP_BLOCKER_ENABLED(optPopupBlockerEnabled, boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setPopupBlockerEnabled(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isPopupBlockerEnabled();
        }
    },

    /**
     * Sets whether or not redirections will be followed automatically on receipt of a redirect
     * status code from the server.
     * <p>
     * property: <b>webdriver.htmlunit.isRedirectEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code true} (enable automatic redirection)
     */
    IS_REDIRECT_ENABLED(optIsRedirectEnabled, boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setRedirectEnabled(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isRedirectEnabled();
        }
    },

    /**
     * Path to the directory to be used for storing the response content in a
     * temporary file. The specified directory is created if if doesn't exist.
     * <p>
     * property: <b>webdriver.htmlunit.tempFileDirectory</b><br>
     * type: {@link File}<br>
     * default: {@code null} (use system default)<br>
     * see: {@link org.htmlunit.WebClientOptions#setMaxInMemory(int) setMaxInMemory}
     */
    TEMP_FILE_DIRECTORY(optTempFileDirectory, File.class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            try {
                options.setTempFileDirectory(TypeCodec.decodeFile(value));
            }
            catch (final IOException e) {
                throw new IllegalArgumentException("Failed setting directory for temporary files", e);
            }
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getTempFileDirectory();
        }
    },

    /**
     * The SSL client certificate <b>KeyStore</b> to use.
     * <p>
     * <b>NOTE</b>:
     * <p>
     * property: <b>webdriver.htmlunit.sslClientCertificateStore</b><br>
     * type: {@link KeyStore}<br>
     * default: {@code null}<br>
     * see: {@link #SSL_CLIENT_CERTIFICATE_TYPE}<br>
     * see: {@link #SSL_CLIENT_CERTIFICATE_PASSWORD}
     */
    SSL_CLIENT_CERTIFICATE_STORE(optSslClientCertificateStore, KeyStore.class, null) {
        @Override
        public Object encode(final Object value) {
            return null;
        }

        @Override
        public void insert(final WebClientOptions options, final Object value) {
            try {
                final KeyStoreBean bean = TypeCodec.decodeKeyStore(value);
                options.setSSLClientCertificateKeyStore(bean.createUrl(), bean.getPassword(), bean.getType());
            }
            catch (final MalformedURLException e) {
                throw new IllegalArgumentException(
                        "Specified SSL_CLIENT_CERTIFICATE_STORE URL is malformed", e);
            }
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getSSLClientCertificateStore();
        }
    },

    /**
     * Type of the specified SSL client certificate <b>KeyStore</b> (e.g. - {@code jks} or {@code pkcs12}).
     * <p>
     * property: <b>webdriver.htmlunit.sslClientCertificateType</b><br>
     * type: {@link char[]}<br>
     * default: {@code null}<br>
     * see: {@link #SSL_CLIENT_CERTIFICATE_STORE}<br>
     * see: {@link #SSL_CLIENT_CERTIFICATE_PASSWORD}<br>
     * see: {@link java.security.Security#getProviders() Security.getProviders()}
     */
    SSL_CLIENT_CERTIFICATE_TYPE(optSslClientCertificateType, char[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefaultValue(value)) {
                throw new UnsupportedOperationException(
                        "SSL client certificate key store type cannot be set as a discrete value; "
                        + "use HtmlUnitDriverOptions.setSSLClientCertificateStore() instead");
            }
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            final KeyStore keyStore = options.getSSLClientCertificateStore();
            return (keyStore != null) ? keyStore.getType() : null;
        }
    },

    /**
     * Password for the specified SSL client certificate <b>KeyStore</b>.
     * <p>
     * property: <b>webdriver.htmlunit.sslClientCertificatePassword</b><br>
     * type: {@code char[]}<br>
     * default: {@code null}<br>
     * see: {@link #SSL_CLIENT_CERTIFICATE_STORE}<br>
     * see: {@link #SSL_CLIENT_CERTIFICATE_TYPE}
     */
    SSL_CLIENT_CERTIFICATE_PASSWORD(optSslClientCertificatePassword, char[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefaultValue(value)) {
                throw new UnsupportedOperationException(
                        "SSL client certificate key store password cannot be set as a discrete value; "
                        + "use HtmlUnitDriverOptions.setSSLClientCertificateStore() instead");
            }
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getSSLClientCertificatePassword();
        }
    },

    /**
     * The SSL server certificate trust store. All server certificates will be validated against
     * this trust store.
     * <p>
     * property: <b>webdriver.htmlunit.sslTrustStore</b><br>
     * type: {@link KeyStore}<br>
     * default: {@code null}<br>
     * see: {@link #SSL_TRUST_STORE_TYPE}<br>
     * see: {@link #SSL_TRUST_STORE_PASSWORD}
     */
    SSL_TRUST_STORE(optSslTrustStore, KeyStore.class, null) {
        @Override
        public Object encode(final Object value) {
            return null;
        }

        @Override
        public void insert(final WebClientOptions options, final Object value) {
            try {
                final KeyStoreBean bean = TypeCodec.decodeKeyStore(value);
                options.setSSLTrustStore(bean.createUrl(), bean.getPassword(), bean.getType());
            }
            catch (final MalformedURLException e) {
                throw new IllegalArgumentException(
                        "Specified SSL_TRUST_STORE URL is malformed", e);
            }
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getSSLTrustStore();
        }
    },

    /**
     * Type of the specified SSL trust <b>KeyStore</b> (e.g. - {@code jks} or {@code pkcs12}).
     * <p>
     * property: <b>webdriver.htmlunit.sslTrustStoreType</b><br>
     * type: {@link char[]}<br>
     * default: {@code null}<br>
     * see: {@link #SSL_TRUST_STORE}<br>
     * see: {@link #SSL_TRUST_STORE_PASSWORD}<br>
     * see: {@link java.security.Security#getProviders() Security.getProviders}
     */
    SSL_TRUST_STORE_TYPE(optSslTrustStoreType, char[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefaultValue(value)) {
                throw new UnsupportedOperationException(
                        "SSL trust key store type cannot be set as a discrete value; "
                        + "use HtmlUnitDriverOptions.setSSLTrustStore() instead");
            }
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            final KeyStore keyStore = options.getSSLTrustStore();
            return (keyStore != null) ? keyStore.getType() : null;
        }
    },

    /**
     * Password for the specified SSL trust <b>KeyStore</b>.
     * <p>
     * property: <b>webdriver.htmlunit.sslTrustStorePassword</b><br>
     * type: {@code char[]}<br>
     * default: {@code null}<br>
     * see: {@link #SSL_TRUST_STORE}<br>
     * see: {@link #SSL_TRUST_STORE_TYPE}
     */
    SSL_TRUST_STORE_PASSWORD(optSslTrustStorePassword, char[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefaultValue(value)) {
                throw new UnsupportedOperationException(
                        "SSL trust key store password cannot be set as a discrete value; "
                        + "use HtmlUnitDriverOptions.setSSLTrustStore() instead");
            }
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            throw new UnsupportedOperationException("SSL trust store password cannot be retrieved");
        }
    },

    /**
     * Sets the protocol versions enabled for use on SSL connections.
     * <p>
     * property: <b>webdriver.htmlunit.sslClientProtocols</b><br>
     * type: {@code String[]}<br>
     * default: {@code null} (use default protocols)<br>
     * see: {@link javax.net.ssl.SSLSocket#setEnabledProtocols(String[]) SSLSocket.setEnabledProtocols}
     */
    SSL_CLIENT_PROTOCOLS(optSslClientProtocols, String[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setSSLClientProtocols(TypeCodec.decodeStringArray(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getSSLClientProtocols();
        }
    },

    /**
     * Sets the cipher suites enabled for use on SSL connections.
     * <p>
     * property: <b>webdriver.htmlunit.sslClientCipherSuites</b><br>
     * type: {@code String[]}<br>
     * default: {@code null} (use default cipher suites)<br>
     * see: {@link javax.net.ssl.SSLSocket#setEnabledCipherSuites(String[]) SSLSocket.setEnabledCipherSuites}
     */
    SSL_CLIENT_CIPHER_SUITES(optSslClientCipherSuites, String[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setSSLClientCipherSuites(TypeCodec.decodeStringArray(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getSSLClientCipherSuites();
        }
    },

    /**
     * Enables/disables geo-location support.
     * <p>
     * property: <b>webdriver.htmlunit.geolocationEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code false}
     */
    GEOLOCATION_ENABLED(optGeolocationEnabled, boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setGeolocationEnabled(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isGeolocationEnabled();
        }
    },

    /**
     * Enables/disables "Do Not Track" support.
     * <p>
     * property: <b>webdriver.htmlunit.doNotTrackEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code false}
     */
    DO_NOT_TRACK_ENABLED(optDoNotTrackEnabled, boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setDoNotTrackEnabled(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isDoNotTrackEnabled();
        }
    },

    /**
     * Sets the client's home page.
     * <p>
     * property: <b>webdriver.htmlunit.homePage</b><br>
     * type: {@link String}<br>
     * default: "https://www.htmlunit.org/"
     */
    HOME_PAGE(optHomePage, String.class, "https://www.htmlunit.org/") {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setHomePage(TypeCodec.decodeString(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getHomePage();
        }
    },

    /**
     * Sets the proxy configuration for this client.
     * <p>
     * property: <b>webdriver.htmlunit.proxyConfig</b><br>
     * type: {@link ProxyConfig}<br>
     * default: {@code null}
     */
    PROXY_CONFIG(optProxyConfig, ProxyConfig.class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setProxyConfig(TypeCodec.decodeProxyConfig(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getProxyConfig();
        }
    },

    /**
     * Sets the timeout of the {@link WebConnection}. Set to zero for an infinite wait.
     * <p>
     * <b>NOTE</b>: The timeout is used twice. The first is for making the socket connection, the second is
     * for data retrieval. If the time is critical you must allow for twice the time specified here.
     * <p>
     * property: <b>webdriver.htmlunit.timeout</b><br>
     * type: {@code int}<br>
     * default: 90_000 (1.5 minutes in milliseconds)
     */
    TIMEOUT(optTimeout, int.class, 90_000) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setTimeout(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getTimeout();
        }
    },

    /**
     * Sets the {@code connTimeToLive} (in milliseconds) of the {@link org.apache.http.client.HttpClient HttpClient}
     * connection pool. Use this if you are working with web pages behind a DNS based load balancer.
     * <p>
     * property: <b>webdriver.htmlunit.connectionTimeToLive</b><br>
     * type: {@code long}<br>
     * default: -1 (use HTTP default)
     */
    CONNECTION_TIME_TO_LIVE(optConnectionTimeToLive, long.class, -1L) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setConnectionTimeToLive(TypeCodec.decodeLong(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getConnectionTimeToLive();
        }
    },

    /**
     * If set to {@code true}, the client will accept connections to any host, regardless of
     * whether they have valid certificates or not. This is especially useful when you are trying to
     * connect to a server with expired or corrupt certificates.
     * <p>
     * property: <b>webdriver.htmlunit.useInsecureSSL</b><br>
     * type: {@code boolean}<br>
     * default: {@code false}
     */
    USE_INSECURE_SSL(optUseInsecureSSL, boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setUseInsecureSSL(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isUseInsecureSSL();
        }
    },

    /**
     * Sets the SSL protocol, used only when {@link #USE_INSECURE_SSL} is set to {@code true}.
     * <p>
     * property: <b>webdriver.htmlunit.sslInsecureProtocol</b><br>
     * type: {@link String}<br>
     * default: {@code null} (use default protocol: SSL)<br>
     * see: <a href="https://docs.oracle.com/en/java/javase/19/docs/specs/security/standard-names.html#sslcontext-algorithms">
     * {@code SSLContext} Algorithms</a>
     */
    SSL_INSECURE_PROTOCOL(optSslInsecureProtocol, String.class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setSSLInsecureProtocol(TypeCodec.decodeString(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getSSLInsecureProtocol();
        }
    },

    /**
     * Sets the maximum bytes to have in memory, after which the content is saved to a temporary file.<br>
     * <b>NOTE</b>: Set this to zero or -1 to deactivate the saving at all.
     * <p>
     * property: <b>webdriver.htmlunit.maxInMemory</b><br>
     * type: {@code int}<br>
     * default: 500 * 1024
     */
    MAX_IN_MEMORY(optMaxInMemory, int.class, 500 * 1024) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setMaxInMemory(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getMaxInMemory();
        }
    },

    /**
     * Sets the maximum number of {@link Page pages} to cache in history. <b>HtmlUnit</b>
     * uses {@code SoftReference<Page>} for storing the pages that are part of the history.
     * If you like to fine tune this you can use {@link #HISTORY_PAGE_CACHE_LIMIT} to limit
     * the number of page references stored by the history.
     * <p>
     * property: <b>webdriver.htmlunit.historySizeLimit</b><br>
     * type: {@code int}<br>
     * default: 50<br>
     * see: {@link java.lang.ref.SoftReference SoftReference}
     */
    HISTORY_SIZE_LIMIT(optHistorySizeLimit, int.class, 50) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setHistorySizeLimit(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getHistorySizeLimit();
        }
    },

    /**
     * Sets the maximum number of {@link Page pages} to cache in history. If this value
     * is smaller than {@link #HISTORY_SIZE_LIMIT}, <b>HtmlUnit</b> will only use soft
     * references for the first <b>HISTORY_PAGE_CACHE_LIMIT</b> entries in the history.
     * For older entries, only the URL is saved; the page will be (re)retrieved on demand.
     * <p>
     * property: <b>webdriver.htmlunit.historyPageCacheLimit</b><br>
     * type: {@code int}<br>
     * default: {@link Integer#MAX_VALUE}
     */
    HISTORY_PAGE_CACHE_LIMIT(optHistoryPageCacheLimit, int.class, Integer.MAX_VALUE) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setHistoryPageCacheLimit(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getHistoryPageCacheLimit();
        }
    },

    /**
     * Sets the local address to be used for request execution.
     * <p>
     * On machines with multiple network interfaces, this parameter can be used to
     * select the network interface from which the connection originates.
     * <p>
     * property: <b>webdriver.htmlunit.localAddress</b><br>
     * type: {@link InetAddress}<br>
     * default: {@code null} (use default 'localhost')
     */
    LOCAL_ADDRESS(optLocalAddress, InetAddress.class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setLocalAddress(TypeCodec.decodeInetAddress(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getLocalAddress();
        }
    },

    /**
     * Sets whether or not to automatically download images.
     * <p>
     * property: <b>webdriver.htmlunit.downloadImages</b><br>
     * type: {@code boolean}<br>
     * default: {@code false}
     */
    DOWNLOAD_IMAGES(optDownloadImages, boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setDownloadImages(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isDownloadImages();
        }
    },

    /**
     * Sets the screen width.
     * <p>
     * property: <b>webdriver.htmlunit.screenWidth</b><br>
     * type: {@code int}<br>
     * default: 1920
     */
    SCREEN_WIDTH(optScreenWidth, int.class, 1920) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setScreenWidth(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getScreenWidth();
        }
    },

    /**
     * Sets the screen height.
     * <p>
     * property: <b>webdriver.htmlunit.screenHeight</b><br>
     * type: {@code int}<br>
     * default: 1080
     */
    SCREEN_HEIGHT(optScreenHeight, int.class, 1080) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setScreenHeight(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getScreenHeight();
        }
    },

    /**
     * Enables/disables WebSocket support.
     * <p>
     * property: <b>webdriver.htmlunit.webSocketEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code true}
     */
    WEB_SOCKET_ENABLED(optWebSocketEnabled, boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketEnabled(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isWebSocketEnabled();
        }
    },

    /**
     * Sets the WebSocket {@code maxTextMessageSize}.
     * <p>
     * property: <b>webdriver.htmlunit.webSocketMaxTextMessageSize</b><br>
     * type: {@code int}<br>
     * default: -1 {use default size)
     */
    WEB_SOCKET_MAX_TEXT_MESSAGE_SIZE(optWebSocketMaxTextMessageSize, int.class, -1) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketMaxTextMessageSize(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getWebSocketMaxTextMessageSize();
        }
    },

    /**
     * Sets the WebSocket {@code maxTextMessageBufferSize}.
     * <p>
     * property: <b>webdriver.htmlunit.webSocketMaxTextMessageBufferSize</b><br>
     * type: {@code int}<br>
     * default: -1 {use default size)
     */
    WEB_SOCKET_MAX_TEXT_MESSAGE_BUFFER_SIZE(optWebSocketMaxTextMessageBufferSize, int.class, -1) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketMaxTextMessageBufferSize(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getWebSocketMaxTextMessageBufferSize();
        }
    },

    /**
     * Sets the WebSocket {@code maxBinaryMessageSize}.
     * <p>
     * property: <b>webdriver.htmlunit.webSocketMaxBinaryMessageSize</b><br>
     * type: {@code int}<br>
     * default: -1 {use default size)
     */
    WEB_SOCKET_MAX_BINARY_MESSAGE_SIZE(optWebSocketMaxBinaryMessageSize, int.class, -1) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketMaxBinaryMessageSize(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getWebSocketMaxBinaryMessageSize();
        }
    },

    /**
     * Sets the WebSocket {@code maxBinaryMessageBufferSize}.
     * <p>
     * property: <b>webdriver.htmlunit.webSocketMaxBinaryMessageBufferSize</b><br>
     * type: {@code int}<br>
     * default: -1 {use default size)
     */
    WEB_SOCKET_MAX_BINARY_MESSAGE_BUFFER_SIZE(optWebSocketMaxBinaryMessageBufferSize, int.class, -1) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketMaxBinaryMessageBufferSize(TypeCodec.decodeInt(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getWebSocketMaxBinaryMessageBufferSize();
        }
    },

    /**
     * Sets whether or not fetch polyfill should be used.
     * <p>
     * property: <b>webdriver.htmlunit.fetchPolyfillEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code false}
     */
    FETCH_POLYFILL_ENABLED(optFetchPolyfillEnabled, boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setFetchPolyfillEnabled(TypeCodec.decodeBoolean(value));
        }

        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isFetchPolyfillEnabled();
        }
    };

    public final String key;
    public final String name;
    public final Class<?> type;
    public final Object initial;

    HtmlUnitOption(final String key, final Class<?> type, final Object initial) {
        this.key = key;
        this.name = "webdriver.htmlunit." + key;
        this.type = type;
        this.initial = initial;
    }

    @Override
    public String getCapabilityKey() {
        return key;
    }

    @Override
    public String getPropertyName() {
        return name;
    }

    @Override
    public Class<?> getOptionType() {
        return type;
    }

    @Override
    public Object getDefaultValue() {
        return initial;
    }

    /**
     * Determine if the specified value matches the default for this option.
     *
     * @param value value to be evaluated
     * @return {@code true} if specified value matches the default value; otherwise {@code false}
     */
    @Override
    public boolean isDefaultValue(final Object value) {
        if (initial == null) {
            return value == null;
        }
        if (value == null) {
            return false;
        }
        return value.equals(initial);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyPropertyTo(final Map<String, Object> optionsMap) {
        final String value = System.getProperty(name);
        if (value != null) {
            optionsMap.put(key, decode(value));
            System.clearProperty(key);
        }
    }

    /**
     * Encode the specified value according to the type of this option.
     *
     * @param value value to be encoded
     * @return option-specific encoding for specified value
     */
    @Override
    public Object encode(final Object value) {
        switch (type.getName()) {
            case "boolean":
            case "int":
            case "long":
            case "java.lang.String":
            case "[C":
            case "[Ljava.lang.String;":
                return value;
            case "java.io.File":
                return TypeCodec.encodeFile(value);
            case "java.net.InetAddress":
                return TypeCodec.encodeInetAddress(value);
            case "org.htmlunit.ProxyConfig":
                return TypeCodec.encodeProxyConfig(value);
            case "org.htmlunit.BrowserVersion":
                return TypeCodec.encodeBrowserVersion(value);
        }
        throw new IllegalStateException(
                String.format("Unsupported type '%s' specified for option [%s]; value is of type: %s",
                this.type.getName(), this.toString(), TypeCodec.getClassName(value)));
    }

    /**
     * Decode the specified value according to the type of this option.
     *
     * @param value value to be decoded
     * @return option-specific decoding for specified value
     */
    @Override
    public Object decode(final Object value) {
        switch (this.type.getName()) {
            case "boolean":
                return TypeCodec.decodeBoolean(value);
            case "int":
                return TypeCodec.decodeInt(value);
            case "long":
                return TypeCodec.decodeLong(value);
            case "java.lang.String":
                return TypeCodec.decodeString(value);
            case "[C":
                return TypeCodec.decodeCharArray(value);
            case "[Ljava.lang.String;":
                return TypeCodec.decodeStringArray(value);
            case "java.io.File":
                return TypeCodec.decodeFile(value);
            case "java.net.InetAddress":
                return TypeCodec.decodeInetAddress(value);
            case "java.security.KeyStore":
                return TypeCodec.decodeKeyStore(value);
            case "org.htmlunit.ProxyConfig":
                return TypeCodec.decodeProxyConfig(value);
            case "org.htmlunit.BrowserVersion":
                return TypeCodec.decodeBrowserVersion(value);
        }
        throw new IllegalStateException(
                String.format("Unsupported type '%s' specified for option [%s]; value is of type: %s",
                this.type.getName(), this.toString(), TypeCodec.getClassName(value)));
    }

    /**
     * Insert the specified value for this option into the provided web client options object.
     *
     * @param options {@link WebClientOptions} object
     * @param value value to be inserted
     */
    public void insert(final WebClientOptions options, final Object value) {
        throw new UnsupportedOperationException(
                String.format("Option '%s' does not support value insertion", this.toString()));
    }

    /**
     * Obtain the value for this option from the specified web client options object.
     *
     * @param options {@link WebClientOptions} object
     * @return value for this option
     */
    public Object obtain(final WebClientOptions options) {
        return null;
    }

    public static HtmlUnitOption fromCapabilityKey(final String key) {
        for (final HtmlUnitOption option : HtmlUnitOption.values()) {
            if (option.key.equals(key)) {
                return option;
            }
        }
        return null;
    }

    public static HtmlUnitOption fromPropertyName(final String name) {
        for (final HtmlUnitOption option : HtmlUnitOption.values()) {
            if (option.name.equals(name)) {
                return option;
            }
        }
        return null;
    }
}
