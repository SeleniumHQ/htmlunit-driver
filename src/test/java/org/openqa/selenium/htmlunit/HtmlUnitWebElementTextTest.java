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
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.HtmlUnitNYI;

/**
 * Separate test class for the HtmlUnitWebElement.getDomProperty(String) method.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementTextTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(CHROME = "    option1\n     Number Three\n    Number 4\n  ",
            EDGE = "    option1\n     Number Three\n    Number 4\n  ",
            FF = "option1\nNumber Three\nNumber 4",
            FF_ESR = "option1\nNumber Three\nNumber 4",
            IE = "option1 Number Three Number 4")
    @HtmlUnitNYI(CHROME = "option1\nNumber Three\nNumber 4",
            EDGE = "option1\nNumber Three\nNumber 4",
            IE = "option1\nNumber Three\nNumber 4")
    public void select() throws Exception {
        final String html =
            "<html>\n"
            + "<head></head>\n"
            + "<body>\n"
            + "<form>\n"
            + "  <select id='tester'>\n"
            + "    <option id='option1'>option1</option>\n"
            + "    <option id='option2' label='Number Two'/>\n"
            + "    <option id='option3' label='overridden'>Number Three</option>\n"
            + "    <option id='option4'>Number&nbsp;4</option>\n"
            + "  </select>\n"
            + "</form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("tester"));
        assertNotNull(element);

        assertEquals(getExpectedAlerts()[0], element.getText());

    }
}
