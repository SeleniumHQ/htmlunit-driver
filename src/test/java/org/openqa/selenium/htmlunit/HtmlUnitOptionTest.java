package org.openqa.selenium.htmlunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.openqa.selenium.htmlunit.HtmlUnitOption.*;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.htmlunit.BrowserVersion;
import org.htmlunit.ProxyConfig;
import org.junit.Test;

public class HtmlUnitOptionTest {
    
    @Test
    public void decodeStringToCharArray() {
        char[] sslTrustStoreType = "jks".toCharArray();
        char[] decoded = (char[]) SSL_TRUST_STORE_TYPE.decode("jks");
        assertArrayEquals(sslTrustStoreType, decoded);
    }
    
    @Test
    public void decodeStringToStringArray() {
        String[] sslClientProtocols = new String[] {"foo", "bar", "baz"};
        String encoded = "[\"foo\", \"bar\",\"baz\"]";
        String[] decoded = (String[]) SSL_CLIENT_PROTOCOLS.decode(encoded);
        assertArrayEquals(sslClientProtocols, decoded);
    }
    
    @Test
    public void encodeAndDecodeFile() {
        File tempFileDirectory = new File(System.getProperty("user.home"));
        String encoded = (String) TEMP_FILE_DIRECTORY.encode(tempFileDirectory);
        File decoded = (File) TEMP_FILE_DIRECTORY.decode(encoded);
        assertEquals(tempFileDirectory, decoded);
    }
    
    @Test
    public void encodeAndDecodeInetAddress() throws UnknownHostException {
        final InetAddress localHost = InetAddress.getLocalHost();
        String encoded = (String) LOCAL_ADDRESS.encode(localHost);
        InetAddress decoded = (InetAddress) LOCAL_ADDRESS.decode(encoded);
        assertEquals(localHost, decoded);
    }
    
    @Test
    public void encodeAndDecodeProxyConfig() {
        final ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyPort("http");
        proxyConfig.setProxyHost("htmlunit.proxy");
        proxyConfig.setProxyPort(1234);
        proxyConfig.addHostsToProxyBypass("localhost");
        proxyConfig.addHostsToProxyBypass("127\\.0\\.0\\.1");
        Object encoded = PROXY_CONFIG.encode(proxyConfig);
        ProxyConfig decoded = (ProxyConfig) PROXY_CONFIG.decode(encoded);
        verify(proxyConfig, decoded);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void encodeAndDecodeBrowserVersion() {
        Map<String, Object> encoded = (Map<String, Object>) WEB_CLIENT_VERSION.encode(BrowserVersion.BEST_SUPPORTED);
        BrowserVersion decoded = (BrowserVersion) WEB_CLIENT_VERSION.decode(encoded);
        BrowserVersionTraitTest.verify(BrowserVersion.BEST_SUPPORTED, decoded);
    }

    static void verify(final ProxyConfig expect, final ProxyConfig actual) {
        for (ProxyConfigOption option : ProxyConfigOption.values()) {
            assertEquals("Proxy config option mismatch for: " + option.key, option.obtain(expect), option.obtain(actual));
        }
    }
}
