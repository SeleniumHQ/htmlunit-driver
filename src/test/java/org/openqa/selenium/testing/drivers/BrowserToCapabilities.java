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

package org.openqa.selenium.testing.drivers;

import static org.openqa.selenium.remote.CapabilityType.HAS_NATIVE_EVENTS;

import org.openqa.selenium.Platform;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.DesiredCapabilities;

public class BrowserToCapabilities {

  public static DesiredCapabilities of(BrowserType browserType) {
    return of(browserType, "");
  }

  public static DesiredCapabilities of(BrowserType browserType, String version) {
    if (browserType == null) {
      return null;
    }

    DesiredCapabilities caps;

    switch (browserType) {
      case CHROME:
        caps = new DesiredCapabilities(Browser.CHROME.browserName(), version, Platform.ANY);
        break;

      case FIREFOX:
        caps = new DesiredCapabilities(Browser.FIREFOX.browserName(), version, Platform.ANY);
        String property = System.getProperty(FirefoxDriver.Capability.MARIONETTE, "true");
        boolean useMarionette = Boolean.parseBoolean(property);
        caps.setCapability(FirefoxDriver.Capability.MARIONETTE, useMarionette);
        break;

      case HTML_UNIT:
        caps = new DesiredCapabilities(Browser.HTMLUNIT.browserName(), version, Platform.ANY);
        break;

      case IE:
        caps = new DesiredCapabilities(Browser.IE.browserName(), version, Platform.WINDOWS);
        break;

      case OPERA_BLINK:
        caps = new DesiredCapabilities(Browser.OPERA.browserName(), version, Platform.ANY);
        break;

      case SAFARI:
        caps = new DesiredCapabilities(Browser.SAFARI.browserName(), version, Platform.MAC);
        break;

      default:
        throw new RuntimeException("Cannot determine browser config to use");
    }

    final String systemVersion = System.getProperty("selenium.browser.version");
    if (systemVersion != null) {
      caps.setVersion(systemVersion);
    }

    caps.setCapability(HAS_NATIVE_EVENTS, Boolean.getBoolean("selenium.browser.native_events"));

    return caps;
  }
}
