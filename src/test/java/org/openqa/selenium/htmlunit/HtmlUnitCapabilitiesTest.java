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

import static com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX;
import static com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX_78;
import static com.gargoylesoftware.htmlunit.BrowserVersion.INTERNET_EXPLORER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openqa.selenium.htmlunit.HtmlUnitDriver.BROWSER_LANGUAGE_CAPABILITY;
import static org.openqa.selenium.htmlunit.HtmlUnitDriver.JAVASCRIPT_ENABLED;

import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Test the determineBrowserVersion method.
 */
public class HtmlUnitCapabilitiesTest {

  @Test
  public void configurationViaDirectCapabilities() {
    DesiredCapabilities ieCapabilities = new DesiredCapabilities(BrowserType.IE, "", Platform.ANY);

    try {
        BrowserVersionDeterminer.determine(ieCapabilities);
        fail("IllegalArgumentException expected");
    }
    catch (IllegalArgumentException e) {
        assertEquals("When building an HtmlUntDriver, the capability browser name "
                        + "must be set to 'htmlunit' but was 'internet explorer'.", e.getMessage());
    }
  }

  @Test
  public void configurationOfFirefoxDefaultViaRemote() {
    DesiredCapabilities firefoxCapabilities =
        new DesiredCapabilities(BrowserType.HTMLUNIT, "firefox", Platform.ANY);

    assertEquals(FIREFOX, BrowserVersionDeterminer.determine(firefoxCapabilities));
  }

  @Test
  public void configurationOfFirefox78ViaRemote() {
    DesiredCapabilities firefoxCapabilities =
        new DesiredCapabilities(BrowserType.HTMLUNIT, "firefox-78", Platform.ANY);

    assertEquals(FIREFOX_78, BrowserVersionDeterminer.determine(firefoxCapabilities));
  }

  @Test
  public void configurationOfIEViaRemote() {
    DesiredCapabilities ieCapabilities =
        new DesiredCapabilities(BrowserType.HTMLUNIT, "internet explorer", Platform.ANY);

    assertEquals(INTERNET_EXPLORER, BrowserVersionDeterminer.determine(ieCapabilities));
  }

  @Test
  public void tetsDefautlBrowserVersion() {
    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();

    assertEquals(BrowserVersion.getDefault(), BrowserVersionDeterminer.determine(capabilities));
  }

  @Test
  public void htmlUnitReportsCapabilities() {
    HtmlUnitDriver driver = new HtmlUnitDriver(true);
    Capabilities jsEnabled = driver.getCapabilities();
    driver.quit();

    driver = new HtmlUnitDriver(false);
    Capabilities jsDisabled = driver.getCapabilities();
    assertTrue(jsEnabled.is(JAVASCRIPT_ENABLED));
    assertFalse(jsDisabled.is(JAVASCRIPT_ENABLED));
  }

  @Test
  public void configurationOfBrowserLanguage() {
    String browserLanguage = "es-ES";

    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setCapability(BROWSER_LANGUAGE_CAPABILITY, browserLanguage);

    assertEquals(browserLanguage, BrowserVersionDeterminer.determine(capabilities).getBrowserLanguage());
  }

}
