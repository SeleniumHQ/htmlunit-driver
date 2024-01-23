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

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import org.htmlunit.ProxyConfig;
import org.junit.Test;
import org.openqa.selenium.json.Json;

public class BeanTest {
    
    private static final String proxyConfig = 
            "{\"host\": \"proxy.example.com\", \"port\": 8080, \"scheme\": \"https\", "
            + "\"socksProxy\": false, \"bypassHosts\": [\"localhost\", \"intranet.info\"], "
            + "\"autoConfigUrl\": \"http://aaa/bb.pac\"}";
    
    private static final String keyStore =
            "{\"url\":\"file:///path/to/cert/store\", \"password\":\"fortnight\", \"type\":\"jks\"}";
    
    @Test
    public void decodeProxyConfigBean() {
        ProxyConfigBean bean = new Json().toType(proxyConfig, ProxyConfigBean.class);
        assertEquals("Failed decoding [host]", "proxy.example.com", bean.getHost());
        assertEquals("Failed decoding [port]", 8080, bean.getPort());
        assertEquals("Failed decoding [scheme]", "https", bean.getScheme());
        assertEquals("Failed decoding [socksProxy]", false, bean.isSocksProxy());
        assertEquals("Failed decoding [bypassHosts]", Arrays.asList("localhost", "intranet.info"), bean.getBypassHosts());
        assertEquals("Failed decoding [autoConfigUrl]", "http://aaa/bb.pac", bean.getAutoConfigUrl());
        
        ProxyConfig config = bean.build();
        assertEquals("Failed building [host]", "proxy.example.com", config.getProxyHost());
        assertEquals("Failed building [port]", 8080, config.getProxyPort());
        assertEquals("Failed building [scheme]", "https", config.getProxyScheme());
        assertEquals("Failed building [socksProxy]", false, config.isSocksProxy());
        assertEquals("Failed decoding [bypassHosts]", Arrays.asList("localhost", "intranet.info"), ProxyConfigBean.getBypassHosts(config));
        assertEquals("Failed building [autoConfigUrl]", "http://aaa/bb.pac", config.getProxyAutoConfigUrl());
    }
    
    @Test
    public void decodeKeyStoreBean() throws MalformedURLException {
        KeyStoreBean bean = new Json().toType(keyStore, KeyStoreBean.class);
        assertEquals("Failed decoding [url]", "file:///path/to/cert/store", bean.getUrl());
        assertEquals("failed deciding [password]", "fortnight", bean.getPassword());
        assertEquals("Failed decoding [type]", "jks", bean.getType());
        
        assertEquals("Failed creating URL from [url]", new URL("file:///path/to/cert/store"), bean.createUrl());
    }
    
}
