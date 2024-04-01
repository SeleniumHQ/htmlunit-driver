package org.openqa.selenium.htmlunit;

import org.htmlunit.ProxyConfig;
import org.htmlunit.WebClient;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.Proxy;

import java.util.ArrayList;
import java.util.List;

public class ProxyConfigurationManager {
    private final ProxyConfig proxyConfig;

    private WebClient webClient_;


    public ProxyConfigurationManager() {
        this.proxyConfig = new ProxyConfig();
    }

    public WebClient getWebClient() {
        if (webClient_ == null) {
            throw new NoSuchSessionException("Session is closed");
        }
        return webClient_;
    }
    public void setProxySettings(final Proxy proxy) {
        if (proxy == null) {
            return;
        }
        switch (proxy.getProxyType()) {
            case MANUAL:
                configureManualProxy(proxy);
                break;
            case PAC:
                configurePacProxy(proxy);
                break;
            default:
                // Unsupported proxy types or no proxy configuration
                break;
        }
    }

    private void configureManualProxy(Proxy proxy) {
        final List<String> noProxyHosts = extractNoProxyHosts(proxy);
        final String httpProxy = proxy.getHttpProxy();
        if (httpProxy != null && !httpProxy.isEmpty()) {
            configureHttpProxy(httpProxy, noProxyHosts);
        }
        final String socksProxy = proxy.getSocksProxy();
        if (socksProxy != null && !socksProxy.isEmpty()) {
            configureSocksProxy(socksProxy, noProxyHosts);
        }
    }

    private void configurePacProxy(Proxy proxy) {
        final String pac = proxy.getProxyAutoconfigUrl();
        if (pac != null && !pac.isEmpty()) {
            this.proxyConfig.setProxyAutoConfigUrl(pac);
        }
    }

    private List<String> extractNoProxyHosts(Proxy proxy) {
        final List<String> noProxyHosts = new ArrayList<>();
        final String noProxy = proxy.getNoProxy();
        if (noProxy != null && !noProxy.isEmpty()) {
            for (String host : noProxy.split(",")) {
                if (!host.trim().isEmpty()) {
                    noProxyHosts.add(host.trim());
                }
            }
        }
        return noProxyHosts;
    }

    private void configureHttpProxy(String httpProxy, List<String> noProxyHosts) {
        final String[] proxyParts = httpProxy.split(":", 2);
        final String host = proxyParts[0];
        final int port = proxyParts.length > 1 ? Integer.parseInt(proxyParts[1]) : 0;
        setHTTPProxy(host, port, noProxyHosts);
    }

    private void configureSocksProxy(String socksProxy, List<String> noProxyHosts) {
        final String[] proxyParts = socksProxy.split(":", 2);
        final String host = proxyParts[0];
        final int port = proxyParts.length > 1 ? Integer.parseInt(proxyParts[1]) : 0;
        setSocksProxy(host, port, noProxyHosts);
    }

    public void setHTTPProxy(final String host, final int port, final List<String> noProxyHosts) {
        this.proxyConfig.setProxyHost(host);
        this.proxyConfig.setProxyPort(port);
        noProxyHosts.forEach(this.proxyConfig::addHostsToProxyBypass);
    }

    public void setSocksProxy(final String host, final int port, final List<String> noProxyHosts) {
        this.proxyConfig.setSocksProxy(true);
        setHTTPProxy(host, port, noProxyHosts);
    }

    public void setProxy(final String host, final int port) {
        setHTTPProxy(host, port, null);
    }

    public void setSocksProxy(final String host, final int port) {
        setSocksProxy(host, port, null);
    }


    public void setAutoProxy(final String autoProxyUrl) {
        this.proxyConfig.setProxyAutoConfigUrl(autoProxyUrl);
    }

    public void applyProxySettings(WebClient webClient) {
        webClient.getOptions().setProxyConfig(this.proxyConfig);
    }
}
