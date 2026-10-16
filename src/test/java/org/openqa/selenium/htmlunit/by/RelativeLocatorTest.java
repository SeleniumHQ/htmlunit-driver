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

package org.openqa.selenium.htmlunit.by;

import static org.junit.Assert.fail;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.locators.RelativeLocator.with;

import java.util.Arrays;
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
    @HtmlUnitNYI(CHROME = "0",
            EDGE = "0",
            FF = "0",
            FF_ESR = "0")
    public void shouldBeAbleToCombineFilters() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final List<WebElement> seen = driver.findElements(
                        with(tagName("td")).above(By.id("center")).toRightOf(By.id("second")));
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        final int expectedSize = Integer.parseInt(getExpectedAlerts()[0]);
        assertEquals(expectedSize, ids.size());
        if (expectedSize > 0) {
            assertTrue(ids.contains(getExpectedAlerts()[1]));
        }
    }

    @Test
    @Alerts({"1", "fourth"})
    @HtmlUnitNYI(CHROME = "0",
            EDGE = "0",
            FF = "0",
            FF_ESR = "0")
    public void shouldBeAbleToCombineFiltersWithXpath() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final List<WebElement> seen = driver.findElements(
                        with(xpath("//td[1]")).below(By.id("second")).above(By.id("seventh")));
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        final int expectedSize = Integer.parseInt(getExpectedAlerts()[0]);
        assertEquals(expectedSize, ids.size());
        if (expectedSize > 0) {
            assertTrue(ids.contains(getExpectedAlerts()[1]));
        }
    }

    @Test
    @Alerts({"1", "third"})
    @HtmlUnitNYI(CHROME = "0",
            EDGE = "0",
            FF = "0",
            FF_ESR = "0")
    public void shouldBeAbleToCombineFiltersWithCssSelector() throws Exception {
        final String html = getFileContent("relative_locators.html");
        final WebDriver driver = loadPage2(html);

        final List<WebElement> seen = driver.findElements(
                        with(cssSelector("td")).above(By.id("center")).toRightOf(By.id("second")));
        final List<String> ids = seen.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());

        final int expectedSize = Integer.parseInt(getExpectedAlerts()[0]);
        assertEquals(expectedSize, ids.size());
        if (expectedSize > 0) {
            assertTrue(ids.contains(getExpectedAlerts()[1]));
        }
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

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /**
     * 3x3 grid of absolutely positioned 50x50 cells with a gap of 50px.
     * <pre>
     *   tl tc tr
     *   ml mc mr
     *   bl bc br
     * </pre>
     */
    private static String grid(final String extraBody) {
        return "<html>\n"
                + "<head>\n"
                + "  <style>\n"
                + "    body { margin: 0; }\n"
                + "    .c { position: absolute; width: 50px; height: 50px; }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div id='tl' class='c' name='cell' style='left: 0px; top: 0px;'></div>\n"
                + "  <div id='tc' class='c' name='cell' style='left: 100px; top: 0px;'></div>\n"
                + "  <div id='tr' class='c' name='cell' style='left: 200px; top: 0px;'></div>\n"
                + "  <div id='ml' class='c' name='cell' style='left: 0px; top: 100px;'></div>\n"
                + "  <div id='mc' class='c' name='cell' style='left: 100px; top: 100px;'></div>\n"
                + "  <div id='mr' class='c' name='cell' style='left: 200px; top: 100px;'></div>\n"
                + "  <div id='bl' class='c' name='cell' style='left: 0px; top: 200px;'></div>\n"
                + "  <div id='bc' class='c' name='cell' style='left: 100px; top: 200px;'></div>\n"
                + "  <div id='br' class='c' name='cell' style='left: 200px; top: 200px;'></div>\n"
                + extraBody
                + "</body>\n"
                + "</html>\n";
    }

    private static List<String> ids(final List<WebElement> elements) {
        return elements.stream().map(e -> e.getAttribute("id")).collect(Collectors.toList());
    }

    private static List<String> sortedIds(final List<WebElement> elements) {
        return elements.stream().map(e -> e.getAttribute("id")).sorted().collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // single filters
    // ------------------------------------------------------------------

    @Test
    public void above() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement mc = driver.findElement(By.id("mc"));

        assertEquals(Arrays.asList("tc", "tl", "tr"), sortedIds(driver.findElements(with(tagName("div")).above(mc))));
        // the closest element comes first
        assertEquals("tc", driver.findElement(with(tagName("div")).above(mc)).getAttribute("id"));
    }

    @Test
    public void below() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement mc = driver.findElement(By.id("mc"));

        assertEquals(Arrays.asList("bc", "bl", "br"), sortedIds(driver.findElements(with(tagName("div")).below(mc))));
        assertEquals("bc", driver.findElement(with(tagName("div")).below(mc)).getAttribute("id"));
    }

    @Test
    public void toLeftOf() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement mc = driver.findElement(By.id("mc"));

        assertEquals(Arrays.asList("bl", "ml", "tl"),
                sortedIds(driver.findElements(with(tagName("div")).toLeftOf(mc))));
        assertEquals("ml", driver.findElement(with(tagName("div")).toLeftOf(mc)).getAttribute("id"));
    }

    @Test
    public void toRightOf() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement mc = driver.findElement(By.id("mc"));

        assertEquals(Arrays.asList("br", "mr", "tr"),
                sortedIds(driver.findElements(with(tagName("div")).toRightOf(mc))));
        assertEquals("mr", driver.findElement(with(tagName("div")).toRightOf(mc)).getAttribute("id"));
    }

    @Test
    public void referenceGivenAsLocator() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement mc = driver.findElement(By.id("mc"));

        assertEquals(sortedIds(driver.findElements(with(tagName("div")).above(mc))),
                sortedIds(driver.findElements(with(tagName("div")).above(By.id("mc")))));
        assertEquals(sortedIds(driver.findElements(with(tagName("div")).toRightOf(mc))),
                sortedIds(driver.findElements(with(tagName("div")).toRightOf(By.id("mc")))));
    }

    @Test
    public void referenceElementIsNeverPartOfTheResult() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement mc = driver.findElement(By.id("mc"));

        assertFalse(ids(driver.findElements(with(tagName("div")).above(mc))).contains("mc"));
        assertFalse(ids(driver.findElements(with(tagName("div")).below(mc))).contains("mc"));
        assertFalse(ids(driver.findElements(with(tagName("div")).toLeftOf(mc))).contains("mc"));
        assertFalse(ids(driver.findElements(with(tagName("div")).toRightOf(mc))).contains("mc"));
        assertFalse(ids(driver.findElements(with(tagName("div")).near(mc, 1000))).contains("mc"));
    }

    // ------------------------------------------------------------------
    // combined filters
    // ------------------------------------------------------------------

    @Test
    public void combinedFilters() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement mc = driver.findElement(By.id("mc"));
        final WebElement tc = driver.findElement(By.id("tc"));
        final WebElement bc = driver.findElement(By.id("bc"));

        assertEquals(Arrays.asList("tl"),
                sortedIds(driver.findElements(with(tagName("div")).above(mc).toLeftOf(mc))));
        assertEquals(Arrays.asList("br"),
                sortedIds(driver.findElements(with(tagName("div")).below(mc).toRightOf(mc))));

        // the middle row: below the top row and above the bottom row
        assertEquals(Arrays.asList("mc", "ml", "mr"),
                sortedIds(driver.findElements(with(tagName("div")).below(tc).above(bc))));
    }

    // ------------------------------------------------------------------
    // root locators
    // ------------------------------------------------------------------

    @Test
    public void rootLocatorTypes() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement mc = driver.findElement(By.id("mc"));

        final By[] roots = {
            tagName("div"), cssSelector("div"), cssSelector(".c"), xpath("//div[@class='c']"),
            By.className("c"), By.name("cell")
        };
        for (final By root : roots) {
            assertEquals(root.toString(), Arrays.asList("tc", "tl", "tr"),
                    sortedIds(driver.findElements(with(root).above(mc))));
        }
    }

    @Test
    public void rootLocatorRestrictsTheCandidates() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement mc = driver.findElement(By.id("mc"));

        // tl is above mc, br is not
        assertEquals(Arrays.asList("tl"), ids(driver.findElements(with(By.id("tl")).above(mc))));
        assertTrue(driver.findElements(with(By.id("br")).above(mc)).isEmpty());
    }

    // ------------------------------------------------------------------
    // no match
    // ------------------------------------------------------------------

    @Test
    public void noMatchFindElementsReturnsEmptyList() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement tl = driver.findElement(By.id("tl"));

        // nothing is above or left of the top left cell
        assertTrue(driver.findElements(with(tagName("div")).above(tl)).isEmpty());
        assertTrue(driver.findElements(with(tagName("div")).toLeftOf(tl)).isEmpty());
        assertTrue(driver.findElements(with(tagName("span")).below(tl)).isEmpty());
    }

    @Test
    public void noMatchFindElementThrows() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement tl = driver.findElement(By.id("tl"));

        try {
            driver.findElement(with(tagName("div")).above(tl));
            fail("expected NoSuchElementException");
        }
        catch (final NoSuchElementException e) {
            assertTrue(e.getMessage().contains("Cannot locate an element using"));
        }
    }

    // ------------------------------------------------------------------
    // near
    // ------------------------------------------------------------------

    @Test
    public void nearWithDefaultAndExplicitDistance() throws Exception {
        final String html = "<html>\n"
                + "<head>\n"
                + "  <style>\n"
                + "    body { margin: 0; }\n"
                + "    .c { position: absolute; width: 50px; height: 50px; }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div id='ref' class='c' style='left: 100px; top: 100px;'></div>\n"
                + "  <div id='a' class='c' style='left: 140px; top: 100px;'></div>\n"
                + "  <div id='b' class='c' style='left: 100px; top: 140px;'></div>\n"
                + "  <div id='far' class='c' style='left: 400px; top: 400px;'></div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ref = driver.findElement(By.id("ref"));

        // equally far away: DOM order
        assertEquals(Arrays.asList("a", "b"), ids(driver.findElements(with(tagName("div")).near(ref))));
        assertEquals(Arrays.asList("a", "b"), ids(driver.findElements(with(tagName("div")).near(ref, 100))));

        // a far away element is not near, neither as filter nor as root
        assertFalse(ids(driver.findElements(with(tagName("div")).near(ref, 100))).contains("far"));
        try {
            driver.findElement(with(By.id("far")).near(ref));
            fail("expected NoSuchElementException");
        }
        catch (final NoSuchElementException e) {
            assertTrue(e.getMessage().contains("Cannot locate an element using"));
        }
    }

    // ------------------------------------------------------------------
    // dynamic layout
    // ------------------------------------------------------------------

    @Test
    public void layoutChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(grid(
                "  <script>\n"
                + "    var tl = document.getElementById('tl');\n"
                + "    tl.style.left = '100px';\n"
                + "    tl.style.top = '400px';\n"
                + "  </script>\n"));
        final WebElement mc = driver.findElement(By.id("mc"));

        // tl moved from the top left to below the grid
        assertEquals(Arrays.asList("tc", "tr"), sortedIds(driver.findElements(with(tagName("div")).above(mc))));
        assertEquals(Arrays.asList("bc", "bl", "br", "tl"),
                sortedIds(driver.findElements(with(tagName("div")).below(mc))));
    }

    // ------------------------------------------------------------------
    // element context
    // ------------------------------------------------------------------

    @Test
    public void elementContextNeverReturnsNull() throws Exception {
        final WebDriver driver = loadPage2(grid(""));
        final WebElement body = driver.findElement(By.tagName("body"));
        final WebElement mc = driver.findElement(By.id("mc"));

        // whatever the semantic is (searching the whole page like the driver, or being unsupported),
        // a relative locator must never produce null or a NullPointerException
        try {
            assertNotNull(body.findElements(with(tagName("div")).above(mc)));
        }
        catch (final UnsupportedOperationException e) {
            // acceptable
        }

        try {
            body.findElement(with(tagName("div")).above(mc));
        }
        catch (final NoSuchElementException | UnsupportedOperationException e) {
            // acceptable
        }
    }

    @Test
    public void elementContextRestrictsTheCandidates() throws Exception {
        final String html = "<html>\n"
                + "<head>\n"
                + "  <style>\n"
                + "    body { margin: 0; }\n"
                + "    .c { position: absolute; width: 50px; height: 50px; }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <div id='tl' class='c' style='left: 0px; top: 0px;'></div>\n"
                + "    <div id='tr' class='c' style='left: 200px; top: 0px;'></div>\n"
                + "    <div id='bl' class='c' style='left: 0px; top: 200px;'></div>\n"
                + "  </div>\n"
                + "  <div id='tc' class='c' style='left: 100px; top: 0px;'></div>\n"
                + "  <div id='mc' class='c' style='left: 100px; top: 100px;'></div>\n"
                + "  <div id='bc' class='c' style='left: 100px; top: 200px;'></div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement mc = driver.findElement(By.id("mc"));

        // the reference element may be outside of the context, only the candidates are restricted
        assertEquals(Arrays.asList("tl", "tr"), sortedIds(ctx.findElements(with(By.className("c")).above(mc))));
        assertEquals(Arrays.asList("bl"), sortedIds(ctx.findElements(with(By.className("c")).below(mc))));

        // the search on the page finds all of them
        assertEquals(Arrays.asList("tc", "tl", "tr"),
                sortedIds(driver.findElements(with(By.className("c")).above(mc))));
    }
}
