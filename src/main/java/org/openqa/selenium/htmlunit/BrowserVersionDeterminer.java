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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Browser;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Determine browser and its version
 */
public class BrowserVersionDeterminer {
    protected static final List<BrowserInfo> browsers = new ArrayList<>();

    static {
        browsers.add(new Chrome());
        browsers.add(new Edge());
        browsers.add(new Firefox());
        browsers.add(new IE());
    }

    /**
     * Determine browser by its capabilities
     */
    public static BrowserVersion determine(Capabilities capabilities) {
        if (!Browser.HTMLUNIT.is(capabilities)) {
            throw new IllegalArgumentException("When building an HtmlUntDriver, the capability browser name must be set to '"
                    + Browser.HTMLUNIT.browserName() + "' but was '" + capabilities.getBrowserName() + "'.");
        }

        String browserName;
        String browserVersion;

        String rawVersion = capabilities.getBrowserVersion();
        String[] splitVersion = rawVersion == null ? new String[0] : rawVersion.split("-");
        if (splitVersion.length > 1) {
            browserName = splitVersion[0];
            browserVersion = splitVersion[1];
        } else {
            browserName = capabilities.getBrowserVersion();
            browserVersion = null;
        }

        if (browserName == null) {
            return BrowserVersion.getDefault();
        }

        final BrowserVersion result = browsers.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getBrowser().is(browserName))
                .findFirst()
                .map(item -> item.getBrowserVersion(browserVersion))
                .orElse(BrowserVersion.getDefault());

        Object rawLanguage = capabilities.getCapability(HtmlUnitDriver.BROWSER_LANGUAGE_CAPABILITY);
        if (rawLanguage instanceof String) {
            return new BrowserVersion.BrowserVersionBuilder(result)
                    .setBrowserLanguage((String) rawLanguage).build();
        }

        return result;
    }

    /**
     * Basic browser info
     */
    protected interface BrowserInfo {
        Browser getBrowser();

        BrowserVersion getBrowserVersion();

        default BrowserVersion getBrowserVersion(String versionNumeric) {
            return getBrowserVersion();
        }
    }

    protected static class Chrome implements BrowserInfo {
        @Override
        public Browser getBrowser() {
            return Browser.CHROME;
        }

        @Override
        public BrowserVersion getBrowserVersion() {
            return BrowserVersion.CHROME;
        }
    }

    protected static class Edge implements BrowserInfo {
        @Override
        public Browser getBrowser() {
            return Browser.EDGE;
        }

        @Override
        public BrowserVersion getBrowserVersion() {
            return BrowserVersion.EDGE;
        }
    }

    protected static class IE implements BrowserInfo {
        @Override
        public Browser getBrowser() {
            return Browser.IE;
        }

        @Override
        public BrowserVersion getBrowserVersion() {
            return BrowserVersion.INTERNET_EXPLORER;
        }
    }

    protected static class Firefox implements BrowserInfo {
        @Override
        public Browser getBrowser() {
            return Browser.FIREFOX;
        }

        @Override
        public BrowserVersion getBrowserVersion() {
            return BrowserVersion.FIREFOX;
        }

        @Override
        public BrowserVersion getBrowserVersion(String versionNumeric) {
            try {
                int version = Integer.parseInt(versionNumeric);

                return BrowserVersion.FIREFOX_78.getBrowserVersionNumeric() == version ?
                        BrowserVersion.FIREFOX_78 :
                        BrowserVersion.FIREFOX;

            } catch (NumberFormatException e) {
                return BrowserVersion.FIREFOX;
            }
        }
    }
}