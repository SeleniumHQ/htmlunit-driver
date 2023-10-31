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

import org.htmlunit.BrowserVersion;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Browser;

/**
 * Determine browser and its version.
 *
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public final class BrowserVersionDeterminer {

    /**
     * Determine browser by its capabilities.
     *
     * @param capabilities the Capabilities
     * @return the browser version
     */
    public static BrowserVersion determine(final Capabilities capabilities) {
        if (!Browser.HTMLUNIT.is(capabilities)) {
            throw new IllegalArgumentException(
                    "When building an HtmlUntDriver, the capability browser name must be set to '"
                            + Browser.HTMLUNIT.browserName() + "' but was '" + capabilities.getBrowserName() + "'.");
        }

        final String browserName;
        final String browserVersion;

        final String rawVersion = capabilities.getBrowserVersion();
        final String[] splitVersion = rawVersion == null ? new String[0] : rawVersion.split("-");
        if (splitVersion.length > 1) {
            browserName = splitVersion[0];
            browserVersion = splitVersion[1];
        }
        else {
            browserName = capabilities.getBrowserVersion();
            browserVersion = null;
        }

        BrowserVersion browserVersionObject;

        if (browserName.equalsIgnoreCase(BrowserVersion.CHROME.getNickname())
                || "googlechrome".equalsIgnoreCase(browserName)) {
            browserVersionObject = BrowserVersion.CHROME;

        }
        else if (browserName.equalsIgnoreCase(BrowserVersion.EDGE.getNickname())
                || "MicrosoftEdge".equalsIgnoreCase(browserName)) {
            browserVersionObject = BrowserVersion.EDGE;

        }
        else if (browserName.equalsIgnoreCase(BrowserVersion.INTERNET_EXPLORER.getNickname())
                || "internet explorer".equalsIgnoreCase(browserName)) {
            browserVersionObject = BrowserVersion.INTERNET_EXPLORER;

        }
        else if (browserName.equalsIgnoreCase(BrowserVersion.FIREFOX.getNickname())
                || "firefox".equalsIgnoreCase(browserName)) {
            if ("esr".equalsIgnoreCase(browserVersion)) {
                browserVersionObject = BrowserVersion.FIREFOX_ESR;
            }
            else {
                try {
                    final int version = Integer.parseInt(browserVersion);
                    if (version == 78 || version == 91 || version == 102 || version == 115) {
                        browserVersionObject = BrowserVersion.FIREFOX_ESR;
                    }
                    else if (version == BrowserVersion.FIREFOX.getBrowserVersionNumeric()) {
                        browserVersionObject = BrowserVersion.FIREFOX;
                    }
                    else {
                        browserVersionObject = BrowserVersion.FIREFOX;
                    }
                }
                catch (final NumberFormatException e) {
                    browserVersionObject = BrowserVersion.FIREFOX;
                }
            }
        }
        else {
            browserVersionObject = BrowserVersion.getDefault();
        }

        final Object rawLanguage = capabilities.getCapability(HtmlUnitDriver.BROWSER_LANGUAGE_CAPABILITY);
        if (rawLanguage instanceof String) {
            return new BrowserVersion.BrowserVersionBuilder(browserVersionObject)
                    .setBrowserLanguage((String) rawLanguage).build();
        }

        return browserVersionObject;
    }

    private BrowserVersionDeterminer() {
    }
}
