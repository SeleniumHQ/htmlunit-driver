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

package org.openqa.selenium.htmlunit.html;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.support.ui.Select;

@RunWith(BrowserRunner.class)
public class SelectTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void select() throws Exception {
        final String html =
            "<html>\n"
            + "<head></head>\n"
            + "<body>\n"
            + "<form>\n"
            + "  <select id='selectWithoutMultiple'>\n"
            + "    <option value='one'>one</option>\n"
            + "    <option value='two'>two</option>\n"
            + "  </select>\n"
            + "</form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("selectWithoutMultiple"));
        assertNotNull(element);

        final Select select = new Select(element);
        assertFalse(select.isMultiple());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void selectMultipleEqualsMultiple() throws Exception {
        final String html =
            "<html>\n"
            + "<head></head>\n"
            + "<body>\n"
            + "<form>\n"
            + "  <select id='selectWithMultipleEqualsMultiple' multiple='multiple'>\n"
            + "    <option value='one'>one</option>\n"
            + "    <option value='two'>two</option>\n"
            + "  </select>\n"
            + "</form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("selectWithMultipleEqualsMultiple"));
        assertNotNull(element);

        final Select select = new Select(element);
        assertTrue(select.isMultiple());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void selectWithMultipleWithoutValue() throws Exception {
        final String html =
            "<html>\n"
            + "<head></head>\n"
            + "<body>\n"
            + "<form>\n"
            + "  <select id='selectWithMultipleWithoutValue' multiple>\n"
            + "    <option value='one'>one</option>\n"
            + "    <option value='two'>two</option>\n"
            + "  </select>\n"
            + "</form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("selectWithMultipleWithoutValue"));
        assertNotNull(element);

        final Select select = new Select(element);
        assertTrue(select.isMultiple());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void selectWithRandomMultipleValue() throws Exception {
        final String html =
            "<html>\n"
            + "<head></head>\n"
            + "<body>\n"
            + "<form>\n"
            + "  <select id='selectWithRandomMultipleValue' multiple='somethingElse'>\n"
            + "    <option value='one'>one</option>\n"
            + "    <option value='two'>two</option>\n"
            + "  </select>\n"
            + "</form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("selectWithRandomMultipleValue"));
        assertNotNull(element);

        final Select select = new Select(element);
        assertTrue(select.isMultiple());
    }
}
