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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.htmlunit.ProxyConfig;

/**
 * @author Scott Babcock
 * @author Ronald Brill
 */
@SuppressWarnings("serial")
public class ProxyConfigBean implements Serializable {
    private String host_;
    private int port_;
    private String scheme_;
    private boolean socksProxy_;
    private List<String> bypassHosts_;
    private String autoConfigUrl_;

    public String getHost() {
        return host_;
    }

    public void setHost(final String host) {
        host_ = host;
    }

    public int getPort() {
        return port_;
    }

    public void setPort(final int port) {
        port_ = port;
    }

    public String getScheme() {
        return scheme_;
    }

    public void setScheme(final String scheme) {
        scheme_ = scheme;
    }

    public boolean isSocksProxy() {
        return socksProxy_;
    }

    public void setSocksProxy(final boolean socksProxy) {
        socksProxy_ = socksProxy;
    }

    public List<String> getBypassHosts() {
        return bypassHosts_;
    }

    public String getBypassHosts(final int index) {
        return bypassHosts_.get(index);
    }

    public void setBypassHosts(final List<String> bypassHosts) {
        bypassHosts_ = bypassHosts;
    }

    public void setBypassHosts(final int index, final String bypassHost) {
        bypassHosts_.set(index, bypassHost);
    }

    public String getAutoConfigUrl() {
        return autoConfigUrl_;
    }

    public void setAutoConfigUrl(final String autoConfigUrl) {
        autoConfigUrl_ = autoConfigUrl;
    }

    /**
     * Encode the specified {@code ProxyConfig} object.
     *
     * @param value {@link ProxyConfig} object to be encoded
     * @return encoded {@code ProxyConfig} object
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
            return new ArrayList<String>(proxyBypassHosts.keySet());
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return null;
        }

    }
}
