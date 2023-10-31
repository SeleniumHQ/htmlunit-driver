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

import static org.htmlunit.BrowserVersion.FIREFOX;
import static org.htmlunit.BrowserVersion.FIREFOX_ESR;
import static org.htmlunit.BrowserVersion.INTERNET_EXPLORER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openqa.selenium.htmlunit.HtmlUnitDriver.BROWSER_LANGUAGE_CAPABILITY;
import static org.openqa.selenium.htmlunit.HtmlUnitDriver.JAVASCRIPT_ENABLED;

import org.htmlunit.BrowserVersion;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Test the determineBrowserVersion method.
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Rafael Jimenez
 * @author Luke Inman-Semerau
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitCapabilitiesTest {

    @Test
    public void configurationViaDirectCapabilities() {
        final DesiredCapabilities ieCapabilities = new DesiredCapabilities(Browser.IE.browserName(), "", Platform.ANY);

        try {
            BrowserVersionDeterminer.determine(ieCapabilities);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("When building an HtmlUntDriver, the capability browser name "
                    + "must be set to 'htmlunit' but was 'internet explorer'.", e.getMessage());
        }
    }

    @Test
    public void configurationOfFirefoxDefaultViaRemote() {
        final DesiredCapabilities firefoxCapabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox",
                Platform.ANY);
        assertEquals(FIREFOX, BrowserVersionDeterminer.determine(firefoxCapabilities));
    }

    @Test
    public void configurationOfFirefox78ViaRemote() {
        final DesiredCapabilities firefoxCapabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-78",
                Platform.ANY);
        assertEquals(FIREFOX_ESR, BrowserVersionDeterminer.determine(firefoxCapabilities));
    }

    @Test
    public void configurationOfFirefox91ViaRemote() {
        final DesiredCapabilities firefoxCapabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-91",
                Platform.ANY);
        assertEquals(FIREFOX_ESR, BrowserVersionDeterminer.determine(firefoxCapabilities));
    }

    @Test
    public void configurationOfFirefox102ViaRemote() {
        final DesiredCapabilities firefoxCapabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-102",
                Platform.ANY);
        assertEquals(FIREFOX_ESR, BrowserVersionDeterminer.determine(firefoxCapabilities));
    }

    @Test
    public void configurationOfFirefox115ViaRemote() {
        final DesiredCapabilities firefoxCapabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-115",
                Platform.ANY);
        assertEquals(FIREFOX_ESR, BrowserVersionDeterminer.determine(firefoxCapabilities));
    }

    @Test
    public void configurationOfFirefoxEsrViaRemote() {
        final DesiredCapabilities firefoxCapabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "firefox-esr",
                Platform.ANY);
        assertEquals(FIREFOX_ESR, BrowserVersionDeterminer.determine(firefoxCapabilities));
    }

    @Test
    public void configurationOfIEViaRemote() {
        final DesiredCapabilities ieCapabilities = new DesiredCapabilities(Browser.HTMLUNIT.browserName(),
                "internet explorer", Platform.ANY);
        assertEquals(INTERNET_EXPLORER, BrowserVersionDeterminer.determine(ieCapabilities));
    }

    @Test
    public void tetsDefautlBrowserVersion() {
        final DesiredCapabilities capabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "", Platform.ANY);

        assertEquals(BrowserVersion.getDefault(), BrowserVersionDeterminer.determine(capabilities));
    }

    @Test
    public void htmlUnitReportsCapabilities() {
        HtmlUnitDriver driver = new HtmlUnitDriver(true);
        final Capabilities jsEnabled = driver.getCapabilities();
        driver.quit();

        driver = new HtmlUnitDriver(false);
        final Capabilities jsDisabled = driver.getCapabilities();
        assertTrue(jsEnabled.is(JAVASCRIPT_ENABLED));
        assertFalse(jsDisabled.is(JAVASCRIPT_ENABLED));
    }

    @Test
    public void configurationOfBrowserLanguage() {
        final String browserLanguage = "es-ES";

        final DesiredCapabilities capabilities =
                new DesiredCapabilities(Browser.HTMLUNIT.browserName(), "", Platform.ANY);
        capabilities.setCapability(BROWSER_LANGUAGE_CAPABILITY, browserLanguage);

        assertEquals(browserLanguage, BrowserVersionDeterminer.determine(capabilities).getBrowserLanguage());
    }

}
