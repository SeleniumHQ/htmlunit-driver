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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;

/**
 * Separate test class for the HtmlUnitWebElement.getCssValue(String) method.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementCssTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("block")
    public void defaultStyle() throws Exception {
        final String html =
            "<html>\n"
            + "<head></head>\n"
            + "<body>\n"
            + "  <div id='tester'>HtmlUnit</div>\n"
            + "</form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("tester"));
        assertNotNull(element);

        assertEquals(getExpectedAlerts()[0], element.getCssValue("display"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("none")
    public void styleAttribute() throws Exception {
        final String html =
            "<html>\n"
            + "<head></head>\n"
            + "<body>\n"
            + "  <div id='tester' style='display: none'>HtmlUnit</div>\n"
            + "</form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("tester"));
        assertNotNull(element);

        assertEquals(getExpectedAlerts()[0], element.getCssValue("display"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("inline")
    public void css() throws Exception {
        final String html =
            "<html>\n"
            + "<head>\n"
            + "<style>\n"
            + " div { display: inline}\n"
            + "</style>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <div id='tester'>HtmlUnit</div>\n"
            + "</form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("tester"));
        assertNotNull(element);

        assertEquals(getExpectedAlerts()[0], element.getCssValue("display"));
    }
}
