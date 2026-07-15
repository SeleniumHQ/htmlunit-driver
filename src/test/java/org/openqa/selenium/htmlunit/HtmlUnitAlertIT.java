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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.BuggyWebDriver;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.NotYetImplemented;

import static org.junit.Assert.fail;

/**
 * Alert tests. (External Domain Tests)
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitAlertIT extends WebDriverTestCase {

    @Test
    public void confirmWithRedirect() throws Exception {
        final String message = "Are you sure?";

        final String html = "<html>\n"
                + "<a id='confirm' href='https://htmlunit.sourceforge.io/' "
                + "onclick='return confirm(\"" + message + "\");'>Confirm</a>\n"
                + "<div id='message'>Default</div>"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("confirm")).click();

        assertEquals(message, driver.switchTo().alert().getText());
        driver.switchTo().alert().accept();

        // sometimes the page is slow
        Thread.sleep(4 * DEFAULT_WAIT_TIME);

        assertTrue("Remote Url test: Title was '" + driver.getTitle() + "' expected 'Welcome to HtmlUnit'",
                driver.getTitle().contains("Welcome to HtmlUnit"));
    }
}
