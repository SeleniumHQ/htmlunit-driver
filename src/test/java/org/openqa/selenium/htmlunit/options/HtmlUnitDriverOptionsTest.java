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
import static org.openqa.selenium.htmlunit.options.BrowserVersionTraitNames.optSystemTimezone;
import static org.openqa.selenium.htmlunit.options.HtmlUnitOption.HOME_PAGE;
import static org.openqa.selenium.htmlunit.options.HtmlUnitOption.PRINT_CONTENT_ON_FAILING_STATUS_CODE;
import static org.openqa.selenium.htmlunit.options.HtmlUnitOption.THROW_EXCEPTION_ON_FAILING_STATUS_CODE;
import static org.openqa.selenium.htmlunit.options.HtmlUnitOption.USE_INSECURE_SSL;
import static org.openqa.selenium.htmlunit.options.HtmlUnitOption.WEB_CLIENT_VERSION;
import static org.openqa.selenium.htmlunit.options.HtmlUnitOptionNames.optWebClientVersion;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.htmlunit.BrowserVersion;
import org.htmlunit.util.UrlUtils;
import org.junit.Test;
import org.openqa.selenium.remote.Browser;

/**
 * HtmlUnitDriverOptions tests.
 *
 * @author Scott Babcock
 */
public class HtmlUnitDriverOptionsTest {

    @Test
    public void newOptionsWithoutArguments() {
        final HtmlUnitDriverOptions options = new HtmlUnitDriverOptions();
        verifyOptions(options, BrowserVersion.BEST_SUPPORTED);
    }

    @Test
    public void newOptionsWithChromeVersion() {
        final HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.CHROME);
        verifyOptions(options, BrowserVersion.CHROME);
    }

    @Test
    public void newOptionsWithEdgeVersion() {
        final HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.EDGE);
        verifyOptions(options, BrowserVersion.EDGE);
    }

    @Test
    public void newOptionsWithFirefoxVersion() {
        final HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.FIREFOX);
        verifyOptions(options, BrowserVersion.FIREFOX);
    }

    @Test
    public void newOptionsWithFirefoxESRVersion() {
        final HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.FIREFOX_ESR);
        verifyOptions(options, BrowserVersion.FIREFOX_ESR);
    }

    @Test
    public void verifyEncodeAndDecode() {
        final HtmlUnitDriverOptions options = new HtmlUnitDriverOptions();
        final Map<String, Object> optionsMap = options.asMap();
        final HtmlUnitDriverOptions decoded = new HtmlUnitDriverOptions(optionsMap);
        assertEquals("Options object serialize/deserialize mismatch", options, decoded);
    }

    @Test
    public void verifyFirefoxESRBrowserVersion() {
        final HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.FIREFOX_ESR);
        final BrowserVersion browserVersion = (BrowserVersion) options.getCapability(optWebClientVersion);
        assertEquals("Browser version mismatch", BrowserVersion.FIREFOX_ESR, browserVersion);
    }

    @Test
    public void verifyEuropeBerlinTimeZone() {
        // NOTE: Don't follow this convoluted process to set the system time zone!
        //       This test is verifying encode/decode of BrowserVersion and HtmlUnitDriverOptions.
        final Map<String, Object> encoded = TypeCodec.encodeBrowserVersion(BrowserVersion.BEST_SUPPORTED);
        encoded.put(optSystemTimezone, "Europe/Berlin");
        final HtmlUnitDriverOptions options = new HtmlUnitDriverOptions();
        options.setCapability(optWebClientVersion, encoded);
        final TimeZone timeZone = (TimeZone) options.getCapability(optSystemTimezone);
        assertEquals("Time zone mismatch", "Europe/Berlin", timeZone.getID());
    }

    private static void verifyOptions(final HtmlUnitDriverOptions options, final BrowserVersion browserVersion) {
        assertEquals("Browser name mismatch", Browser.HTMLUNIT.browserName(), options.getBrowserName());
        BrowserVersionTraitTest.verify(browserVersion, options.getWebClientVersion());

        final Map<HtmlUnitOption, Object> nonDefault = getNonDefaultOptions(options);
        final Set<HtmlUnitOption> expectKeySet = Set.of(WEB_CLIENT_VERSION,
                HOME_PAGE, PRINT_CONTENT_ON_FAILING_STATUS_CODE,
                THROW_EXCEPTION_ON_FAILING_STATUS_CODE, USE_INSECURE_SSL);

        assertEquals("Non-default value set mismatch", expectKeySet, nonDefault.keySet());

        final BrowserVersion decodedVersion =
                (BrowserVersion) WEB_CLIENT_VERSION.decode(nonDefault.get(WEB_CLIENT_VERSION));
        BrowserVersionTraitTest.verify(browserVersion, decodedVersion);

        assertEquals("Mismatch for option: homePage", UrlUtils.URL_ABOUT_BLANK.toString(), nonDefault.get(HOME_PAGE));
        assertEquals("Mismatch for option: printContentOnFailingStatusCode", false,
                nonDefault.get(PRINT_CONTENT_ON_FAILING_STATUS_CODE));
        assertEquals("Mismatch for option: throwExceptionOnFailingStatusCode", false,
                nonDefault.get(THROW_EXCEPTION_ON_FAILING_STATUS_CODE));
        assertEquals("Mismatch for option: useInsecureSSL", true, nonDefault.get(USE_INSECURE_SSL));
    }

    @SuppressWarnings("unchecked")
    private static Map<HtmlUnitOption, Object> getNonDefaultOptions(final HtmlUnitDriverOptions options) {
        final Map<HtmlUnitOption, Object> result = new HashMap<>();
        final Map<String, Object> extraOptions =
                (Map<String, Object>) options.getExtraCapability(HtmlUnitDriverOptions.HTMLUNIT_OPTIONS);
        for (final HtmlUnitOption option : HtmlUnitOption.values()) {
            if (extraOptions.containsKey(option.getCapabilityKey())) {
                result.put(option, extraOptions.get(option.getCapabilityKey()));
            }
        }
        return result;
    }
}
