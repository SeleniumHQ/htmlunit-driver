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

/**
 * Separate test class for the HtmlUnitWebElement.getAttribute(String) method.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementEnabledTest extends WebDriverTestCase {

    @Test
    public void div() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement elem = driver.findElement(By.id("testDivId"));
        assertTrue(elem.isEnabled());
    }

    @Test
    public void divDisabled() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId' disabled='disabled'>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement elem = driver.findElement(By.id("testDivId"));
        assertTrue(elem.isEnabled());
    }

    @Test
    public void checkboxDisabled() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='checkbox' id='chkBx' name='chbox' value='dis' disabled>disabled\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement elem = driver.findElement(By.id("chkBx"));
        assertFalse(elem.isEnabled());
    }
}
