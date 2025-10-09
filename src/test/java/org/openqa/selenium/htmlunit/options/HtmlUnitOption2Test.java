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

package org.openqa.selenium.htmlunit.options;

import java.time.Duration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;

/**
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitOption2Test extends WebDriverTestCase {

    @Test
    @Alerts({"300", "30", "0"})
    public void defaultTimeouts() throws Exception {
        shutDownAll();
        final WebDriver webDriver = getWebDriver();

        assertEquals(getExpectedAlerts()[0], "" + webDriver.manage().timeouts().getPageLoadTimeout().getSeconds());
        assertEquals(getExpectedAlerts()[1], "" + webDriver.manage().timeouts().getScriptTimeout().getSeconds());
        assertEquals(getExpectedAlerts()[2], "" + webDriver.manage().timeouts().getImplicitWaitTimeout().getNano());
    }

    @Test
    @Alerts({"300", "1"})
    public void changePageLoadTimeout() throws Exception {
        shutDownAll();
        final WebDriver webDriver = getWebDriver();

        assertEquals(getExpectedAlerts()[0], "" + webDriver.manage().timeouts().getPageLoadTimeout().getSeconds());
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(1));
        assertEquals(getExpectedAlerts()[1], "" + webDriver.manage().timeouts().getPageLoadTimeout().getSeconds());
    }

    @Test
    @Alerts({"30", "1"})
    public void changeScriptTimeout() throws Exception {
        shutDownAll();
        final WebDriver webDriver = getWebDriver();

        assertEquals(getExpectedAlerts()[0], "" + webDriver.manage().timeouts().getScriptTimeout().getSeconds());
        webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));
        assertEquals(getExpectedAlerts()[1], "" + webDriver.manage().timeouts().getScriptTimeout().getSeconds());
    }

    @Test
    @Alerts({"0", "1"})
    public void changeImplicitWaitTimeout() throws Exception {
        shutDownAll();
        final WebDriver webDriver = getWebDriver();

        assertEquals(getExpectedAlerts()[0], "" + webDriver.manage().timeouts().getImplicitWaitTimeout().getSeconds());
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        assertEquals(getExpectedAlerts()[1], "" + webDriver.manage().timeouts().getImplicitWaitTimeout().getSeconds());
    }
}
