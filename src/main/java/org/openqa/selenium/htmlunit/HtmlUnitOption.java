package org.openqa.selenium.htmlunit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.htmlunit.BrowserVersion;
import org.htmlunit.BrowserVersion.BrowserVersionBuilder;
import org.htmlunit.Page;
import org.htmlunit.ProxyConfig;
import org.htmlunit.WebClientOptions;
import org.htmlunit.WebConnection;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.TypeToken;

public enum HtmlUnitOption {
    WEB_CLIENT_VERSION("webClientVersion", BrowserVersion.class, BrowserVersion.BEST_SUPPORTED),
    
    /**
     * Enables/disables JavaScript support.
     * <p>
     * property: <b>webdriver.htmlunit.javaScriptEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code true}
     */
    JAVASCRIPT_ENABLED("javaScriptEnabled", boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setJavaScriptEnabled(decodeBoolean(value));
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
    CSS_ENABLED("cssEnabled", boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setCssEnabled(decodeBoolean(value));
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
    PRINT_CONTENT_ON_FAILING_STATUS_CODE("printContentOnFailingStatusCode", boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setPrintContentOnFailingStatusCode(decodeBoolean(value));
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
    THROW_EXCEPTION_ON_FAILING_STATUS_CODE("throwExceptionOnFailingStatusCode", boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setThrowExceptionOnFailingStatusCode(decodeBoolean(value));
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
    THROW_EXCEPTION_ON_SCRIPT_ERROR("throwExceptionOnScriptError", boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setThrowExceptionOnScriptError(decodeBoolean(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isThrowExceptionOnScriptError();
        }
    },
    
    /**
     * Enables/disables applet support.<br>
     * <b>NOTE</b>: Applet support is experimental and minimal.
     * <p>
     * property: <b>webdriver.htmlunit.appletEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code false}
     */
    APPLET_ENABLED("appletEnabled", boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setAppletEnabled(decodeBoolean(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isAppletEnabled();
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
    POPUP_BLOCKER_ENABLED("popupBlockerEnabled", boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setPopupBlockerEnabled(decodeBoolean(value));
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
    IS_REDIRECT_ENABLED("isRedirectEnabled", boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setRedirectEnabled(decodeBoolean(value));
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
    TEMP_FILE_DIRECTORY("tempFileDirectory", File.class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            try {
                options.setTempFileDirectory(decodeFile(value));
            } catch (IOException e) {
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
    SSL_CLIENT_CERTIFICATE_STORE("sslClientCertificateStore", KeyStore.class, null) {
        @Override
        public Object encode(final Object value) {
            return null;
        }
        
        @Override
        public Object decode(final Object value) {
            if (value instanceof KeyStore) {
                return (KeyStore) value;
            }
            
            if (value instanceof String) {
                URL storeUrl;
                
                try {
                    storeUrl = new URL((String) value);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(
                            "Specified SSL_CLIENT_CERTIFICATE_STORE URL is malformed", e);
                }
                
                String storeType = Objects.requireNonNull(
                        System.getProperty(SSL_CLIENT_CERTIFICATE_TYPE.name),
                        "Required system property 'webdriver.htmlunit.sslClientCertificateStoreType' is undefined");
                
                String password = System.getProperty(SSL_CLIENT_CERTIFICATE_PASSWORD.name);
                
                WebClientOptions options = new WebClientOptions();
                options.setSSLClientCertificateKeyStore(storeUrl, password, storeType);
                return options.getSSLClientCertificateStore();
            }
            
            throw new IllegalStateException("Specified value must be 'KeyStore' or 'String'; was " + getClassName(value));
        }
        
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefault(value)) {
                throw new UnsupportedOperationException(
                        "SSL client certificate key store cannot be set as a discrete value; "
                        + "use HtmlUnitDriverOptions.setSSLClientCertificateStore() instead");
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
    SSL_CLIENT_CERTIFICATE_TYPE("sslClientCertificateType", char[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefault(value)) {
                throw new UnsupportedOperationException(
                        "SSL client certificate key store type cannot be set as a discrete value; "
                        + "use HtmlUnitDriverOptions.setSSLClientCertificateStore() instead");
            }
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            KeyStore keyStore = options.getSSLClientCertificateStore();
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
    SSL_CLIENT_CERTIFICATE_PASSWORD("sslClientCertificatePassword", char[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefault(value)) {
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
    SSL_TRUST_STORE("sslTrustStore", KeyStore.class, null) {
        @Override
        public Object encode(final Object value) {
            return null;
        }
        
        @Override
        public Object decode(final Object value) {
            if (value instanceof KeyStore) {
                return (KeyStore) value;
            }
            
            if (value instanceof String) {
                URL storeUrl;
                
                try {
                    storeUrl = new URL((String) value);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(
                            "Specified SSL_TRUST_STORE URL is malformed", e);
                }
                
                String storeType = Objects.requireNonNull(
                        System.getProperty(SSL_TRUST_STORE_TYPE.name),
                        "Required system property 'webdriver.htmlunit.sslTrustStoreType' is undefined");
                
                String password = System.getProperty(SSL_TRUST_STORE_PASSWORD.name);
                
                WebClientOptions options = new WebClientOptions();
                options.setSSLClientCertificateKeyStore(storeUrl, password, storeType);
                return options.getSSLClientCertificateStore();
            }
            
            throw new IllegalStateException("Specified value must be 'KeyStore' or 'String'; was " + getClassName(value));
        }
        
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefault(value)) {
                throw new UnsupportedOperationException(
                        "SSL trust key store cannot be set as a discrete value; "
                        + "use HtmlUnitDriverOptions.setSSLTrustStore() instead");
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
    SSL_TRUST_STORE_TYPE("sslTrustStoreType", char[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefault(value)) {
                throw new UnsupportedOperationException(
                        "SSL trust key store type cannot be set as a discrete value; "
                        + "use HtmlUnitDriverOptions.setSSLTrustStore() instead");
            }
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            KeyStore keyStore = options.getSSLTrustStore();
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
    SSL_TRUST_STORE_PASSWORD("sslTrustStorePassword", char[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            if (!isDefault(value)) {
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
    SSL_CLIENT_PROTOCOLS("sslClientProtocols", String[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setSSLClientProtocols(decodeStringArray(value));
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
    SSL_CLIENT_CIPHER_SUITES("sslClientCipherSuites", String[].class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setSSLClientCipherSuites(decodeStringArray(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getSSLClientCipherSuites();
        }
    },
    
    /**
     * Enables/disables Geolocation support.
     * <p>
     * property: <b>webdriver.htmlunit.geolocationEnabled</b><br>
     * type: {@code boolean}<br>
     * default: {@code false}
     */
    GEOLOCATION_ENABLED("geolocationEnabled", boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setGeolocationEnabled(decodeBoolean(value));
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
    DO_NOT_TRACK_ENABLED("doNotTrackEnabled", boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setDoNotTrackEnabled(decodeBoolean(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isDoNotTrackEnabled();
        }
    },
    
    /**
     * Sets the client's homepage.
     * <p>
     * property: <b>webdriver.htmlunit.homePage</b><br>
     * type: {@link String}<br>
     * default: "https://www.htmlunit.org/"
     */
    HOME_PAGE("homePage", String.class, "https://www.htmlunit.org/") {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setHomePage(decodeString(value));
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
    PROXY_CONFIG("proxyConfig", ProxyConfig.class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setProxyConfig(decodeProxyConfig(value));
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
    TIMEOUT("timeout", int.class, 90_000) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setTimeout(decodeInt(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getTimeout();
        }
    },
    
    /**
     * Sets the {@code connTimeToLive} of the {@link org.apache.http.client.HttpClient HttpClient}
     * connection pool. Use this if you are working with web pages behind a DNS based load balancer.
     * <p>
     * property: <b>webdriver.htmlunit.connectionTimeToLive</b><br>
     * type: {@code long}<br>
     * default: -1 (use HTTP default)
     */
    CONNECTION_TIME_TO_LIVE("connectionTimeToLive", long.class, -1L) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setConnectionTimeToLive(decodeLong(value));
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
    USE_INSECURE_SSL("useInsecureSSL", boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setUseInsecureSSL(decodeBoolean(value));
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
    SSL_INSECURE_PROTOCOL("sslInsecureProtocol", String.class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setSSLInsecureProtocol(decodeString(value));
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
    MAX_IN_MEMORY("maxInMemory", int.class, 500 * 1024) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setMaxInMemory(decodeInt(value));
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
    HISTORY_SIZE_LIMIT("historySizeLimit", int.class, 50) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setHistorySizeLimit(decodeInt(value));
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
    HISTORY_PAGE_CACHE_LIMIT("historyPageCacheLimit", int.class, Integer.MAX_VALUE) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setHistoryPageCacheLimit(decodeInt(value));
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
    LOCAL_ADDRESS("localAddress", InetAddress.class, null) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setLocalAddress(decodeInetAddress(value));
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
    DOWNLOAD_IMAGES("downloadImages", boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setDownloadImages(decodeBoolean(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isDownloadImages();
        }
    },
    
    /**
     * Set the screen width.
     * <p>
     * property: <b>webdriver.htmlunit.screenWidth</b><br>
     * type: {@code int}<br>
     * default: 1920
     */
    SCREEN_WIDTH("screenWidth", int.class, 1920) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setScreenWidth(decodeInt(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getScreenWidth();
        }
    },
    
    /**
     * Set the screen height.
     * <p>
     * property: <b>webdriver.htmlunit.screenHeight</b><br>
     * type: {@code int}<br>
     * default: 1080
     */
    SCREEN_HEIGHT("screenHeight", int.class, 1080) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setScreenHeight(decodeInt(value));
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
    WEB_SOCKET_ENABLED("webSocketEnabled", boolean.class, true) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketEnabled(decodeBoolean(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.isWebSocketEnabled();
        }
    },
    
    /**
     * Sets the WebSocket maxTextMessageSize.
     * <p>
     * property: <b>webdriver.htmlunit.webSocketMaxTextMessageSize</b><br>
     * type: {@code int}<br>
     * default: -1 {use default size)
     */
    WEB_SOCKET_MAX_TEXT_MESSAGE_SIZE("webSocketMaxTextMessageSize", int.class, -1) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketMaxTextMessageSize(decodeInt(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getWebSocketMaxTextMessageSize();
        }
    },
    
    /**
     * Sets the WebSocket maxTextMessageBufferSize.
     * <p>
     * property: <b>webdriver.htmlunit.webSocketMaxTextMessageBufferSize</b><br>
     * type: {@code int}<br>
     * default: -1 {use default size)
     */
    WEB_SOCKET_MAX_TEXT_MESSAGE_BUFFER_SIZE("webSocketMaxTextMessageBufferSize", int.class, -1) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketMaxTextMessageBufferSize(decodeInt(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getWebSocketMaxTextMessageBufferSize();
        }
    },
    
    /**
     * Sets the WebSocket maxBinaryMessageSize.
     * <p>
     * property: <b>webdriver.htmlunit.webSocketMaxBinaryMessageSize</b><br>
     * type: {@code int}<br>
     * default: -1 {use default size)
     */
    WEB_SOCKET_MAX_BINARY_MESSAGE_SIZE("webSocketMaxBinaryMessageSize", int.class, -1) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketMaxBinaryMessageSize(decodeInt(value));
        }
        
        @Override
        public Object obtain(final WebClientOptions options) {
            return options.getWebSocketMaxBinaryMessageSize();
        }
    },
    
    /**
     * Sets the WebSocket maxBinaryMessageBufferSize.
     * <p>
     * property: <b>webdriver.htmlunit.webSocketMaxBinaryMessageBufferSize</b><br>
     * type: {@code int}<br>
     * default: -1 {use default size)
     */
    WEB_SOCKET_MAX_BINARY_MESSAGE_BUFFER_SIZE("webSocketMaxBinaryMessageBufferSize", int.class, -1) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setWebSocketMaxBinaryMessageBufferSize(decodeInt(value));
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
    FETCH_POLYFILL_ENABLED("fetchPolyfillEnabled", boolean.class, false) {
        @Override
        public void insert(final WebClientOptions options, final Object value) {
            options.setFetchPolyfillEnabled(decodeBoolean(value));
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
    
    /** Specifier for {@code Map<String, Object>} input/output type */
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    
    /** Specifier for {@code List<String>} input/output type */
    private static final Type LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    
    HtmlUnitOption(final String key, final Class<?> type, final Object initial) {
        this.key = key;
        this.name = "webdriver.htmlunit." + key;
        this.type = type;
        this.initial = initial;
    }
    
    public boolean isDefault(final Object value) {
        if (initial == null) return value == null;
        if (value == null) return false;
        return value.equals(initial);
    }
    
    public Object encode(final Object value) {
        switch (this.type.getName()) {
        case "boolean":
        case "int":
        case "long":
        case "java.lang.String":
        case "[C":
        case "[Ljava.lang.String;":
            return value;
        case "java.io.File":
            return encodeFile(value);
        case "java.net.InetAddress":
            return encodeInetAddress(value);
        case "org.htmlunit.ProxyConfig":
            return encodeProxyConfig(value);
        case "org.htmlunit.BrowserVersion":
            return encodeBrowserVersion(value);
        }
        throw new IllegalStateException(
                String.format("Unsupported type '%s' specified for option [%s]; value is of type: %s",
                this.type.getName(), this.toString(), getClassName(value)));
    }
    
    public Object decode(final Object value) {
        switch (this.type.getName()) {
        case "boolean":
            return decodeBoolean(value);
        case "int":
            return decodeInt(value);
        case "long":
            return decodeLong(value);
        case "java.lang.String":
            return decodeString(value);
        case "[C":
            return decodeCharArray(value);
        case "[Ljava.lang.String;":
            return decodeStringArray(value);
        case "java.io.File":
            return decodeFile(value);
        case "java.net.InetAddress":
            return decodeInetAddress(value);
        case "org.htmlunit.ProxyConfig":
            return decodeProxyConfig(value);
        case "org.htmlunit.BrowserVersion":
            return decodeBrowserVersion(value);
        }
        throw new IllegalStateException(
                String.format("Unsupported type '%s' specified for option [%s]; value is of type: %s",
                this.type.getName(), this.toString(), getClassName(value)));
    }
    
    public void insert(final WebClientOptions options, final Object value) {
        throw new UnsupportedOperationException(
                String.format("Option '%s' does not support value insertion", this.toString()));
    }
    
    public Object obtain(final WebClientOptions options) {
        return null;
    }
    
    private static boolean decodeBoolean(final Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        throw new IllegalStateException("Specified value must be 'Boolean' or 'String'; was " + getClassName(value));
    }
    
    private static int decodeInt(final Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        throw new IllegalStateException("Specified value must be 'Long', 'Integer', or 'String'; was " + getClassName(value));
    }
    
    private static long decodeLong(final Object value) {
        if (value instanceof Long) {
            return ((Long) value).longValue();
        }
        if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        throw new IllegalStateException("Specified value must be 'Long' or 'String'; was " + getClassName(value));
    }
    
    private static String decodeString(final Object value) {
        if (value == null) return null;
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalStateException("Specified value must be 'String'; was " + getClassName(value));
    }
    
    private static char[] decodeCharArray(final Object value) {
        if (value == null) return null;
        if (value instanceof char[]) {
            return (char[]) value;
        }
        if (value instanceof String) {
            return ((String) value).toCharArray();
        }
        throw new IllegalStateException("Specified value must be 'char[]' or 'String'; was " + getClassName(value));
    }
    
    private static String[] decodeStringArray(final Object value) {
        if (value == null) return null;
        if (value instanceof String[]) {
            return (String[]) value;
        }
        if (value instanceof String) {
            List<String> listOfStrings = new Json().toType((String) value, LIST_TYPE);
            return listOfStrings.toArray(new String[0]);
        }
        throw new IllegalStateException("Specified value must be 'String[]' or 'String'; was " + getClassName(value));
    }
    
    private static String encodeFile(final Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof File) {
            try {
                return ((File) value).getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalStateException("Failed encoding 'File' to canonical path", e);
            }
        }
        throw new IllegalStateException("Specified value must be 'File' or 'String'; was " + getClassName(value));
    }
    
    private static File decodeFile(final Object value) {
        if (value instanceof File) {
            return (File) value;
        }
        if (value instanceof String) {
            return new File((String) value);
        }
        throw new IllegalStateException("Specified value must be 'File' or 'String'; was " + getClassName(value));
    }
    
    private static String encodeInetAddress(final Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof InetAddress) {
            return ((InetAddress) value).getHostAddress();
        }
        throw new IllegalStateException("Specified value must be 'InetAddress' or 'String'; was " + getClassName(value));
    }
    
    private static InetAddress decodeInetAddress(final Object value) {
        if (value instanceof InetAddress) {
            return (InetAddress) value;
        }
        if (value instanceof String) {
            try {
                return InetAddress.getByName((String) value);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Failed decoding address: " + ((String) value), e);
            }
        }
        throw new IllegalStateException("Specified value must be 'InetAddress' or 'String'; was " + getClassName(value));
    }
    
    private static Map<String, Object> encodeProxyConfig(final Object value) {
        if (value instanceof ProxyConfig) {
            Map<String, Object> configMap = new HashMap<>();
            ProxyConfig proxyConfig = (ProxyConfig) value;
            for (ProxyConfigOption option : ProxyConfigOption.values()) {
                Object optionValue = option.obtain(proxyConfig);
                configMap.put(option.key, option.encode(optionValue));
            }
            return configMap;
        }
        throw new IllegalStateException("Specified value must be 'ProxyConfig'; was " + getClassName(value));
    }
    
    @SuppressWarnings("unchecked")
    private static ProxyConfig decodeProxyConfig(final Object value) {
        Map<String, Object> configMap;

        if (value instanceof ProxyConfig) {
            return (ProxyConfig) value;
        } else if (value instanceof Map) {
            configMap = (Map<String, Object>) value;
        } else if (value instanceof String) {
            configMap = new Json().toType((String) value, MAP_TYPE);
        } else {
            throw new IllegalStateException("Specified value must be 'ProxyConfig', 'Map', or 'String'; was " + getClassName(value));
        }
        
        ProxyConfig config = new ProxyConfig();
        for (ProxyConfigOption option : ProxyConfigOption.values()) {
            option.insert(config, configMap.get(option.key));
        }
        
        return config;
    }

    private static Map<String, Object> encodeBrowserVersion(final Object value) {
        if (value instanceof BrowserVersion) {
            Map<String, Object> optionsMap = new HashMap<>();
            BrowserVersion browserVersion = (BrowserVersion) value;
            for (BrowserVersionTrait trait : BrowserVersionTrait.values()) {
                Object traitValue = trait.obtain(browserVersion);
                if (!trait.isDefault(traitValue)) {
                    optionsMap.put(trait.key, trait.encode(traitValue));
                }
            }
            return optionsMap;
        }
        throw new IllegalStateException("Specified value must be 'BrowserVersion'; was " + getClassName(value));
    }
    
    @SuppressWarnings({ "deprecation", "unchecked" })
    private static BrowserVersion decodeBrowserVersion(final Object value) {
        int code;
        String name;
        BrowserVersion seed;
        Map<String, Object> optionsMap;
        
        if (value instanceof BrowserVersion) {
            return (BrowserVersion) value;
        } else if (value instanceof Map) {
            optionsMap = (Map<String, Object>) value;
        } else if (value instanceof String) {
            optionsMap = new Json().toType((String) value, MAP_TYPE);
        } else {
            throw new IllegalStateException("Specified value must be 'BrowserVersion', 'Map', or 'String'; was " + getClassName(value));
        }
        
        Object numericCode = Objects.requireNonNull(optionsMap.get(BrowserVersionTrait.NUMERIC_CODE.key), 
                "Required browser version trait [numericCode] is unspecified");
        if (numericCode instanceof Long) {
            code = ((Long) numericCode).intValue();
        } else if (numericCode instanceof Integer) {
            code = ((Integer) numericCode).intValue();
        } else {
            throw new IllegalStateException("Browser numeric code must be 'Long' or 'Integer'; was " + getClassName(numericCode));
        }
        
        Object nickname = optionsMap.getOrDefault(BrowserVersionTrait.NICKNAME.key, "");
        if (nickname instanceof String) {
            name = (String) nickname;
        } else {
            throw new IllegalStateException("Browser nickname must be 'String'; was " + getClassName(nickname));
        }
        
        if (name.startsWith("Chrome")) {
            seed = BrowserVersion.CHROME;
        } else if (name.startsWith("Edge")) {
            seed = BrowserVersion.EDGE;
        } else if (name.startsWith("FF")) {
            seed = (code == 115) ? BrowserVersion.FIREFOX_ESR : BrowserVersion.FIREFOX;
        } else if (name.startsWith("IE")) {
            seed = BrowserVersion.INTERNET_EXPLORER;
        } else {
            throw new IllegalArgumentException("Browser nickname must start with 'Chrome', 'Edge', 'FF', or 'IE'; was: " + name);
        }
        
        if (seed.getBrowserVersionNumeric() != code) {
            try {
                Field browserVersionNumeric_ = BrowserVersion.class.getField("browserVersionNumeric_");
                browserVersionNumeric_.setAccessible(true);
                browserVersionNumeric_.set(seed, code);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // nothing to do here
            }
        }
        
        BrowserVersionBuilder builder = new BrowserVersionBuilder(seed);
        for (BrowserVersionTrait trait : BrowserVersionTrait.values()) {
            switch (trait) {
            case NUMERIC_CODE:
            case NICKNAME:
                continue;
            default:
                if (optionsMap.containsKey(trait.key)) {
                    trait.insert(builder, optionsMap.get(trait.key));
                }
            }
        }
        
        return builder.build();
    }

    private static String getClassName(final Object value) {
        return (value != null) ? value.getClass().getName() : "'null'";
    }
}
