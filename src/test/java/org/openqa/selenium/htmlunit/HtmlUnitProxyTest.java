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

package org.openqa.selenium.htmlunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.remote.CapabilityType.PROXY;

import java.util.ArrayList;
import java.util.List;

import org.htmlunit.ProxyConfig;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Test the proxy setting.
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitProxyTest {

    @Test
    public void testProxyAsCapability() {
        final DesiredCapabilities capabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "", Platform.ANY);
        final Proxy proxy = new Proxy().setHttpProxy("http.proxy");
        capabilities.setCapability(PROXY, proxy);

        final HtmlUnitDriver driver = new HtmlUnitDriver(capabilities);
        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("http.proxy", config.getProxyHost());

        driver.quit();
    }

    @Test
    public void testManualHttpProxy() {
        final Proxy proxy = new Proxy().setHttpProxy("http.proxy:1234");

        final HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setProxySettings(proxy);

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("http.proxy", config.getProxyHost());
        assertEquals(1234, config.getProxyPort());
        assertFalse(config.isSocksProxy());

        driver.quit();
    }

    @Test
    public void testManualHttpProxyDirectly() {

        final HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setProxy("http.proxy", 1234);

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("http.proxy", config.getProxyHost());
        assertEquals(1234, config.getProxyPort());
        assertFalse(config.isSocksProxy());

        driver.quit();
    }

    @Test
    public void testManualHttpProxyWithNoProxy() {
        final Proxy proxy = new Proxy().setHttpProxy("http.proxy").setNoProxy("localhost, 127.0.0.1");

        final HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setProxySettings(proxy);

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("http.proxy", config.getProxyHost());
        assertEquals(0, config.getProxyPort());
        assertFalse(config.isSocksProxy());

        driver.quit();
    }

    @Test
    public void testManualHttpProxyWithNoProxyDirectly() {
        final HtmlUnitDriver driver = new HtmlUnitDriver();

        final List<String> noProxy = new ArrayList<>();
        noProxy.add("localhost");
        noProxy.add("127.0.0.1");
        driver.setHTTPProxy("http.proxy", 0, noProxy);

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("http.proxy", config.getProxyHost());
        assertEquals(0, config.getProxyPort());
        assertFalse(config.isSocksProxy());

        driver.quit();
    }

    @Test
    public void testManualSocksProxy() {
        final Proxy proxy = new Proxy().setSocksProxy("socks.proxy:1234");

        final HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setProxySettings(proxy);

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("socks.proxy", config.getProxyHost());
        assertEquals(1234, config.getProxyPort());
        assertTrue(config.isSocksProxy());

        driver.quit();
    }

    @Test
    public void testManualSocksProxyDirectly() {

        final HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setSocksProxy("socks.proxy", 1234);

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("socks.proxy", config.getProxyHost());
        assertEquals(1234, config.getProxyPort());
        assertTrue(config.isSocksProxy());

        driver.quit();
    }

    @Test
    public void testManualSocksProxyWithNoProxy() {
        final Proxy proxy = new Proxy().setSocksProxy("socks.proxy").setNoProxy("localhost");

        final HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setProxySettings(proxy);

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("socks.proxy", config.getProxyHost());
        assertEquals(0, config.getProxyPort());
        assertTrue(config.isSocksProxy());

        driver.quit();
    }

    @Test
    public void testManualSocksProxyWithNoProxyDirectly() {
        final HtmlUnitDriver driver = new HtmlUnitDriver();
        final List<String> noProxy = new ArrayList<>();
        noProxy.add("localhost");
        driver.setSocksProxy("socks.proxy", 0, noProxy);

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("socks.proxy", config.getProxyHost());
        assertEquals(0, config.getProxyPort());
        assertTrue(config.isSocksProxy());

        driver.quit();
    }

    @Test
    public void testPACProxy() {
        final Proxy proxy = new Proxy().setProxyAutoconfigUrl("http://aaa/bb.pac");

        final HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setProxySettings(proxy);

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("http://aaa/bb.pac", config.getProxyAutoConfigUrl());

        driver.quit();
    }

    @Test
    public void testPACProxyDirectly() {
        final HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setAutoProxy("http://aaa/bb.pac");

        final ProxyConfig config = driver.getWebClient().getOptions().getProxyConfig();

        assertEquals("http://aaa/bb.pac", config.getProxyAutoConfigUrl());

        driver.quit();
    }
}
