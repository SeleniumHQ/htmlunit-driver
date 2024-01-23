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

@SuppressWarnings("serial")
public class ProxyConfigBean implements Serializable {
    private String host;
    private int port;
    private String scheme;
    private boolean socksProxy;
    private List<String> bypassHosts;
    private String autoConfigUrl;
    
    public String getHost() {
        return host;
    }
    
    public void setHost(final String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(final int port) {
        this.port = port;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }
    
    public boolean isSocksProxy() {
        return socksProxy;
    }
    
    public void setSocksProxy(final boolean socksProxy) {
        this.socksProxy = socksProxy;
    }
    
    public List<String> getBypassHosts() {
        return bypassHosts;
    }
    
    public String getBypassHosts(final int index) {
        return bypassHosts.get(index);
    }

    public void setBypassHosts(final List<String> bypassHosts) {
        this.bypassHosts = bypassHosts;
    }
    
    public void setBypassHosts(final int index, final String bypassHost) {
        bypassHosts.set(index, bypassHost);
    }
    
    public String getAutoConfigUrl() {
        return autoConfigUrl;
    }
    
    public void setAutoConfigUrl(final String autoConfigUrl) {
        this.autoConfigUrl = autoConfigUrl;
    }
    
    /**
     * Encode the specified {@code ProxyConfig} object.
     * 
     * @param value {@link ProxyConfig} object to be encoded
     * @return encoded {@code ProxyConfig} object
     */
    public static Map<String, Object> encodeProxyConfig(final ProxyConfig value) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("host", value.getProxyHost());
        configMap.put("port", value.getProxyPort());
        configMap.put("scheme", value.getProxyScheme());
        configMap.put("bypassHosts", getBypassHosts(value));
        configMap.put("autoConfigUrl", value.getProxyAutoConfigUrl());
        return configMap;
    }
    
    public ProxyConfig build() {
        ProxyConfig value = new ProxyConfig(host, port, scheme, socksProxy);
        bypassHosts.forEach(value::addHostsToProxyBypass);
        value.setProxyAutoConfigUrl(autoConfigUrl);
        return value;
    }
    
    @SuppressWarnings("unchecked")
    static List<String> getBypassHosts(final ProxyConfig value) {
        try {
            Field proxyBypassHosts_ = ProxyConfig.class.getDeclaredField("proxyBypassHosts_");
            proxyBypassHosts_.setAccessible(true);
            Map<String, Pattern> proxyBypassHosts = (Map<String, Pattern>) proxyBypassHosts_.get(value);
            return new ArrayList<String>(proxyBypassHosts.keySet());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return null;
        }
        
    }
}
