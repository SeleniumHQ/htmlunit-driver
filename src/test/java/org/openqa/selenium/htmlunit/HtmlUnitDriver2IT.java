// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit;

import org.htmlunit.MockWebConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.HtmlUnitNYI;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.Assert.fail;

/**
 * General tests for the HtmlUnitDriver. (External Domain Tests)
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitDriver2IT extends WebDriverTestCase {

    @Test
    @Alerts(DEFAULT = "Privacy error",
            FF = "self-signed.badssl.com",
            FF_ESR = "self-signed.badssl.com")
    @HtmlUnitNYI(CHROME = "self-signed.badssl.com",
            EDGE = "self-signed.badssl.com")
    public void getSslSelfSigned() throws Exception {
        final WebDriver webDriver = getWebDriver();

        webDriver.get("https://self-signed.badssl.com");
        assertEquals(getExpectedAlerts()[0], webDriver.getTitle());
        assertEquals("https://self-signed.badssl.com/", webDriver.getCurrentUrl());
    }

    @Test
    @Alerts(DEFAULT = "Privacy error",
            FF = "wrong.host.badssl.com",
            FF_ESR = "wrong.host.badssl.com")
    @HtmlUnitNYI(CHROME = "wrong.host.badssl.com",
            EDGE = "wrong.host.badssl.com")
    public void getSslWrongHost() throws Exception {
        final WebDriver webDriver = getWebDriver();

        webDriver.get("https://wrong.host.badssl.com/");
        assertEquals(getExpectedAlerts()[0], webDriver.getTitle());
        assertEquals("https://wrong.host.badssl.com/", webDriver.getCurrentUrl());
    }

    @Test
    @Alerts("revoked.badssl.com")
    public void getSslRevoked() throws Exception {
        final WebDriver webDriver = getWebDriver();

        webDriver.get("https://revoked.badssl.com");
        assertEquals(getExpectedAlerts()[0], webDriver.getTitle());
        assertEquals("https://revoked.badssl.com/", webDriver.getCurrentUrl());
    }
}
