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

package org.openqa.selenium.htmlunit.by;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.locators.RelativeLocator.with;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.HtmlUnitNYI;

/**
 * Tests for RelativeLocator.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class RelativeLocatorTest extends WebDriverTestCase {

    @Test
    @Alerts({"2", "mid", "above"})
    @HtmlUnitNYI(CHROME = {"1", "above", "above"},
            EDGE = {"1", "above", "above"},
            FF = {"1", "above", "above"},
            FF_ESR = {"1", "above", "above"})
    public void shouldBeAbleToFindElementsAboveAnotherWithTagName() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final WebElement lowest = driver.findElement(By.id("below"));

        final List<WebElement> seen  = driver.findElements(with(tagName("p")).above(lowest));
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        assertEquals(Integer.parseInt(getExpectedAlerts()[0]), ids.size());
        assertTrue(ids.contains(getExpectedAlerts()[1]));
        assertTrue(ids.contains(getExpectedAlerts()[2]));
    }

    @Test
    @Alerts({"2", "fourth", "first"})
    @HtmlUnitNYI(CHROME = {"1", "first", "first"},
            EDGE = {"1", "first", "first"},
            FF = {"1", "first", "first"},
            FF_ESR = {"1", "first", "first"})
    public void shouldBeAbleToFindElementsAboveAnotherWithXpath() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final WebElement lowest = driver.findElement(By.id("seventh"));

        final List<WebElement> seen = driver.findElements(with(xpath("//td[1]")).above(lowest));
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        assertEquals(Integer.parseInt(getExpectedAlerts()[0]), ids.size());
        assertTrue(ids.contains(getExpectedAlerts()[1]));
        assertTrue(ids.contains(getExpectedAlerts()[2]));
    }

    @Test
    @Alerts({"2", "mid", "above"})
    @HtmlUnitNYI(CHROME = {"1", "above", "above"},
            EDGE = {"1", "above", "above"},
            FF = {"1", "above", "above"},
            FF_ESR = {"1", "above", "above"})
    public void shouldBeAbleToFindElementsAboveAnotherWithCssSelector() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final WebElement lowest = driver.findElement(By.id("below"));

        final List<WebElement> seen = driver.findElements(with(cssSelector("p")).above(lowest));
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        assertEquals(Integer.parseInt(getExpectedAlerts()[0]), ids.size());
        assertTrue(ids.contains(getExpectedAlerts()[1]));
        assertTrue(ids.contains(getExpectedAlerts()[2]));
    }

    @Test
    @Alerts({"1", "third"})
    public void shouldBeAbleToCombineFilters() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final List<WebElement> seen = driver.findElements(
                        with(tagName("td")).above(By.id("center")).toRightOf(By.id("second")));
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        assertEquals(Integer.parseInt(getExpectedAlerts()[0]), ids.size());
        assertTrue(ids.contains(getExpectedAlerts()[1]));
    }

    @Test
    @Alerts({"1", "fourth"})
    public void shouldBeAbleToCombineFiltersWithXpath() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final List<WebElement> seen = driver.findElements(
                        with(xpath("//td[1]")).below(By.id("second")).above(By.id("seventh")));
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        assertEquals(Integer.parseInt(getExpectedAlerts()[0]), ids.size());
        assertTrue(ids.contains(getExpectedAlerts()[1]));
    }

    @Test
    @Alerts({"1", "third"})
    public void shouldBeAbleToCombineFiltersWithCssSelector() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final List<WebElement> seen = driver.findElements(
                        with(cssSelector("td")).above(By.id("center")).toRightOf(By.id("second")));
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        assertEquals(Integer.parseInt(getExpectedAlerts()[0]), ids.size());
        assertTrue(ids.contains(getExpectedAlerts()[1]));
    }

    @Test
    public void exerciseNearLocatorWithTagName() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final List<WebElement> seen = driver.findElements(with(tagName("td")).near(By.id("center")));

        // Elements are sorted by proximity and then DOM insertion order.
        // Proximity is determined using distance from center points, so
        // we expect the order to be:
        // 1. Directly above (short vertical distance, first in DOM)
        // 2. Directly below (short vertical distance, later in DOM)
        // 3. Directly left (slight longer distance horizontally, first in DOM)
        // 4. Directly right (slight longer distance horizontally, later in DOM)
        // 5-8. Diagonally close (pythagoras sorting, with top row first
        //    because of DOM insertion order)
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        assertEquals(8, ids.size());
        assertTrue(ids.contains("second"));
        assertTrue(ids.contains("eighth"));
        assertTrue(ids.contains("fourth"));
        assertTrue(ids.contains("sixth"));
        assertTrue(ids.contains("first"));
        assertTrue(ids.contains("third"));
        assertTrue(ids.contains("seventh"));
        assertTrue(ids.contains("ninth"));
    }

    @Test
    public void exerciseNearLocatorWithXpath() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final List<WebElement> seen = driver.findElements(with(xpath("//td")).near(By.id("center")));

        // Elements are sorted by proximity and then DOM insertion order.
        // Proximity is determined using distance from center points, so
        // we expect the order to be:
        // 1. Directly above (short vertical distance, first in DOM)
        // 2. Directly below (short vertical distance, later in DOM)
        // 3. Directly left (slight longer distance horizontally, first in DOM)
        // 4. Directly right (slight longer distance horizontally, later in DOM)
        // 5-8. Diagonally close (pythagoras sorting, with top row first
        //    because of DOM insertion order)
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        assertEquals(8, ids.size());
        assertTrue(ids.contains("second"));
        assertTrue(ids.contains("eighth"));
        assertTrue(ids.contains("fourth"));
        assertTrue(ids.contains("sixth"));
        assertTrue(ids.contains("first"));
        assertTrue(ids.contains("third"));
        assertTrue(ids.contains("seventh"));
        assertTrue(ids.contains("ninth"));
    }

    @Test
    public void exerciseNearLocatorWithCssSelector() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final List<WebElement> seen = driver.findElements(with(cssSelector("td")).near(By.id("center")));

        // Elements are sorted by proximity and then DOM insertion order.
        // Proximity is determined using distance from center points, so
        // we expect the order to be:
        // 1. Directly above (short vertical distance, first in DOM)
        // 2. Directly below (short vertical distance, later in DOM)
        // 3. Directly left (slight longer distance horizontally, first in DOM)
        // 4. Directly right (slight longer distance horizontally, later in DOM)
        // 5-8. Diagonally close (pythagoras sorting, with top row first
        //    because of DOM insertion order)
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        assertEquals(8, ids.size());
        assertTrue(ids.contains("second"));
        assertTrue(ids.contains("eighth"));
        assertTrue(ids.contains("fourth"));
        assertTrue(ids.contains("sixth"));
        assertTrue(ids.contains("first"));
        assertTrue(ids.contains("third"));
        assertTrue(ids.contains("seventh"));
        assertTrue(ids.contains("ninth"));
    }

    @Test
    public void ensureNoRepeatedElements() throws Exception {
        final String html = "<html>\n"
                + "<head>\n"
                + "  <title>Repeated Elements</title>"
                + "  <style>\n"
                + "    .c {\n"
                + "      position: absolute;\n"
                + "      border: 1px solid black;\n"
                + "      height: 50px;\n"
                + "      width: 50px;\n"
                + "    }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <span style=\"position: relative;\">\n"
                + "    <div id= \"a\" class=\"c\" style=\"left:25;top:0;\">El-A</div>\n"
                + "    <div id= \"b\" class=\"c\" style=\"left:78;top:30;\">El-B</div>\n"
                + "    <div id= \"c\" class=\"c\" style=\"left:131;top:60;\">El-C</div>\n"
                + "    <div id= \"d\" class=\"c\" style=\"left:0;top:53;\">El-D</div>\n"
                + "    <div id= \"e\" class=\"c\" style=\"left:53;top:83;\">El-E</div>\n"
                + "    <div id= \"f\" class=\"c\" style=\"left:106;top:113;\">El-F</div>\n"
                + "  </span>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement base = driver.findElement(By.id("e"));
        final List<WebElement> cells = driver.findElements(with(tagName("div")).above(base));

        final WebElement a = driver.findElement(By.id("a"));
        final WebElement b = driver.findElement(By.id("b"));

        assertEquals(2, cells.size());
        assertEquals(b, cells.get(0));
        assertEquals(a, cells.get(1));
    }

    @Test
    public void nearLocatorShouldFindNearElements() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final WebElement rect1 = driver.findElement(By.id("rect1"));
        final WebElement rect2 = driver.findElement(with(By.id("rect2")).near(rect1));

        assertEquals("rect2", rect2.getAttribute("id"));
    }

    @Test
    public void nearLocatorShouldNotFindFarElements() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final WebElement rect3 = driver.findElement(By.id("rect3"));

        try {
            driver.findElement(with(By.id("rect4")).near(rect3));
        }
        catch (final NoSuchElementException e) {
            assertTrue(e.getMessage().contains("Cannot locate an element using"));
        }
    }
}
