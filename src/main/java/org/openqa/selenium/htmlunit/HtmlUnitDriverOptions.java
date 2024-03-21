package org.openqa.selenium.htmlunit;

import static org.openqa.selenium.remote.Browser.HTMLUNIT;
import static org.openqa.selenium.htmlunit.HtmlUnitDriver.JAVASCRIPT_ENABLED;
import static org.openqa.selenium.htmlunit.HtmlUnitDriver.DOWNLOAD_IMAGES_CAPABILITY;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.htmlunit.BrowserVersion;
import org.htmlunit.ProxyConfig;
import org.htmlunit.WebClientOptions;
import org.htmlunit.util.UrlUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.CapabilityType;

@SuppressWarnings("serial")
public class HtmlUnitDriverOptions extends AbstractDriverOptions<HtmlUnitDriverOptions> {

    /**
     * Key used to store a set of HtmlUnitDriverOptions in a {@link Capabilities}
     * object.
     */
    public static final String HTMLUNIT_OPTIONS = "garg:htmlunitOptions";
    
    private WebClientOptions webClientOptions = new WebClientOptions();
    private BrowserVersion webClientVersion = BrowserVersion.BEST_SUPPORTED;

    public HtmlUnitDriverOptions() {
        setCapability(CapabilityType.BROWSER_NAME, HTMLUNIT.browserName());
        webClientOptions.setHomePage(UrlUtils.URL_ABOUT_BLANK.toString());
        webClientOptions.setThrowExceptionOnFailingStatusCode(false);
        webClientOptions.setPrintContentOnFailingStatusCode(false);
        webClientOptions.setUseInsecureSSL(true);
    }
    
    public HtmlUnitDriverOptions(final BrowserVersion version) {
        this();
        setWebClientVersion(version);
    }
    
    public HtmlUnitDriverOptions(final BrowserVersion version, final boolean enableJavascript) {
        this();
        setWebClientVersion(version);
        setJavaScriptEnabled(enableJavascript);
    }
    
    public HtmlUnitDriverOptions(final Capabilities source) {
        this();
        if (source != null) {
            // transfer mapped capabilities
            source.asMap().forEach(this::setCapability);
            // ensure browser name is correct
            setCapability(CapabilityType.BROWSER_NAME, HTMLUNIT.browserName());
            
            if (source instanceof HtmlUnitDriverOptions) {
                // transfer web client option from source capabilities
                transfer(((HtmlUnitDriverOptions) source).webClientOptions, webClientOptions);
                // copy web client version from source capabilities
                webClientVersion = ((HtmlUnitDriverOptions) source).webClientVersion;
            } else {
                // get HtmlUnit options from standard capabilities
                Object htmlunitOptions = source.getCapability(HTMLUNIT_OPTIONS);
                // if capability was found
                if (htmlunitOptions != null) {
                    // import HtmlUnit options
                    importOptions(htmlunitOptions);
                    
                    // remove encoded HtmlUnit options
                    setCapability(HTMLUNIT_OPTIONS, (Object) null);
                } else {
                    // set web client version from standard capabilities
                    webClientVersion = BrowserVersionDeterminer.determine(source);
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
            setCapability(JAVASCRIPT_ENABLED, (Object) null);
            setCapability(DOWNLOAD_IMAGES_CAPABILITY, (Object) null);
        }
    }

    public HtmlUnitDriverOptions(final Map<String, Object> optionsMap) {
        this(new MutableCapabilities(optionsMap));
    }
    
    @Override
    public Object getCapability(String capabilityName) {
        if (JAVASCRIPT_ENABLED.equals(capabilityName)) {
            return isJavaScriptEnabled();
        } else if (DOWNLOAD_IMAGES_CAPABILITY.equals(capabilityName)) {
            return isDownloadImages();
        }
        return super.getCapability(capabilityName);
    }

    @Override
    public void setCapability(String capabilityName, boolean value) {
        if (JAVASCRIPT_ENABLED.equals(capabilityName)) {
            setJavaScriptEnabled(value);
        } else if (DOWNLOAD_IMAGES_CAPABILITY.equals(capabilityName)) {
            setDownloadImages(value);
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
    
    public HtmlUnitDriverOptions importOptions(final WebClientOptions source) {
        transfer(source, webClientOptions);
        return this;
    }
    
    public HtmlUnitDriverOptions applyOptions(final WebClientOptions target) {
        transfer(webClientOptions, target);
        return this;
    }
    
    public boolean isJavaScriptEnabled() {
        return webClientOptions.isJavaScriptEnabled();
    }
    
    public HtmlUnitDriverOptions setJavaScriptEnabled(final boolean enableJavascript) {
        webClientOptions.setJavaScriptEnabled(enableJavascript);
        return this;
    }
    
    public boolean isDownloadImages() {
        return webClientOptions.isDownloadImages();
    }
    
    public HtmlUnitDriverOptions setDownloadImages(final boolean downloadImages) {
        webClientOptions.setDownloadImages(downloadImages);
        return this;
    }
    
    public BrowserVersion getWebClientVersion() {
        return webClientVersion;
    }
    
    public HtmlUnitDriverOptions setWebClientVersion(final BrowserVersion webClientVersion) {
        this.webClientVersion = webClientVersion;
        return this;
    }
    
    public HtmlUnitDriverOptions setSSLClientCertificateKeyStore(final KeyStore keyStore, final char[] keyStorePassword) {
        webClientOptions.setSSLClientCertificateKeyStore(keyStore, keyStorePassword);
        return this;
    }

    public HtmlUnitDriverOptions setSSLClientCertificateKeyStore(final URL keyStoreUrl, final String keyStorePassword,
            final String keyStoreType) {
        webClientOptions.setSSLClientCertificateKeyStore(keyStoreUrl, keyStorePassword, keyStoreType);
        return this;
    }

    public HtmlUnitDriverOptions setSSLClientCertificateKeyStore(final InputStream keyStoreInputStream, final String keyStorePassword,
            final String keyStoreType) {
        webClientOptions.setSSLClientCertificateKeyStore(keyStoreInputStream, keyStorePassword, keyStoreType);
        return this;
    }

    public HtmlUnitDriverOptions setSSLTrustStore(final URL sslTrustStoreUrl, final String sslTrustStorePassword,
            final String sslTrustStoreType) {
        webClientOptions.setSSLTrustStore(sslTrustStoreUrl, sslTrustStorePassword, sslTrustStoreType);
        return this;
    }
    
    private void importOptions(final Object rawOptions) {
        if (rawOptions != null) {
            Require.stateCondition(rawOptions instanceof Map,
                    "Specified value must be 'Map'; was %s", rawOptions.getClass().getName());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> optionsMap = (Map<String, Object>) rawOptions;
            for (HtmlUnitOption option : HtmlUnitOption.values()) {
                if (optionsMap.containsKey(option.key)) {
                    switch (option) {
                    case SSL_CLIENT_CERTIFICATE_PASSWORD:
                    case SSL_TRUST_STORE_PASSWORD:
                        continue;
                    case WEB_CLIENT_VERSION:
                        webClientVersion = (BrowserVersion) option.decode(optionsMap.get(option.key));
                        break;
                    default:
                        option.insert(webClientOptions, optionsMap.get(option.key));
                        break;
                    }
                }
            }
        }
    }
    
    private Map<String, Object> exportOptions() {
        Map<String, Object> optionsMap = new HashMap<>();
        for (HtmlUnitOption option : HtmlUnitOption.values()) {
            switch (option) {
            case SSL_CLIENT_CERTIFICATE_PASSWORD:
            case SSL_TRUST_STORE_PASSWORD:
                continue;
            case WEB_CLIENT_VERSION:
                if (webClientVersion != null) {
                    optionsMap.put(option.key, option.encode(webClientVersion));
                }
                break;
            default:
                Object value = option.obtain(webClientOptions);
                if (!option.isDefault(value)) {
                    optionsMap.put(option.key, option.encode(value));
                }
                break;
            }
        }
        return optionsMap;
    }
    
    private static void transfer(final WebClientOptions source, final WebClientOptions target) {
        target.setJavaScriptEnabled(source.isJavaScriptEnabled());
        target.setCssEnabled(source.isCssEnabled());
        target.setPrintContentOnFailingStatusCode(source.isPrintContentOnFailingStatusCode());
        target.setThrowExceptionOnFailingStatusCode(source.isThrowExceptionOnFailingStatusCode());
        target.setThrowExceptionOnScriptError(source.isThrowExceptionOnScriptError());
        target.setAppletEnabled(source.isAppletEnabled());
        target.setPopupBlockerEnabled(source.isPopupBlockerEnabled());
        target.setRedirectEnabled(source.isRedirectEnabled());
        try { target.setTempFileDirectory(source.getTempFileDirectory()); } catch (IOException eaten) { }
        target.setSSLClientProtocols(source.getSSLClientProtocols());
        target.setSSLClientCipherSuites(source.getSSLClientCipherSuites());
        target.setGeolocationEnabled(source.isGeolocationEnabled());
        target.setDoNotTrackEnabled(source.isDoNotTrackEnabled());
        target.setHomePage(source.getHomePage());
        
        ProxyConfig proxyConfig = source.getProxyConfig();
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
