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

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * General tests for the HtmlUnitDriver.
 *
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitDriverTest {

    @Test
    public void customizeWebClient() {
        final StringBuffer detector = new StringBuffer();

        final WebDriver webDriver = new HtmlUnitDriver() {
            @Override
            protected WebClient newWebClient(final BrowserVersion version) {
                detector.append("newWebClient");
                final WebClient webClient = super.newWebClient(version);
                return webClient;
            }

            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                detector.append("-modifyWebClient");
                return super.modifyWebClient(client);
            }
        };

        assertTrue(webDriver instanceof HtmlUnitDriver);
        assertEquals("newWebClient-modifyWebClient", detector.toString());
    }

    @Test
    public void ctorWebClient() {
        new HtmlUnitDriver() {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

                assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                        client.getOptions().isJavaScriptEnabled());
                assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientJsFalse() {
        new HtmlUnitDriver(false) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

                assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                        client.getOptions().isJavaScriptEnabled());
                assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientJsTrue() {
        new HtmlUnitDriver(true) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

                assertTrue("client.getOptions().isJavaScriptEnabled() is false",
                        client.getOptions().isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEnabled() is false", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientBrowserVersionChrome() {
        new HtmlUnitDriver(BrowserVersion.CHROME) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.CHROME, client.getBrowserVersion());

                assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                        client.getOptions().isJavaScriptEnabled());
                assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientBrowserVersionEdge() {
        new HtmlUnitDriver(BrowserVersion.EDGE) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.EDGE, client.getBrowserVersion());

                assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                        client.getOptions().isJavaScriptEnabled());
                assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientBrowserVersionFirefox() {
        new HtmlUnitDriver(BrowserVersion.FIREFOX) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.FIREFOX, client.getBrowserVersion());

                assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                        client.getOptions().isJavaScriptEnabled());
                assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientBrowserVersionFirefoxEsr() {
        new HtmlUnitDriver(BrowserVersion.FIREFOX_ESR) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                        client.getOptions().isJavaScriptEnabled());
                assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientBrowserVersionIE() {
        new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.INTERNET_EXPLORER, client.getBrowserVersion());

                assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                        client.getOptions().isJavaScriptEnabled());
                assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientBrowserVersionFirefoxJsFalse() {
        new HtmlUnitDriver(BrowserVersion.FIREFOX, false) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.FIREFOX, client.getBrowserVersion());

                assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                        client.getOptions().isJavaScriptEnabled());
                assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientBrowserVersionFirefoxEsrJsTrue() {
        new HtmlUnitDriver(BrowserVersion.FIREFOX_ESR, true) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {

                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                assertTrue("client.getOptions().isJavaScriptEnabled() is false",
                        client.getOptions().isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEnabled() is false", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientCapabilitiesVersionString() {
        DesiredCapabilities capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox",
                Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "googlechrome", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.CHROME, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "MicrosoftEdge", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.EDGE, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-78", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-91", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-102", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-115", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-esr", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientCapabilitiesVersion() {
        DesiredCapabilities capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(),
                Browser.FIREFOX.browserName(), Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), Browser.CHROME.browserName(),
                Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.CHROME, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), Browser.EDGE.browserName(),
                Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.EDGE, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-78", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-91", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-102", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-115", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };

        capabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-esr", Platform.ANY);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.FIREFOX_ESR, client.getBrowserVersion());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientJsEnabledTrue() {
        final DesiredCapabilities capabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "", Platform.ANY);

        final HtmlUnitDriver driver = new HtmlUnitDriver(capabilities);
        assertTrue("client.getOptions().isJavaScriptEnabled() is false", driver.isJavascriptEnabled());
        assertTrue("client.getOptions().isJavaScriptEnabled() is true",
                driver.getWebClient().getOptions().isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEnabled() is true", driver.getWebClient().isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", driver.getWebClient().isJavaScriptEngineEnabled());

        driver.setJavascriptEnabled(false);
        assertFalse("client.getOptions().isJavaScriptEnabled() is true", driver.isJavascriptEnabled());
        assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                driver.getWebClient().getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", driver.getWebClient().isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", driver.getWebClient().isJavaScriptEngineEnabled());
    }

    @Test
    public void ctorWebClientJsEnabledFalse() {
        final DesiredCapabilities capabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "", Platform.ANY);

        final HtmlUnitDriver driver = new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

                client.getOptions().setJavaScriptEnabled(false);

                return client;
            }
        };

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", driver.isJavascriptEnabled());
        assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                driver.getWebClient().getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", driver.getWebClient().isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", driver.getWebClient().isJavaScriptEngineEnabled());
    }

    @Test
    public void ctorWebClientCapabilitiesProxy() {
        final Proxy proxy = new Proxy();
        proxy.setHttpProxy("hostname:1234");

        final DesiredCapabilities capabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "", Platform.ANY);
        capabilities.setCapability(CapabilityType.PROXY, proxy);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

                assertTrue("client.getOptions().isJavaScriptEnabled() is false",
                        client.getOptions().isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEnabled() is false", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                assertEquals("hostname", client.getOptions().getProxyConfig().getProxyHost());
                assertEquals(1234, client.getOptions().getProxyConfig().getProxyPort());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientCapabilitiesJsTrue() {
        final DesiredCapabilities capabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "", Platform.ANY);
        capabilities.setCapability(HtmlUnitDriver.JAVASCRIPT_ENABLED, true);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

                assertTrue("client.getOptions().isJavaScriptEnabled() is false",
                        client.getOptions().isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEnabled() is false", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void ctorWebClientCapabilitiesJsFalse() {
        final DesiredCapabilities capabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "", Platform.ANY);
        capabilities.setCapability(HtmlUnitDriver.JAVASCRIPT_ENABLED, false);

        new HtmlUnitDriver(capabilities) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

                assertFalse("client.getOptions().isJavaScriptEnabled() is true",
                        client.getOptions().isJavaScriptEnabled());
                assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
                assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

                return client;
            }
        };
    }

    @Test
    public void resetWebClient() {
        final HtmlUnitDriver webDriver = new HtmlUnitDriver();

        webDriver.get("https://www.htmlunit.org");
        webDriver.getWebClient().reset();

        webDriver.get("https://www.htmlunit.org");
    }
}
