// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit.options;

import static org.openqa.selenium.htmlunit.HtmlUnitDriver.DOWNLOAD_IMAGES_CAPABILITY;
import static org.openqa.selenium.htmlunit.HtmlUnitDriver.JAVASCRIPT_ENABLED;
import static org.openqa.selenium.remote.Browser.HTMLUNIT;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.htmlunit.BrowserVersion;
import org.htmlunit.ProxyConfig;
import org.htmlunit.WebClient;
import org.htmlunit.WebClientOptions;
import org.htmlunit.util.UrlUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.htmlunit.BrowserVersionDeterminer;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.CapabilityType;

/**
 * Class to manage options specific to {@link HtmlUnitDriver}.
 *
 * <p>Example usage:
 *
 * <pre><code>
 * HtmlUnitDriverOptions options = new HtmlUnitDriverOptions()
 *     .setWebClientVersion(BrowserVersion.FIREFOX_ESR)
 *     .setJavaScriptEnabled(true);
 *
 * // For use with HtmlUnitDriver:
 * HtmlUnitDriver driver = new HtmlUnitDriver(options);
 *
 * // For use with RemoteWebDriver:
 * RemoteWebDriver driver = new RemoteWebDriver(
 *     new URL("http://localhost:4444/"),
 *     new HtmlUnitDriverOptions());
 * </code></pre>
 *
 * <p>Getting/setting HtmlUnitDriver options:
 * <p>
 * In addition to methods for reading and writing specific <b>HtmlUnitDriver</b> options, you can use
 * the standard {@link MutableCapabilities} API:
 * <ul>
 *     <li>{@link #is(String)}</li>
 *     <li>{@link #getCapability(String)}</li>
 *     <li>{@link #setCapability(String, Object)}</li>
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre><code>
 * HtmlUnitDriverOptions options = new HtmlUnitDriverOptions();
 * boolean popupBlockerEnabled = options.is(HtmlUnitOption.optPopupBlockerEnabled);
 * // NOTE: See "Getting individual browser version traits" below
 * String  browserLanguage = (String) options.getCapability(BrowserVersionTrait.optBrowserLanguage);
 * options.setCapability(HtmlUnitOption.optGeolocationEnabled, true);
 * </code></pre>
 *
 * <p>Getting individual browser version traits:
 * <p>
 * <b>HtmlUnitDriverOption</b> contains a {@link BrowserVersion} which can be read and written directly:
 * <ul>
 *     <li>{@link #getWebClientVersion()}</li>
 *     <li>{@link #setWebClientVersion(BrowserVersion)}</li>
 * </ul>
 * The individual traits of the <b>BrowserVersion</b> object can be read directly as well via the standard
 * {@link Capabilities} API. For example:
 *
 * <pre><code>
 * HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.EDGE);
 * // System time zone accessed via BrowserVersion API
 * TimeZone viaBrowserVersion = options.getWebClientVersion.getSystemTimezone();
 * // System time zone accessed via standard Capabilities API
 * TimeZone viaCapabilityName = (TimeZone) options.getCapability(BrowserVersionTrait.optSystemTimezone);
 * </code></pre>
 *
 * <b>NOTE</b>: Although <b>HtmlUnitDriverOptions</b> objects are mutable (their properties can be altered),
 * the individual traits of the {@link BrowserVersion} object within these objects cannot be altered:
 *
 * <pre><code>
 * HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.CHROME);
 * options.setCapability(BrowserVersionTrait.optUserAgent, "HtmlUnitDriver emulating Google Chrome");
 * // =&gt; UnsupporterOperationException: Individual browser version traits are immutable; 'optUserAgent' cannot be set
 * </code></pre>
 *
 * @since HtmlUnitDriver v4.22.0
 * @see HtmlUnitOption
 * @see BrowserVersionTrait
 *
 * @author Scott Babcock
 * @author Ronald Brill
 */
@SuppressWarnings("serial")
public class HtmlUnitDriverOptions extends AbstractDriverOptions<HtmlUnitDriverOptions> {

    /**
     * Key used to store a set of HtmlUnitDriverOptions in a {@link Capabilities}
     * object.
     */
    public static final String HTMLUNIT_OPTIONS = "garg:htmlunitOptions";

    /**
     * Key used to store the browser version in a {@link Capabilities} object.
     * <p>
     * This key includes the "garg:" vendor prefix so that the Grid Distributor
     * will ignore it. Using the standard "browserVersion" key instead results
     * in slot match failures, because the distributor requires the version
     * specified in the requested capabilities to match the version specified
     * by the slot stereotype.
     */
    public static final String BROWSER_VERSION = "garg:browserVersion";

    /** List of capability keys used for storing browser version. */
    private static final List<String> BROWSER_VERSION_KEYS =
        List.of(BROWSER_VERSION, CapabilityType.BROWSER_VERSION);

    /** Configuration options for the underlying {@link WebClient} instance. */
    private WebClientOptions webClientOptions_ = new WebClientOptions();
    /** The {@link BrowserVersion} used by the underlying {@link WebClient}. */
    private BrowserVersion webClientVersion_ = BrowserVersion.BEST_SUPPORTED;

    /**
     * Default constructor.
     * <p>
     * Initializes HtmlUnitDriverOptions with default capabilities:
     * <ul>
     *   <li>Browser name set to "htmlunit"</li>
     *   <li>Home page set to "about:blank"</li>
     *   <li>Exceptions on failing status codes disabled</li>
     *   <li>Print content on failing status code disabled</li>
     *   <li>Use insecure SSL enabled</li>
     * </ul>
     */
    public HtmlUnitDriverOptions() {
        setCapability(CapabilityType.BROWSER_NAME, HTMLUNIT.browserName());
        webClientOptions_.setHomePage(UrlUtils.URL_ABOUT_BLANK.toString());
        webClientOptions_.setThrowExceptionOnFailingStatusCode(false);
        webClientOptions_.setPrintContentOnFailingStatusCode(false);
        webClientOptions_.setUseInsecureSSL(true);
    }

    /**
     * Constructs HtmlUnitDriverOptions with a specific {@link BrowserVersion}.
     *
     * @param version the browser version to use
     */
    public HtmlUnitDriverOptions(final BrowserVersion version) {
        this();
        setWebClientVersion(version);
    }

    /**
     * Constructs HtmlUnitDriverOptions with a specific {@link BrowserVersion} and
     * JavaScript support enabled/disabled.
     *
     * @param version the browser version to use
     * @param enableJavascript true to enable JavaScript support, false to disable
     */
    public HtmlUnitDriverOptions(final BrowserVersion version, final boolean enableJavascript) {
        this();
        setWebClientVersion(version);
        setJavaScriptEnabled(enableJavascript);
    }

    /**
     * Constructs HtmlUnitDriverOptions from an existing {@link Capabilities} object.
     * <p>
     * Transfers mapped capabilities and legacy HtmlUnit options. If the source is
     * another HtmlUnitDriverOptions instance, copies the {@link WebClientOptions}
     * and {@link BrowserVersion} from it.
     *
     * @param source source capabilities to copy
     */
    public HtmlUnitDriverOptions(final Capabilities source) {
        this();
        if (source != null) {
            // transfer mapped capabilities
            source.asMap().forEach(this::setCapability);
            // ensure browser name is correct
            setCapability(CapabilityType.BROWSER_NAME, HTMLUNIT.browserName());

            if (source instanceof HtmlUnitDriverOptions) {
                // transfer web client option from source capabilities
                transfer(((HtmlUnitDriverOptions) source).webClientOptions_, webClientOptions_);
                // copy web client version from source capabilities
                webClientVersion_ = ((HtmlUnitDriverOptions) source).webClientVersion_;
            }
            else {
                // get HtmlUnit options from standard capabilities
                final Object htmlunitOptions = source.getCapability(HTMLUNIT_OPTIONS);
                // if capability was found
                if (htmlunitOptions != null) {
                    // import HtmlUnit options
                    importOptions(htmlunitOptions);

                    // remove encoded HtmlUnit options
                    setCapability(HTMLUNIT_OPTIONS, (Object) null);
                }
                else {
                    // set web client version from standard capabilities
                    webClientVersion_ = BrowserVersionDeterminer.determine(source);
                }
            }

            // if JAVASCRIPT_ENABLED has default value, but legacy capability is 'false'
            if (isJavaScriptEnabled() && (Boolean.FALSE == source.getCapability(JAVASCRIPT_ENABLED))) {
                // disable JavaScript support
                setJavaScriptEnabled(false);
            }
            // if DOWNLOAD_IMAGES has default value, but legacy capability is 'true'
            if (!isDownloadImages() && source.is(DOWNLOAD_IMAGES_CAPABILITY)) {
                // enable image download
                setDownloadImages(true);
            }

            // remove legacy capabilities
            super.setCapability(JAVASCRIPT_ENABLED, (Object) null);
            super.setCapability(DOWNLOAD_IMAGES_CAPABILITY, (Object) null);
        }
    }

    /**
     * Constructs HtmlUnitDriverOptions from a map of options.
     *
     * @param optionsMap map containing option key-value pairs
     */
    public HtmlUnitDriverOptions(final Map<String, Object> optionsMap) {
        this(new MutableCapabilities(Require.nonNull("Source options map", optionsMap)));
    }

    @Override
    public HtmlUnitDriverOptions setBrowserVersion(final String browserVersion) {
        super.setCapability(BROWSER_VERSION, (Object) browserVersion);
        return this;
    }

    @Override
    public String getBrowserVersion() {
        return BrowserVersionDeterminer.getBrowserVersion(this);
    }

    @Override
    public Object getCapability(final String capabilityName) {
        Require.nonNull("Capability name", capabilityName);
        if (HTMLUNIT_OPTIONS.equals(capabilityName)) {
            return exportOptions();
        }
        if (BROWSER_VERSION_KEYS.contains(capabilityName)) {
            return getBrowserVersion();
        }
        final HtmlUnitOption option = HtmlUnitOption.fromCapabilityKey(capabilityName);
        if (option != null) {
            switch (option) {
                case SSL_CLIENT_CERTIFICATE_PASSWORD:
                case SSL_TRUST_STORE_PASSWORD:
                    return null;
                case WEB_CLIENT_VERSION:
                    return webClientVersion_;
                default:
                    return option.obtain(webClientOptions_);
            }
        }
        final BrowserVersionTrait trait = BrowserVersionTrait.fromCapabilityKey(capabilityName);
        if (trait != null) {
            return trait.obtain(webClientVersion_);
        }
        return super.getCapability(capabilityName);
    }

    @Override
    public void setCapability(final String capabilityName, final Object value) {
        Require.nonNull("Capability name", capabilityName);
        if (HTMLUNIT_OPTIONS.equals(capabilityName)) {
            importOptions(value);
            return;
        }
        if (BROWSER_VERSION_KEYS.contains(capabilityName)) {
            setBrowserVersion(value != null ? String.valueOf(value) : null);
            return;
        }
        final HtmlUnitOption option = HtmlUnitOption.fromCapabilityKey(capabilityName);
        if (option != null) {
            if (option == HtmlUnitOption.WEB_CLIENT_VERSION) {
                webClientVersion_ = (BrowserVersion) option.decode(value);
                return;
            }
            option.insert(webClientOptions_, value);
            return;
        }
        if (BrowserVersionTrait.fromCapabilityKey(capabilityName) != null) {
            throw new UnsupportedOperationException(
                    "Individual browser version traits are immutable; '" + capabilityName + "' cannot be set");
        }
        super.setCapability(capabilityName, value);
    }

    @Override
    protected Set<String> getExtraCapabilityNames() {
        return Collections.singleton(HTMLUNIT_OPTIONS);
    }

    @Override
    protected Object getExtraCapability(final String capabilityName) {
        Require.nonNull("Capability name", capabilityName);
        if (HTMLUNIT_OPTIONS.equals(capabilityName)) {
            return exportOptions();
        }
        return null;
    }

    /**
     * Import values from the specified source into this <b>HtmlUnitDriver</b> options object.
     *
     * @param source source {@link WebClientOptions} object
     * @return this {@link HtmlUnitDriverOptions} object
     */
    public HtmlUnitDriverOptions importOptions(final WebClientOptions source) {
        transfer(source, webClientOptions_);
        return this;
    }

    /**
     * Apply values from this <b>HtmlUnitDriver</b> options object to the specifies target.
     *
     * @param target target {@link WebClientOptions} object
     */
    public void applyOptions(final WebClientOptions target) {
        transfer(webClientOptions_, target);
    }

    /**
     * Returns whether JavaScript execution is enabled.
     *
     * @return {@code true} if JavaScript is enabled; {@code false} otherwise
     */
    public boolean isJavaScriptEnabled() {
        return webClientOptions_.isJavaScriptEnabled();
    }

    /**
     * Enables or disables JavaScript execution.
     *
     * @param enableJavascript {@code true} to enable JavaScript; {@code false} to disable it
     * @return this options instance
     */
    public HtmlUnitDriverOptions setJavaScriptEnabled(final boolean enableJavascript) {
        webClientOptions_.setJavaScriptEnabled(enableJavascript);
        return this;
    }

    /**
     * Returns whether images are downloaded automatically.
     *
     * @return {@code true} if image downloading is enabled; {@code false} otherwise
     */
    public boolean isDownloadImages() {
        return webClientOptions_.isDownloadImages();
    }

    /**
     * Enables or disables automatic image downloading.
     *
     * @param downloadImages {@code true} to download images; {@code false} otherwise
     * @return this options instance
     */
    public HtmlUnitDriverOptions setDownloadImages(final boolean downloadImages) {
        webClientOptions_.setDownloadImages(downloadImages);
        return this;
    }

    /**
     * Returns the configured {@link BrowserVersion} used by the underlying WebClient.
     *
     * @return the current web client browser version
     */
    public BrowserVersion getWebClientVersion() {
        return webClientVersion_;
    }

    /**
     * Sets the {@link BrowserVersion} used by the underlying WebClient.
     *
     * @param webClientVersion the browser version to use
     * @return this options instance
     * @throws NullPointerException if {@code webClientVersion} is {@code null}
     */
    public HtmlUnitDriverOptions setWebClientVersion(final BrowserVersion webClientVersion) {
        Require.nonNull("Web client version", webClientVersion);
        webClientVersion_ = webClientVersion;
        return this;
    }

    /**
     * Sets the SSL client certificate key store from an existing {@link KeyStore}.
     *
     * @param keyStore the key store containing the client certificate
     * @param keyStorePassword the password for the key store
     * @return this options instance
     */
    public HtmlUnitDriverOptions setSSLClientCertificateKeyStore(final KeyStore keyStore,
            final char[] keyStorePassword) {
        webClientOptions_.setSSLClientCertificateKeyStore(keyStore, keyStorePassword);
        return this;
    }

    /**
     * Sets the SSL client certificate key store using a URL.
     *
     * @param keyStoreUrl the URL to the key store
     * @param keyStorePassword the key store password
     * @param keyStoreType the key store type (e.g., {@code "PKCS12"})
     * @return this options instance
     * @throws NullPointerException if {@code keyStoreUrl} is {@code null}
     */
    public HtmlUnitDriverOptions setSSLClientCertificateKeyStore(final URL keyStoreUrl, final String keyStorePassword,
            final String keyStoreType) {
        Require.nonNull("Key store URL", keyStoreUrl);
        webClientOptions_.setSSLClientCertificateKeyStore(keyStoreUrl, keyStorePassword, keyStoreType);
        return this;
    }

    /**
     * Sets the SSL client certificate key store from an {@link InputStream}.
     *
     * @param keyStoreInputStream the input stream providing the key store
     * @param keyStorePassword the key store password
     * @param keyStoreType the key store type (e.g., {@code "PKCS12"})
     * @return this options instance
     */
    public HtmlUnitDriverOptions setSSLClientCertificateKeyStore(final InputStream keyStoreInputStream,
            final String keyStorePassword, final String keyStoreType) {
        webClientOptions_.setSSLClientCertificateKeyStore(keyStoreInputStream, keyStorePassword, keyStoreType);
        return this;
    }

    /**
     * Sets the SSL trust store using a URL.
     *
     * @param sslTrustStoreUrl the URL to the trust store
     * @param sslTrustStorePassword the trust store password
     * @param sslTrustStoreType the trust store type (e.g., {@code "JKS"})
     * @return this options instance
     * @throws NullPointerException if {@code sslTrustStoreUrl} is {@code null}
     */
    public HtmlUnitDriverOptions setSSLTrustStore(final URL sslTrustStoreUrl, final String sslTrustStorePassword,
            final String sslTrustStoreType) {
        Require.nonNull("Trust store URL", sslTrustStoreUrl);
        webClientOptions_.setSSLTrustStore(sslTrustStoreUrl, sslTrustStorePassword, sslTrustStoreType);
        return this;
    }

    @SuppressWarnings("unchecked")
    private void importOptions(final Object rawOptions) {
        Map<String, Object> optionsMap = new HashMap<>();

        // if value specified
        if (rawOptions != null) {
            Require.stateCondition(rawOptions instanceof Map,
                    "Specified value must be 'Map'; was %s", rawOptions.getClass().getName());
            optionsMap = (Map<String, Object>) rawOptions;
        }

        // apply specified system properties to options map
        for (final HtmlUnitOption option : HtmlUnitOption.values()) {
            option.applyPropertyTo(optionsMap);
        }

        if (!optionsMap.isEmpty()) {
            for (final HtmlUnitOption option : HtmlUnitOption.values()) {
                if (optionsMap.containsKey(option.getCapabilityKey())) {
                    switch (option) {
                        case SSL_CLIENT_CERTIFICATE_PASSWORD:
                        case SSL_TRUST_STORE_PASSWORD:
                            continue;
                        case WEB_CLIENT_VERSION:
                            webClientVersion_ =
                                (BrowserVersion) option.decode(optionsMap.get(option.getCapabilityKey()));
                            break;
                        default:
                            option.insert(webClientOptions_, optionsMap.get(option.getCapabilityKey()));
                            break;
                    }
                }
            }
        }
    }

    private Map<String, Object> exportOptions() {
        final Map<String, Object> optionsMap = new HashMap<>();
        for (final HtmlUnitOption option : HtmlUnitOption.values()) {
            switch (option) {
                case SSL_CLIENT_CERTIFICATE_PASSWORD:
                case SSL_TRUST_STORE_PASSWORD:
                    continue;
                case WEB_CLIENT_VERSION:
                    if (webClientVersion_ != null) {
                        optionsMap.put(option.getCapabilityKey(), option.encode(webClientVersion_));
                    }
                    break;
                default:
                    final Object value = option.obtain(webClientOptions_);
                    if (!option.isDefaultValue(value)) {
                        optionsMap.put(option.getCapabilityKey(), option.encode(value));
                    }
                    break;
            }
        }
        return optionsMap;
    }

    private static void transfer(final WebClientOptions source, final WebClientOptions target) {
        Require.nonNull("Source capabilities", source);
        Require.nonNull("Target capabilities", target);
        target.setJavaScriptEnabled(source.isJavaScriptEnabled());
        target.setCssEnabled(source.isCssEnabled());
        target.setPrintContentOnFailingStatusCode(source.isPrintContentOnFailingStatusCode());
        target.setThrowExceptionOnFailingStatusCode(source.isThrowExceptionOnFailingStatusCode());
        target.setThrowExceptionOnScriptError(source.isThrowExceptionOnScriptError());
        target.setPopupBlockerEnabled(source.isPopupBlockerEnabled());
        target.setRedirectEnabled(source.isRedirectEnabled());
        try {
            target.setTempFileDirectory(source.getTempFileDirectory());
        }
        catch (final IOException eaten) {
            // ignore
        }
        target.setSSLClientProtocols(source.getSSLClientProtocols());
        target.setSSLClientCipherSuites(source.getSSLClientCipherSuites());
        target.setGeolocationEnabled(source.isGeolocationEnabled());
        target.setDoNotTrackEnabled(source.isDoNotTrackEnabled());
        target.setHomePage(source.getHomePage());

        final ProxyConfig proxyConfig = source.getProxyConfig();
        if (proxyConfig != null) {
            target.setProxyConfig(proxyConfig);
        }

        target.setTimeout(source.getTimeout());
        target.setConnectionTimeToLive(source.getConnectionTimeToLive());
        target.setUseInsecureSSL(source.isUseInsecureSSL());
        target.setSSLInsecureProtocol(source.getSSLInsecureProtocol());
        target.setMaxInMemory(source.getMaxInMemory());
        target.setHistorySizeLimit(source.getHistorySizeLimit());
        target.setHistoryPageCacheLimit(source.getHistoryPageCacheLimit());
        target.setLocalAddress(source.getLocalAddress());
        target.setDownloadImages(source.isDownloadImages());
        target.setScreenWidth(source.getScreenWidth());
        target.setScreenHeight(source.getScreenHeight());
        target.setWebSocketEnabled(source.isWebSocketEnabled());
        target.setWebSocketMaxTextMessageSize(source.getWebSocketMaxTextMessageSize());
        target.setWebSocketMaxTextMessageBufferSize(source.getWebSocketMaxTextMessageBufferSize());
        target.setWebSocketMaxBinaryMessageSize(source.getWebSocketMaxBinaryMessageSize());
        target.setWebSocketMaxBinaryMessageBufferSize(source.getWebSocketMaxBinaryMessageBufferSize());
        target.setFetchPolyfillEnabled(source.isFetchPolyfillEnabled());
    }

}
