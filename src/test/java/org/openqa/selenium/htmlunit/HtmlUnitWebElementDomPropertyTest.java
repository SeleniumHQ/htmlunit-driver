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

/**
 * Separate test class for the HtmlUnitWebElement.getDomProperty(String) method.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementDomPropertyTest extends WebDriverTestCase {

    @Test
    public void domProperty() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId' name prop='' >TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement elem = driver.findElement(By.id("testDivId"));
        assertEquals("testDivId", elem.getDomProperty("id"));
        assertNull(elem.getDomProperty("unknown"));
        assertNull(elem.getDomProperty("name"));
        assertNull(elem.getDomProperty("prop"));
    }

    @Test
    @Alerts(DEFAULT = "null",
            IE = "true")
    public void unsupportedAttribute() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId' disabled>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement elem = driver.findElement(By.id("testDivId"));
        assertEquals(getExpectedAlerts()[0], "" + elem.getDomProperty("disabled"));
    }

    @Test
    public void disabled() throws Exception {
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
        assertEquals("true", elem.getDomProperty("disabled"));
    }

    @Test
    public void checkbox() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='checkbox' id='chkBx' name='chbox' value='dis' checked='checked' />\n"
                        + "    <input type='checkbox' id='chkBx2' name='chbox2' value='dis'/>\n"
                        + "    <input type='checkbox' id='chkBx3' name='chbox3' value='dis' checked='false' />\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("chkBx"));
        assertEquals("true", elem.getDomProperty("checked"));

        elem = driver.findElement(By.id("chkBx2"));
        assertEquals("false", elem.getDomProperty("checked"));

        elem = driver.findElement(By.id("chkBx3"));
        assertEquals("true", elem.getDomProperty("checked"));
    }

    @Test
    public void checkboxClicked() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='checkbox' id='chkBx' name='chbox' value='dis' />\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement elem = driver.findElement(By.id("chkBx"));
        assertEquals("false", elem.getDomProperty("checked"));
        elem.click();
        assertEquals("true", elem.getDomProperty("checked"));
    }

    @Test
    public void radio() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='radio' id='radioBx' name='radio' value='dis' checked='checked' />\n"
                        + "    <input type='radio' id='radioBx2' name='radio2' value='dis'/>\n"
                        + "    <input type='radio' id='radioBx3' name='radio3' value='dis' checked='false' />\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("radioBx"));
        assertEquals("true", elem.getDomProperty("checked"));

        elem = driver.findElement(By.id("radioBx2"));
        assertEquals("false", elem.getDomProperty("checked"));

        elem = driver.findElement(By.id("radioBx3"));
        assertEquals("true", elem.getDomProperty("checked"));
    }

    @Test
    public void radioClicked() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='radio' id='radioBx' name='radio' value='dis' />\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement elem = driver.findElement(By.id("radioBx"));
        assertEquals("false", elem.getDomProperty("checked"));
        elem.click();
        assertEquals("true", elem.getDomProperty("checked"));
    }

    @Test
    public void inputTextValue() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='text' id='textBx' name='text' value='world'/>\n"
                        + "    <input type='text' id='textBx2' name='text'/>\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("textBx"));
        assertEquals("world", elem.getDomProperty("value"));

        elem = driver.findElement(By.id("textBx2"));
        assertEquals("", elem.getDomProperty("value"));
    }

    @Test
    public void inputTextValueTyped() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='text' id='textBx' name='text' value='world'/>\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement elem = driver.findElement(By.id("textBx"));
        assertEquals("world", elem.getDomProperty("value"));
        elem.sendKeys("hello");
        assertEquals("worldhello", elem.getDomProperty("value"));
    }
}
