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

import java.time.Duration;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Test for {@link WebElement#getDomProperty(String)}.
 * Tests taken from selenium ElementDomPropertyTest.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementDomProperty2Test extends WebDriverTestCase {

    @Test
    public void testShouldReturnNullWhenGettingTheValueOfAPropertyThatDoesNotExist() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement head = driver.findElement(By.xpath("/html"));
        assertNull(head.getDomProperty("cheese"));
    }

    @Test
    public void testShouldReturnAnAbsoluteUrlWhenGettingSrcAttributeOfAValidImgTag() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement img = driver.findElement(By.id("validImgTag"));
        assertEquals(URL_FIRST.toExternalForm() + "icon.gif", img.getDomProperty("src"));
    }

    @Test
    public void testShouldReturnAnAbsoluteUrlWhenGettingHrefAttributeOfAValidAnchorTag() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement img = driver.findElement(By.id("validAnchorTag"));
        assertEquals(URL_FIRST.toExternalForm() + "icon.gif", img.getDomProperty("href"));
    }

    @Test
    public void testShouldReturnTheValueOfTheIndexAttributeEvenIfItIsMissing() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement multiSelect = driver.findElement(By.id("multi"));
        final List<WebElement> options = multiSelect.findElements(By.tagName("option"));
        assertEquals("1", options.get(1).getDomProperty("index"));
    }

    @Test
    public void testShouldReturnTheValueOfCheckedForACheckboxOnlyIfItIsChecked() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement checkbox = driver.findElement(By.xpath("//input[@id='checky']"));
        assertEquals("false", checkbox.getDomProperty("checked"));

        checkbox.click();
        assertEquals("true", checkbox.getDomProperty("checked"));
    }

    @Test
    public void testShouldReturnTheValueOfSelectedForOptionsOnlyIfTheyAreSelected() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement selectBox = driver.findElement(By.xpath("//select[@name='selectomatic']"));
        final List<WebElement> options = selectBox.findElements(By.tagName("option"));
        final WebElement one = options.get(0);
        final WebElement two = options.get(1);

        assertTrue(one.isSelected());
        assertFalse(two.isSelected());
        assertEquals("true", one.getDomProperty("selected"));
        assertEquals("false", two.getDomProperty("selected"));
        assertEquals("0", selectBox.getDomProperty("selectedIndex"));
    }

    @Test
    public void testShouldGetClassPropertiesOfAnElement() throws Exception {
        final String html = getFileContent("xhtmlTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement heading = driver.findElement(By.cssSelector(".nameA"));
        assertNull(heading.getDomProperty("class"));
        assertEquals("nameA nameBnoise   nameC", heading.getDomProperty("className"));
        assertEquals("[nameA, nameBnoise, nameC]", heading.getDomProperty("classList"));
    }

    @Test
    public void testShouldReturnTheContentsOfATextAreaAsItsValue() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement withText = driver.findElement(By.id("withText"));
        assertEquals("Example text", withText.getDomProperty("value"));
    }

    @Test
    public void testShouldReturnInnerHtml() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement wrapping = driver.findElement(By.id("wrappingtext"));
        assertTrue(wrapping.getDomProperty("innerHTML").contains("<tbody>"));
    }

    @Test
    public void testShouldReturnHiddenTextForTextContentProperty() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("hiddenline"));
        assertEquals("A hidden line of text", element.getDomProperty("textContent"));
    }

    @Test
    public void testShouldGetNumericProperty() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("withText"));
        assertEquals("5", element.getDomProperty("rows"));
    }

    @Test
    public void testCanReturnATextApproximationOfTheStyleProperty() throws Exception {
        final String html = getFileContent("javascriptPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("red-item"));
        assertTrue(element.getDomProperty("style").toLowerCase().contains("background-color"));
    }

    @Test
    public void testPropertyNamesAreCaseSensitive() throws Exception {
        final String html = getFileContent("tables.html");
        final WebDriver driver = loadPage2(html);

        final WebElement th1 = driver.findElement(By.id("th1"));
        assertNull(th1.getDomProperty("colspan"));
        assertNull(th1.getDomProperty("COLSPAN"));
        assertEquals("3", th1.getDomProperty("colSpan"));
    }

    @Test
    public void testCanRetrieveTheCurrentValueOfATextFormField_textInput() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("working"));
        assertEquals("", element.getDomProperty("value"));

        element.sendKeys("hello world");
        new WebDriverWait(driver, Duration.ofSeconds(5))
            .until(ExpectedConditions.domPropertyToBe(element, "value", "hello world"));
    }

    @Test
    public void testCanRetrieveTheCurrentValueOfATextFormField_emailInput() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("email"));
        assertEquals("", element.getDomProperty("value"));

        element.sendKeys("hello@example.com");
        new WebDriverWait(driver, Duration.ofSeconds(5))
            .until(ExpectedConditions.domPropertyToBe(element, "value", "hello@example.com"));
    }

    @Test
    public void testCanRetrieveTheCurrentValueOfATextFormField_textArea() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("emptyTextArea"));
        assertEquals("", element.getDomProperty("value"));

        element.sendKeys("hello world");
        new WebDriverWait(driver, Duration.ofSeconds(5))
            .until(ExpectedConditions.domPropertyToBe(element, "value", "hello world"));
    }

    @Test
    public void testMultiplePropertyShouldBeTrueWhenSelectHasMultipleWithValueAsBlank() throws Exception {
        final String html = getFileContent("selectPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("selectWithEmptyStringMultiple"));
        assertEquals("true", element.getDomProperty("multiple"));
    }

    @Test
    public void testMultiplePropertyShouldBeTrueWhenSelectHasMultipleWithoutAValue() throws Exception {
        final String html = getFileContent("selectPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("selectWithMultipleWithoutValue"));
        assertEquals("true", element.getDomProperty("multiple"));
    }

    @Test
    public void testMultiplePropertyShouldBeTrueWhenSelectHasMultipleWithValueAsSomethingElse() throws Exception {
        final String html = getFileContent("selectPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("selectWithRandomMultipleValue"));
        assertEquals("true", element.getDomProperty("multiple"));
    }

    @Test
    public void testGetValueOfUserDefinedProperty() throws Exception {
        final String html = getFileContent("userDefinedProperty.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("d"));
        assertEquals("sampleValue", element.getDomProperty("dynamicProperty"));
    }
}
