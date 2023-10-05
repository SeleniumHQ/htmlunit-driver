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
public class MouseTest extends WebDriverTestCase {

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("mouse over [btn]")
    public void mouseOver() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html>\n"
            + "  <head>\n"
            + "    <title>Test</title>\n"
            + "    <script>\n"
            + "    function dumpEvent(event) {\n"
            + "      // target\n"
            + "      var eTarget;\n"
            + "      if (event.target) {\n"
            + "        eTarget = event.target;\n"
            + "      } else if (event.srcElement) {\n"
            + "        eTarget = event.srcElement;\n"
            + "      }\n"
            + "      var msg = 'mouse over';\n"
            + "      if (eTarget.name) {\n"
            + "        msg = msg + ' [' + eTarget.name + ']';\n"
            + "      } else {\n"
            + "        msg = msg + ' [' + eTarget.id + ']';\n"
            + "      }\n"
            + "      alert(msg);\n"
            + "    }\n"
            + "    </script>\n"
            + "  </head>\n"
            + "<body>\n"
            + "  <form id='form1'>\n"
            + "    <button id='btn' onmouseover='dumpEvent(event);'>button</button><br>\n"
            + "  </form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);

        final Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(By.id("btn")));
        actions.perform();

        verifyAlerts(driver, getExpectedAlerts());
    }
    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "Test:mouse over [disabledBtn]",
            IE = "Test:")
    public void mouseOverDiabled() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html>\n"
            + "  <head>\n"
            + "    <title>Test:</title>\n"
            + "    <script>\n"
            + "    function dumpEvent(event) {\n"
            + "      // target\n"
            + "      var eTarget;\n"
            + "      if (event.target) {\n"
            + "        eTarget = event.target;\n"
            + "      } else if (event.srcElement) {\n"
            + "        eTarget = event.srcElement;\n"
            + "      }\n"
            + "      var msg = 'mouse over';\n"
            + "      if (eTarget.name) {\n"
            + "        msg = msg + ' [' + eTarget.name + ']';\n"
            + "      } else {\n"
            + "        msg = msg + ' [' + eTarget.id + ']';\n"
            + "      }\n"
            + "      document.title += msg;\n"
            + "    }\n"
            + "    </script>\n"
            + "  </head>\n"
            + "<body>\n"
            + "  <form id='form1'>\n"
            + "    <button id='disabledBtn' onmouseover='dumpEvent(event);' disabled>disabled button</button><br>\n"
            + "  </form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);

        final Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(By.id("disabledBtn")));
        actions.perform();

        assertTitle(driver, getExpectedAlerts()[0]);
    }
}
