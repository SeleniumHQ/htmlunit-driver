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

package org.openqa.selenium.htmlunit.html5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.ApplicationCache;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.html5.LocationContext;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.HtmlUnitNYI;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.testing.JUnit4TestBase;

/**
 * Tests for WebStorage support.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class ApplicationCacheTest extends WebDriverTestCase {

    @Test
    @Alerts("ApplicationCache not supported")
    @HtmlUnitNYI(CHROME = "ApplicationCache not supported",
            EDGE = "ApplicationCache not supported",
            FF = "ApplicationCache not supported",
            FF_ESR = "ApplicationCache not supported",
            IE = "ApplicationCache not supported")
    public void applicationCache() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        List<String> collectedAlerts = getCollectedAlerts(driver);
        if (driver instanceof ApplicationCache) {
            collectedAlerts.add("ApplicationCache supported");
        } else {
            collectedAlerts.add("ApplicationCache not supported");
        }

        assertEquals(getExpectedAlerts(), collectedAlerts);
    }
}
