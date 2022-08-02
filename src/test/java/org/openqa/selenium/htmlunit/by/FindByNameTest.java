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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;

/**
 * Tests for By.name.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByNameTest extends WebDriverTestCase {

    @Test
    public void elementByName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <input id='testId' name='testName'>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.name("testName"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void elementsByName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <input id='testId' name='testName'>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final List<WebElement> elements = driver.findElements(By.name("testName"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }

    @Test
    public void relativeElementByName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <div id='ctx'>\n"
                        + "      <input id='testId' name='testName'>\n"
                        + "      <input id='testId2' name='testName2'>\n"
                        + "    </div>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.name("testName"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void relativeElementsByName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <div id='ctx'>\n"
                        + "      <input id='testId' name='testName'>\n"
                        + "      <input id='testId2' name='testName2'>\n"
                        + "    </div>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final List<WebElement> elements = ctx.findElements(By.name("testName"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }
}
