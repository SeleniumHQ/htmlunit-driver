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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.BrowserRunner.Alerts;

/**
 * Separate test class for the HtmlUnitWebElement.getDomAttribute(String) method.
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementDomAttributeTest extends WebDriverTestCase {


    @Test
    public void domAttribute() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId' name prop='' >TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        WebElement elem = driver.findElement(By.id("testDivId"));
        assertEquals("testDivId", elem.getDomAttribute("id"));
        assertNull(elem.getDomAttribute("unknown"));
        assertEquals("", elem.getDomAttribute("name"));
        assertEquals("", elem.getDomAttribute("prop"));
    }

    @Test
    @Alerts(DEFAULT = "true",
            FF = "",
            FF78 = "")
    public void unsupportedAttribute() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId' disabled>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        WebElement elem = driver.findElement(By.id("testDivId"));
        assertEquals(getExpectedAlerts()[0], elem.getDomAttribute("disabled"));
    }

    @Test
    @Alerts(DEFAULT = {"true", "true"},
            FF = {"", "disabled"},
            FF78 = {"", "disabled"})
    public void disabled() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='checkbox' id='chkBx' name='chbox' value='dis' disabled>disabled\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        WebElement elem = driver.findElement(By.id("chkBx"));
        assertEquals("true", elem.getDomAttribute("disabled"));
    }
}
