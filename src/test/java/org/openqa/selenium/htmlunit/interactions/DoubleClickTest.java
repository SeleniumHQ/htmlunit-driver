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

package org.openqa.selenium.htmlunit.interactions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.interactions.Actions;

/**
 * Tests for double click action.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class DoubleClickTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("click click dblclick ")
    public void dblClick() throws Exception {
        final String content = "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "  function clickMe() {\n"
            + "    document.getElementById('myTextarea').value+='click ';\n"
            + "  }\n"
            + "  function dblClickMe() {\n"
            + "    document.getElementById('myTextarea').value+='dblclick ';\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body id='myBody' onclick='clickMe()' ondblclick='dblClickMe()'>\n"
            + "<textarea id='myTextarea'></textarea>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(content);

        final Actions action = new Actions(driver);
        action.doubleClick(driver.findElement(By.id("myBody")));
        action.perform();

        assertEquals(getExpectedAlerts()[0], driver.findElement(By.id("myTextarea")).getAttribute("value"));
    }
}
