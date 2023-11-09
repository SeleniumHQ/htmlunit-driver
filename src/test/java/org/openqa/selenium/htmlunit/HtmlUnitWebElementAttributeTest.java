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
public class HtmlUnitWebElementAttributeTest extends WebDriverTestCase {

    @Test
    public void attribute() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId' name prop='' >TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement elem = driver.findElement(By.id("testDivId"));
        assertEquals("testDivId", elem.getAttribute("id"));
        assertNull(elem.getAttribute("unknown"));
        assertEquals("", elem.getAttribute("name"));
        assertEquals("", elem.getAttribute("prop"));
    }

    @Test
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
        assertEquals("true", elem.getAttribute("disabled"));
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
        assertEquals("true", elem.getAttribute("disabled"));
    }

    @Test
    public void valueOption() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "<form>\n"
                        + "  <select id='selectWithRandomMultipleValue' multiple='somethingElse'>\n"
                        + "    <option id='o1' value='one'>option one</option>\n"
                        + "    <option id='o2'>option two</option>\n"
                        + "    <option id='o3'>option  three    \n    second line</option>\n"
                        + "    <option id='o4' value='4'>option four</option>\n"
                        + "  </select>\n"
                        + "</form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("o1"));
        assertEquals("one", elem.getAttribute("value"));

        elem = driver.findElement(By.id("o2"));
        assertEquals("option two", elem.getAttribute("value"));

        elem = driver.findElement(By.id("o3"));
        assertEquals("option three second line", elem.getAttribute("value"));

        elem = driver.findElement(By.id("o4"));
        assertEquals("4", elem.getAttribute("value"));
    }

    @Test
    public void indexOption() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "<form>\n"
                        + "  <select id='selectWithRandomMultipleValue' multiple='somethingElse'>\n"
                        + "    <option id='o1' value='one'>option one</option>\n"
                        + "    <option id='o2'>option two</option>\n"
                        + "    <option id='o3'>option  three    \n    second line</option>\n"
                        + "    <option id='o4' value='4'>option four</option>\n"
                        + "  </select>\n"
                        + "</form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("o1"));
        assertEquals("0", elem.getAttribute("index"));

        elem = driver.findElement(By.id("o2"));
        assertEquals("1", elem.getAttribute("index"));

        elem = driver.findElement(By.id("o3"));
        assertEquals("2", elem.getAttribute("index"));

        elem = driver.findElement(By.id("o4"));
        assertEquals("3", elem.getAttribute("index"));
    }

    @Test
    public void indexP() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <p id='d1'>1</p>\n"
                        + "  <p id='d2' index='two'>2</p>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("d1"));
        assertNull(elem.getAttribute("index"));

        elem = driver.findElement(By.id("d2"));
        assertEquals("two", elem.getAttribute("index"));
    }

    @Test
    public void valueDiv() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='d1'>1</div>\n"
                        + "  <div id='d2' value='two'>2</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("d1"));
        assertNull(elem.getAttribute("value"));

        elem = driver.findElement(By.id("d2"));
        assertEquals("two", elem.getAttribute("value"));
    }

    @Test
    public void valueP() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <p id='d1'>1</p>\n"
                        + "  <p id='d2' value='two'>2</p>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("d1"));
        assertNull(elem.getAttribute("value"));

        elem = driver.findElement(By.id("d2"));
        assertEquals("two", elem.getAttribute("value"));
    }

    @Test
    public void checkbox() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='checkbox' id='chkBx' name='chbox' value='dis' checked='checked' />checked"
                        + "    <input type='checkbox' id='chkBx2' name='chbox2' value='dis'/> not checked"
                        + "    <input type='checkbox' id='chkBx3' name='chbox3' value='dis' checked='false' />\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("chkBx"));
        assertEquals("true", elem.getAttribute("checked"));

        elem = driver.findElement(By.id("chkBx2"));
        assertNull(elem.getAttribute("checked"));

        elem = driver.findElement(By.id("chkBx3"));
        assertEquals("true", elem.getAttribute("checked"));
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
        assertNull(elem.getAttribute("checked"));
        elem.click();
        assertEquals("true", elem.getAttribute("checked"));
    }

    @Test
    public void radio() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='radio' id='radioBx' name='radio' value='dis' checked='checked' />checked"
                        + "    <input type='radio' id='radioBx2' name='radio2' value='dis'/> not checked"
                        + "    <input type='radio' id='radioBx3' name='radio3' value='dis' checked='false' />\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("radioBx"));
        assertEquals("true", elem.getAttribute("checked"));

        elem = driver.findElement(By.id("radioBx2"));
        assertNull(elem.getAttribute("checked"));

        elem = driver.findElement(By.id("radioBx3"));
        assertEquals("true", elem.getAttribute("checked"));
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
        assertNull(elem.getAttribute("checked"));
        elem.click();
        assertEquals("true", elem.getAttribute("checked"));
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
        assertEquals("world", elem.getAttribute("value"));

        elem = driver.findElement(By.id("textBx2"));
        assertEquals("", elem.getAttribute("value"));
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
        assertEquals("world", elem.getAttribute("value"));
        elem.sendKeys("hello");
        assertEquals("worldhello", elem.getAttribute("value"));
    }

    @Test
    public void inputTextAreaValueTyped() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <textarea id='textBx' name='text' >world</textarea>\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement elem = driver.findElement(By.id("textBx"));
        assertEquals("world", elem.getAttribute("value"));
        elem.sendKeys("hello");
        assertEquals("worldhello", elem.getAttribute("value"));
    }

    @Test
    public void inputFileValue() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <fieldset>\n"
                        + "    <input type='file' id='myFile1' name='myFile' accept='image/png'/>\n"
                        + "    <input type='file' id='myFile2' name='myFile' accept='image/png' value='secret.txt' />\n"
                        + "  </fieldset>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("myFile1"));
        assertEquals("", elem.getAttribute("value"));

        elem = driver.findElement(By.id("myFile2"));
        assertEquals("", elem.getAttribute("value"));
    }

    @Test
    public void anchor() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='a1'>empty</a>"
                        + "  <a id='a2' href='test'>empty</a>"
                        + "  <a id='a3' href='https://www.htmlunit.org/href'>empty</a>"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("a1"));
        assertNull(elem.getAttribute("href"));

        elem = driver.findElement(By.id("a2"));
        assertEquals(URL_FIRST.toExternalForm() + "test", elem.getAttribute("href"));

        elem = driver.findElement(By.id("a3"));
        assertEquals("https://www.htmlunit.org/href", elem.getAttribute("href"));
    }

    @Test
    public void src() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <input id='img1' type='image'>"
                        + "  <input id='img2' type='image' src='test'>"
                        + "  <input id='img3' type='image' src='https://www.htmlunit.org/src'>"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement elem = driver.findElement(By.id("img1"));
        assertEquals("", elem.getAttribute("src"));

        elem = driver.findElement(By.id("img2"));
        assertEquals(URL_FIRST.toExternalForm() + "test", elem.getAttribute("src"));

        elem = driver.findElement(By.id("img3"));
        assertEquals("https://www.htmlunit.org/src", elem.getAttribute("src"));
    }
}
