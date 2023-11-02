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

import org.htmlunit.html.HtmlInput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for the HtmlUnitWebElement.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
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

    @Test
    public void attributeExists() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv' class='test-class'></div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertNotNull(divElement.getAttribute("class"));
    }
    @Test
    public void innerTextExists() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv'>Inner Text</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertEquals("Inner Text", divElement.getAttribute("innerText"));
    }

    @Test
    public void innerTextDoesNotExist() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv'></div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        String res = divElement.getAttribute("innerText");
        assertNull(res.isEmpty()? null : res.isEmpty());
    }

    @Test
    public void valueAttribute() throws Exception {
        final String html = "<html><head></head><body><input id='myInput' type='text' value='Initial Value'></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement inputElement = webDriver.findElement(By.id("myInput"));
        assertEquals("Initial Value", inputElement.getAttribute("value"));
    }

    @Test
    public void getCssValue() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv' style='color: red; font-size: 16px;'></div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertEquals("rgba(255, 0, 0, 1)", divElement.getCssValue("color"));
        assertEquals("16px", divElement.getCssValue("font-size"));
    }

    @Test
    public void testGetText() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv'>This is some text</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertEquals("This is some text", divElement.getText());
    }

    @Test
    public void testIsDisplayedWhenVisible() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv' style='display: block;'>Visible Div</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertTrue(divElement.isDisplayed());
    }

    @Test
    public void testGetAttributeWithExistingAttribute() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv' class='my-class'>Test Div</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertEquals("my-class", divElement.getAttribute("class"));
    }

    @Test
    public void testGetAttributeWithNonExistingAttribute() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv' class='my-class'>Test Div</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertNull(divElement.getAttribute("nonexistent"));
    }

    @Test
    public void testGetCssValue() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv' style='color: red;'>Test Div</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertEquals("rgba(255, 0, 0, 1)", divElement.getCssValue("color"));
    }

    @Test
    public void testGetLocation() throws Exception {
        final String html = "<html><head></head><body><a id='myLink' href='https://example.com'>Example</a></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement linkElement = webDriver.findElement(By.id("myLink"));
        assertEquals("https://example.com", linkElement.getAttribute("href"));
    }

    @Test
    public void testGetLocationWhenHrefMissing() throws Exception {
        final String html = "<html><head></head><body><a id='myLink'>Missing Href</a></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement linkElement = webDriver.findElement(By.id("myLink"));
        assertNull(linkElement.getAttribute("href"));
    }


    @Test
    public void testIsDisplayedWhenInvisible() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv' style='display: none;'>Invisible Div</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertFalse(divElement.isDisplayed());
    }

    @Test
    public void testGetTagNameForDiv() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv'>Test Div</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertEquals("div", divElement.getTagName());
    }

    @Test
    public void testGetTagNameForInput() throws Exception {
        final String html = "<html><head></head><body><input id='myInput' type='text'></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement inputElement = webDriver.findElement(By.id("myInput"));
        assertEquals("input", inputElement.getTagName());
    }

    @Test
    public void multipleElements() throws Exception {
        final String html = "<html><head></head><body><div class='test-class'></div><div class='test-class'></div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final List<WebElement> elements = webDriver.findElements(By.className("test-class"));
        assertEquals(2, elements.size());
    }

    @Test
    public void isSelected() throws Exception {
        final String html = "<html><head></head><body><input id='myCheckbox' type='checkbox' checked></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement checkboxElement = webDriver.findElement(By.id("myCheckbox"));
        assertTrue(checkboxElement.isSelected());
    }


    @Test
    public void attributeDoesNotExist() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv'></div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertNull(divElement.getAttribute("class"));
    }

    @Test
    public void testTextContent() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv'>This is some text.</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertEquals("This is some text.", divElement.getText());
    }

    @Test
    public void testTextContentWithEmptyElement() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv'></div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertEquals("", divElement.getText());
    }

    @Test
    public void testGetTagName() throws Exception {
        final String html = "<html><head></head><body><a id='myLink' href='https://example.com'>Example</a></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement linkElement = webDriver.findElement(By.id("myLink"));
        assertEquals("a", linkElement.getTagName());
    }

    @Test
    public void testClearInputField() throws Exception {
        final String html = "<html><head></head><body><input id='myInput' type='text' value='Initial Value'></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement inputElement = webDriver.findElement(By.id("myInput"));
        inputElement.clear();
        assertEquals("", inputElement.getAttribute("value"));
    }

    @Test
    public void testIsDisplayed() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv' style='display: block;'>Visible Div</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertTrue(divElement.isDisplayed());
    }

    @Test
    public void testIsNotDisplayed() throws Exception {
        final String html = "<html><head></head><body><div id='myDiv' style='display: none;'>Hidden Div</div></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement divElement = webDriver.findElement(By.id("myDiv"));
        assertFalse(divElement.isDisplayed());
    }

    @Test
    public void testIsEnabled() throws Exception {
        final String html = "<html><head></head><body><button id='myButton' disabled>Disabled Button</button></body></html>";
        final WebDriver webDriver = loadPage2(html);
        final WebElement buttonElement = webDriver.findElement(By.id("myButton"));
        assertFalse(buttonElement.isEnabled());
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
        final String html = "<html>\n"
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
