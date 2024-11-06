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

import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.BuggyWebDriver;

/**
 * Test for {@link WebElement#getDomAttribute(String)}.
 * Tests taken from selenium ElementDomAttributeTest.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementDomAttribute2Test extends WebDriverTestCase {

    @Test
    public void testShouldReturnNullWhenGettingTheValueOfAnAttributeThatIsNotListed() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement head = driver.findElement(By.xpath("/html"));
        final String attribute = head.getDomAttribute("cheese");
        assertNull(attribute);
    }

    @Test
    public void testShouldReturnNullWhenGettingSrcAttributeOfInvalidImgTag() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement img = driver.findElement(By.id("invalidImgTag"));
        final String attribute = img.getDomAttribute("src");
        assertNull(attribute);
    }

    @Test
    public void testShouldReturnTheActualValueWhenGettingSrcAttributeOfAValidImgTag() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement img = driver.findElement(By.id("validImgTag"));
        final String attribute = img.getDomAttribute("src");
        assertEquals("icon.gif", attribute);
    }

    @Test
    public void testShouldReturnTheActualValueWhenGettingHrefAttributeOfAValidAnchorTag() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement img = driver.findElement(By.id("validAnchorTag"));
        final String attribute = img.getDomAttribute("href");
        assertEquals("icon.gif", attribute);
    }

    @Test
    public void testShouldReturnEmptyAttributeValuesWhenPresentAndTheValueIsActuallyEmpty() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement body = driver.findElement(By.xpath("//body"));
        assertEquals("", body.getDomAttribute("style"));
    }

    @Test
    public void testShouldReturnTheValueOfTheDisabledAttributeAsNullIfNotSet() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement inputElement = driver.findElement(By.xpath("//input[@id='working']"));
        assertNull(inputElement.getDomAttribute("disabled"));
        assertTrue(inputElement.isEnabled());

        final WebElement pElement = driver.findElement(By.id("peas"));
        assertNull(pElement.getDomAttribute("disabled"));
        assertTrue(pElement.isEnabled());
    }

    @Test
    public void testShouldNotReturnTheValueOfTheIndexAttributeIfItIsMissing() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement multiSelect = driver.findElement(By.id("multi"));
        final List<WebElement> options = multiSelect.findElements(By.tagName("option"));
        assertNull(options.get(1).getDomAttribute("index"));
    }

    @Test
    public void testShouldIndicateTheElementsThatAreDisabledAreNotEnabled() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        WebElement inputElement = driver.findElement(By.xpath("//input[@id='notWorking']"));
        assertFalse(inputElement.isEnabled());

        inputElement = driver.findElement(By.xpath("//input[@id='working']"));
        assertTrue(inputElement.isEnabled());
    }

    @Test
    public void testElementsShouldBeDisabledIfTheyAreDisabledUsingRandomDisabledStrings() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement disabledTextElement1 = driver.findElement(By.id("disabledTextElement1"));
        assertFalse(disabledTextElement1.isEnabled());

        final WebElement disabledTextElement2 = driver.findElement(By.id("disabledTextElement2"));
        assertFalse(disabledTextElement2.isEnabled());

        final WebElement disabledSubmitElement = driver.findElement(By.id("disabledSubmitElement"));
        assertFalse(disabledSubmitElement.isEnabled());
    }

    @Test
    public void testShouldThrowExceptionIfSendingKeysToElementDisabledUsingRandomDisabledStrings() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement disabledTextElement1 = driver.findElement(By.id("disabledTextElement1"));
        assertThrows(InvalidElementStateException.class, () -> disabledTextElement1.sendKeys("foo"));
        assertEquals("", disabledTextElement1.getText());

        final WebElement disabledTextElement2 = driver.findElement(By.id("disabledTextElement2"));
        assertThrows(InvalidElementStateException.class, () -> disabledTextElement2.sendKeys("bar"));
        assertEquals("", disabledTextElement2.getText());
    }

    @Test
    public void testShouldIndicateWhenATextAreaIsDisabled() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement textArea = driver.findElement(By.xpath("//textarea[@id='notWorkingArea']"));
        assertFalse(textArea.isEnabled());
    }

    @Test
    public void testShouldIndicateWhenASelectIsDisabled() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement enabled = driver.findElement(By.name("selectomatic"));
        final WebElement disabled = driver.findElement(By.name("no-select"));

        assertTrue(enabled.isEnabled());
        assertFalse(disabled.isEnabled());
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
        assertEquals("true", one.getDomAttribute("selected"));
        assertNull(two.getDomAttribute("selected"));
    }

    @Test
    public void testShouldReturnValueOfClassAttributeOfAnElement() throws Exception {
        final String html = getFileContent("xhtmlTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement heading = driver.findElement(By.xpath("//h1"));
        final String className = heading.getDomAttribute("class");

        assertEquals("header", className);
    }

    @Test
    public void testShouldNotReturnTheContentsOfATextAreaAsItsValue() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final String value = driver.findElement(By.id("withText")).getDomAttribute("value");
        assertNull(value);
    }

    @Test
    public void testShouldNotReturnInnerHtmlProperty() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final String innerHtml = driver.findElement(By.id("wrappingtext")).getDomAttribute("innerHTML");
        assertNull(innerHtml);
    }

    @Test
    public void testShouldTreatReadonlyAsAValue() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.name("readonly"));
        final String readonly = element.getDomAttribute("readonly");

        assertNotNull(readonly);

        final WebElement textInput = driver.findElement(By.name("x"));
        final String notReadonly = textInput.getDomAttribute("readonly");

        assertNotSame(notReadonly, readonly);
    }

    @Test
    public void testShouldNotReturnTextContentProperty() throws Exception {
        final String html = getFileContent("simpleTest.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("hiddenline"));
        assertNull(element.getDomAttribute("textContent"));
    }

    @Test
    public void testShouldGetNumericAttribute() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("withText"));
        assertEquals("5", element.getDomAttribute("rows"));
    }

    @Test
    public void testCanReturnATextApproximationOfTheStyleAttribute() throws Exception {
        final String html = getFileContent("javascriptPage.html");
        final WebDriver driver = loadPage2(html);

        final String style = driver.findElement(By.id("red-item")).getDomAttribute("style");
        assertTrue(style.toLowerCase().contains("background-color"));
    }

    @Test
    public void testShouldCorrectlyReportValueOfColspan() throws Exception {
        final String html = getFileContent("tables.html");
        final WebDriver driver = loadPage2(html);

        final WebElement th1 = driver.findElement(By.id("th1"));
        final WebElement td2 = driver.findElement(By.id("td2"));

        assertEquals("th1", th1.getDomAttribute("id"));
        assertEquals("3", th1.getDomAttribute("colspan"));

        assertEquals("td2", td2.getDomAttribute("id"));
        assertEquals("2", td2.getDomAttribute("colspan"));
    }

    @Test
    @Alerts("javascript:displayMessage('mouse click');")
    public void testShouldReturnValueOfOnClickAttribute() throws Exception {
        final String html = getFileContent("javascriptPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement mouseclickDiv = driver.findElement(By.id("mouseclick"));

        final String onClickValue = mouseclickDiv.getDomAttribute("onclick");
        assertEquals(getExpectedAlerts()[0], onClickValue);

        final WebElement mousedownDiv = driver.findElement(By.id("mousedown"));
        assertNull(mousedownDiv.getDomAttribute("onclick"));
    }

//    @Test
//    void testgetDomAttributeDoesNotReturnAnObjectForSvgProperties() {
//      driver.get(pages.svgPage);
//      WebElement svgElement = driver.findElement(By.id("rotate"));
//      assertThat(svgElement.getDomAttribute("transform")).isEqualTo("rotate(30)");
//    }

    @Test
    public void testCanRetrieveTheCurrentValueOfATextFormFieldWithPresetText() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("inputWithText"));
        assertEquals("Example text", element.getDomAttribute("value"));

        element.sendKeys("hello@example.com");
        assertEquals("Example text", element.getDomAttribute("value"));
    }

    @Test
    public void testShouldNotReturnTextOfATextArea() throws Exception {
        final String html = getFileContent("formPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("withText"));
        assertNull(element.getDomAttribute("value"));
    }

    @Test
    public void testShouldReturnNullForNonPresentBooleanAttributes() throws Exception {
        final String html = getFileContent("booleanAttributes.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element1 = driver.findElement(By.id("working"));
        assertNull(element1.getDomAttribute("required"));

        final WebElement element2 = driver.findElement(By.id("wallace"));
        assertNull(element2.getDomAttribute("nowrap"));
    }

    @Test
//    @NotYetImplemented(value = CHROME, reason = "It returns a property")
//    @NotYetImplemented(EDGE)
//    @NotYetImplemented(FIREFOX)
    @Alerts({"", "required", "", "false", ""})
    @BuggyWebDriver(
            CHROME = {"true", "true", "true", "true", "true"},
            EDGE = {"true", "true", "true", "true", "true"},
            FF = {"true", "true", "true", "true", ""},
            FF_ESR = {"true", "true", "true", "true", ""})
    public void testShouldReturnEmptyStringForPresentBooleanAttributes() throws Exception {
        final String html = getFileContent("booleanAttributes.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element1 = driver.findElement(By.id("emailRequired"));
        assertEquals(getExpectedAlerts()[0], element1.getDomAttribute("required"));

        final WebElement element2 = driver.findElement(By.id("emptyTextAreaRequired"));
        assertEquals(getExpectedAlerts()[1], element2.getDomAttribute("required"));

        final WebElement element3 = driver.findElement(By.id("inputRequired"));
        assertEquals(getExpectedAlerts()[2], element3.getDomAttribute("required"));

        final WebElement element4 = driver.findElement(By.id("textAreaRequired"));
        assertEquals(getExpectedAlerts()[3], element4.getDomAttribute("required"));

        final WebElement element5 = driver.findElement(By.id("unwrappable"));
        assertEquals(getExpectedAlerts()[4], element5.getDomAttribute("nowrap"));
    }

    @Test
    public void testMultipleAttributeShouldBeNullWhenNotSet() throws Exception {
        final String html = getFileContent("selectPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("selectWithoutMultiple"));
        assertNull(element.getDomAttribute("multiple"));
    }

    @Test
    public void testMultipleAttributeShouldBeTrueWhenSet() throws Exception {
        final String html = getFileContent("selectPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("selectWithMultipleEqualsMultiple"));
        assertEquals("true", element.getDomAttribute("multiple"));
    }

    @Test
    public void testMultipleAttributeShouldBeTrueWhenSelectHasMultipleWithValueAsBlank() throws Exception {
        final String html = getFileContent("selectPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("selectWithEmptyStringMultiple"));
        assertEquals("true", element.getDomAttribute("multiple"));
    }

    @Test
    public void testMultipleAttributeShouldBeTrueWhenSelectHasMultipleWithoutAValue() throws Exception {
        final String html = getFileContent("selectPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("selectWithMultipleWithoutValue"));
        assertEquals("true", element.getDomAttribute("multiple"));
    }

    @Test
    public void testMultipleAttributeShouldBeTrueWhenSelectHasMultipleWithValueAsSomethingElse() throws Exception {
        final String html = getFileContent("selectPage.html");
        final WebDriver driver = loadPage2(html);

        final WebElement element = driver.findElement(By.id("selectWithRandomMultipleValue"));
        assertEquals("true", element.getDomAttribute("multiple"));
    }

//    @Test
//    void shouldTreatContenteditableAsEnumeratedButNotBoolean() {
//      checkEnumeratedAttribute("contenteditable", "true", "false", "yes", "no", "", "blabla");
//    }
//
//    @Test
//    @NotYetImplemented(IE)
//    @NotYetImplemented(SAFARI)
//    public void shouldTreatDraggableAsEnumeratedButNotBoolean() {
//      checkEnumeratedAttribute("draggable", "true", "false", "yes", "no", "", "blabla");
//    }
//
//    private void checkEnumeratedAttribute(String name, String... values) {
//      asList(values)
//          .forEach(
//              value -> {
//                driver.get(
//                    appServer.create(
//                        new Page()
//                            .withBody(String.format("<div id=\"attr\" %s=\"%s\">", name, value))));
//                assertThat(driver.findElement(By.id("attr")).getDomAttribute(name)).isEqualTo(value);
//              });
//
//      driver.get(appServer.create(new Page().withBody(String.format("<div id=\"attr\" %s>", name))));
//      assertThat(driver.findElement(By.id("attr")).getDomAttribute(name)).isEmpty();
//
//      driver.get(appServer.create(new Page().withBody("<div id=\"attr\">")));
//      assertThat(driver.findElement(By.id("attr")).getDomAttribute(name)).isNull();
//    }
}
