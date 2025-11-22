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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.htmlunit.ProxyConfig;

/**
 * A JavaBean representation of a proxy configuration. This class provides getters and setters
 * for common proxy properties such as host, port, scheme, SOCKS flag, bypass hosts, and
 * auto-configuration URL. It also provides utilities to convert to and from {@link ProxyConfig}.
 * <p>
 * Example usage:
 * <pre>
 * ProxyConfigBean bean = new ProxyConfigBean();
 * bean.setHost("proxy.example.com");
 * bean.setPort(8080);
 * bean.setScheme("http");
 * bean.setSocksProxy(false);
 * bean.setBypassHosts(Arrays.asList("localhost", "127.0.0.1"));
 * ProxyConfig proxyConfig = bean.build();
 * </pre>
 * 
 * @author Scott Babcock
 * @author Ronald Brill
 */
@SuppressWarnings("serial")
public class ProxyConfigBean implements Serializable {

    /** The proxy host name or IP address. */
    private String host_;
    /** The port number of the proxy server. */
    private int port_;
    /** The scheme used by the proxy (e.g., "http", "https"). */
    private String scheme_;
    /** Indicates whether this is a SOCKS proxy. */
    private boolean socksProxy_;
    /** The list of hosts to bypass the proxy for. */
    private List<String> bypassHosts_;
    /** URL to a proxy auto-configuration (PAC) file. */
    private String autoConfigUrl_;

    /**
     * Returns the proxy host.
     *
     * @return the proxy host
     */
    public String getHost() {
        return host_;
    }

    /**
     * Sets the proxy host.
     *
     * @param host the proxy host to set
     */
    public void setHost(final String host) {
        host_ = host;
    }

    /**
     * Returns the proxy port.
     *
     * @return the proxy port
     */
    public int getPort() {
        return port_;
    }

    /**
     * Sets the proxy port.
     *
     * @param port the proxy port to set
     */
    public void setPort(final int port) {
        port_ = port;
    }

    /**
     * Returns the proxy scheme.
     *
     * @return the proxy scheme
     */
    public String getScheme() {
        return scheme_;
    }

    /**
     * Sets the proxy scheme.
     *
     * @param scheme the proxy scheme to set
     */
    public void setScheme(final String scheme) {
        scheme_ = scheme;
    }

    /**
     * Indicates if this is a SOCKS proxy.
     *
     * @return {@code true} if SOCKS proxy, {@code false} otherwise
     */
    public boolean isSocksProxy() {
        return socksProxy_;
    }

    /**
     * Sets whether this is a SOCKS proxy.
     *
     * @param socksProxy {@code true} to use SOCKS proxy, {@code false} otherwise
     */
    public void setSocksProxy(final boolean socksProxy) {
        socksProxy_ = socksProxy;
    }

    /**
     * Returns the list of bypass hosts.
     *
     * @return the list of bypass hosts
     */
    public List<String> getBypassHosts() {
        return bypassHosts_;
    }

    /**
     * Returns a bypass host at the specified index.
     *
     * @param index the index of the bypass host
     * @return the bypass host at the specified index
     */
    public String getBypassHosts(final int index) {
        return bypassHosts_.get(index);
    }

    /**
     * Sets the list of bypass hosts.
     *
     * @param bypassHosts the list of bypass hosts to set
     */
    public void setBypassHosts(final List<String> bypassHosts) {
        bypassHosts_ = bypassHosts;
    }

    /**
     * Sets a specific bypass host at the given index.
     *
     * @param index the index to set
     * @param bypassHost the bypass host to set
     */
    public void setBypassHosts(final int index, final String bypassHost) {
        bypassHosts_.set(index, bypassHost);
    }

    /**
     * Returns the auto-configuration URL.
     *
     * @return the auto-config URL
     */
    public String getAutoConfigUrl() {
        return autoConfigUrl_;
    }

    /**
     * Sets the auto-configuration URL.
     *
     * @param autoConfigUrl the URL to set
     */
    public void setAutoConfigUrl(final String autoConfigUrl) {
        autoConfigUrl_ = autoConfigUrl;
    }

    /**
     * Encodes a {@link ProxyConfig} object into a {@link Map} representation.
     *
     * @param value the {@link ProxyConfig} to encode
     * @return a map representing the proxy configuration
     */
    public static Map<String, Object> encodeProxyConfig(final ProxyConfig value) {
        final Map<String, Object> configMap = new HashMap<>();
        configMap.put("host", value.getProxyHost());
        configMap.put("port", value.getProxyPort());
        configMap.put("scheme", value.getProxyScheme());
        configMap.put("bypassHosts", getBypassHosts(value));
        configMap.put("autoConfigUrl", value.getProxyAutoConfigUrl());
        return configMap;
    }

    /**
     * Builds a {@link ProxyConfig} object from this bean.
     *
     * @return a {@link ProxyConfig} with the properties of this bean
     */
    public ProxyConfig build() {
        final ProxyConfig value = new ProxyConfig(host_, port_, scheme_, socksProxy_);
        bypassHosts_.forEach(value::addHostsToProxyBypass);
        value.setProxyAutoConfigUrl(autoConfigUrl_);
        return value;
    }

    @SuppressWarnings("unchecked")
    static List<String> getBypassHosts(final ProxyConfig value) {
        try {
            final Field proxyBypassHostsField = ProxyConfig.class.getDeclaredField("proxyBypassHosts_");
            proxyBypassHostsField.setAccessible(true);
            final Map<String, Pattern> proxyBypassHosts = (Map<String, Pattern>) proxyBypassHostsField.get(value);
            return new ArrayList<>(proxyBypassHosts.keySet());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return null;
        }
    }
}
