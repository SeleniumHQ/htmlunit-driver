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

@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void outerHTML() throws Exception {
        final String html = "<html><head></head>\n" + "</html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement head = webDriver.findElement(By.xpath("//head"));
        assertEquals("<head></head>", head.getAttribute("outerHTML"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void outerhtml() throws Exception {
        final String html = "<html><head></head>\n" + "</html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement head = webDriver.findElement(By.xpath("//head"));
        assertNull(head.getAttribute("outerhtml"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void innterHTML() throws Exception {
        final String html = "<html><head><title>abc</title></head>\n" + "</html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement head = webDriver.findElement(By.xpath("//head"));
        assertEquals("<title>abc</title>", head.getAttribute("innerHTML"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void innterhtml() throws Exception {
        final String html = "<html><head><title>abc</title></head>\n" + "</html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement head = webDriver.findElement(By.xpath("//head"));
        assertNull(head.getAttribute("innerhtml"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void title() throws Exception {
        final String html = "<html><head><title>abc</title></head>\n" + "<body></body>\n" + "</html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement body = webDriver.findElement(By.tagName("body"));
        assertEquals("", body.getAttribute("title"));
    }

    /**
     * See https://github.com/HtmlUnit/htmlunit/issues/142.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void clearHasToFixTheSelectionAlso() throws Exception {
        final String html = "<html>\n" + "<head><title>abc</title></head>\n" + "<body>\n" + "<form id='form1'>\n"
                + "  <input id='foo' type='text' value='0815'>\n" + "</form>\n" + "</body>\n" + "</html>";
        final WebDriver webDriver = loadPage2(html);

        final WebElement input = webDriver.findElement(By.id("foo"));
        input.clear();
        input.sendKeys("4711");
    }

    @Test
    public void tagName() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "  <DIV id='testDivId2'>TestDiv</DIV>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        WebElement elem = driver.findElement(By.id("testDivId"));
        assertEquals("div", elem.getTagName());

        elem = driver.findElement(By.id("testDivId2"));
        assertEquals("div", elem.getTagName());
    }

    @Test
    @Alerts(DEFAULT = "null",
            IE = "true")
    public void domProperty() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId' name prop='' >TestDiv</div>\n"
                        + "  <div id='testDivId2' disabled>TestDiv</div>\n"
                        + "  <div id='testDivId3' disabled='disabled'>TestDiv</div>\n"
                        + "  <fieldset>\n"
                        + "    <input type='checkbox' id='chkBx' name='chbox' value='dis' disabled>disabled\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        WebElement elem = driver.findElement(By.id("testDivId"));
        assertEquals("testDivId", elem.getDomProperty("id"));
        assertNull(elem.getDomProperty("unknown"));
        assertNull(elem.getDomProperty("name"));
        assertNull(elem.getDomProperty("prop"));

        elem = driver.findElement(By.id("testDivId2"));
        assertEquals(getExpectedAlerts()[0], "" + elem.getDomProperty("disabled"));

        elem = driver.findElement(By.id("testDivId3"));
        assertEquals(getExpectedAlerts()[0], "" + elem.getDomProperty("disabled"));

        elem = driver.findElement(By.id("chkBx"));
        assertEquals("chbox", elem.getDomProperty("name"));
        assertEquals("dis", elem.getDomProperty("value"));
        assertEquals("true", elem.getDomProperty("disabled"));
    }

    @Test
    @Alerts(DEFAULT = {"true", "true"},
            FF = {"", "disabled"},
            FF78 = {"", "disabled"})
    public void domAttribute() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId' name prop='' >TestDiv</div>\n"
                        + "  <div id='testDivId2' disabled>TestDiv</div>\n"
                        + "  <div id='testDivId3' disabled='disabled'>TestDiv</div>\n"
                        + "  <fieldset>\n"
                        + "    <input type='checkbox' id='chkBx' name='chbox' value='dis' disabled>disabled\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        WebElement elem = driver.findElement(By.id("testDivId"));
        assertEquals("testDivId", elem.getDomAttribute("id"));
        assertNull(elem.getDomAttribute("unknown"));
        assertEquals("", elem.getDomAttribute("name"));
        assertEquals("", elem.getDomAttribute("prop"));

        elem = driver.findElement(By.id("testDivId2"));
        assertEquals(getExpectedAlerts()[0], elem.getDomAttribute("disabled"));

        elem = driver.findElement(By.id("testDivId3"));
        assertEquals(getExpectedAlerts()[1], elem.getDomAttribute("disabled"));

        elem = driver.findElement(By.id("chkBx"));
        assertEquals("chbox", elem.getDomAttribute("name"));
        assertEquals("dis", elem.getDomAttribute("value"));
        assertEquals("true", elem.getDomAttribute("disabled"));
    }

    @Test
    public void attribute() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId' name prop='' >TestDiv</div>\n"
                        + "  <div id='testDivId2' disabled>TestDiv</div>\n"
                        + "  <div id='testDivId3' disabled='disabled'>TestDiv</div>\n"
                        + "  <fieldset>\n"
                        + "    <input type='checkbox' id='chkBx' name='chbox' value='dis' disabled>disabled\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        WebElement elem = driver.findElement(By.id("testDivId"));
        assertEquals("testDivId", elem.getAttribute("id"));
        assertNull(elem.getAttribute("unknown"));
        assertEquals("", elem.getAttribute("name"));
        assertEquals("", elem.getAttribute("prop"));

        elem = driver.findElement(By.id("testDivId2"));
        assertEquals("true", elem.getAttribute("disabled"));

        elem = driver.findElement(By.id("testDivId3"));
        assertEquals("true", elem.getAttribute("disabled"));

        elem = driver.findElement(By.id("chkBx"));
        assertEquals("chbox", elem.getAttribute("name"));
        assertEquals("dis", elem.getAttribute("value"));
        assertEquals("true", elem.getAttribute("disabled"));
    }

    // @Test
    public void memoryLeak() throws Exception {
        final int elements = 1000;
        String html = "<html><head><title>abc</title></head>\n" + "<body>\n";
        for (int i = 0; i < elements; i++) {
            html += "<div id='id" + i + "'>abcd</div>\n";
        }
        html += "</body>\n" + "</html>";

        for (int i = 0; i < 10000; i++) {
            final WebDriver webDriver = loadPage2(html);
            for (int j = 0; j < elements; j++) {
                webDriver.findElement(By.id("id" + j));
            }
        }
    }
}
