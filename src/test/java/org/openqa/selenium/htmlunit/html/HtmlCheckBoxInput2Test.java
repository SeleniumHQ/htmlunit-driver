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
import org.openqa.selenium.htmlunit.BrowserRunner;
import org.openqa.selenium.htmlunit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.WebDriverTestCase;

@RunWith(BrowserRunner.class)
public class HtmlCheckBoxInput2Test extends WebDriverTestCase {

  /**
   * @throws Exception if the test fails
   */
  @Test
  @Alerts({"true", "null", "false", "", "false", "yes"})
  public void checkedAttribute() throws Exception {
      final String html =
          HtmlPageTest.STANDARDS_MODE_PREFIX_
          + "<html><head><title>foo</title>\n"
          + "<script>\n"
          + "  function test() {\n"
          + "    var checkbox = document.getElementById('c1');\n"
          + "    alert(checkbox.checked);\n"
          + "    alert(checkbox.getAttribute('checked'));\n"

          + "    checkbox = document.getElementById('c2');\n"
          + "    alert(checkbox.checked);\n"
          + "    alert(checkbox.getAttribute('checked'));\n"

          + "    checkbox = document.getElementById('c3');\n"
          + "    alert(checkbox.checked);\n"
          + "    alert(checkbox.getAttribute('checked'));\n"
          + "  }\n"
          + "</script>\n"
          + "</head><body>\n"
          + "<form>\n"
          + "  <input type='checkbox' id='c1' name='radar' value='initial'>\n"
          + "  <input type='checkbox' id='c2' name='radar' value='initial' checked>\n"
          + "  <input type='checkbox' id='c3' name='radar' value='initial' checked='yes'>\n"
          + "</form>\n"
          + "  <button id='clickMe' onClick='test()'>do it</button>\n"
          + "</body></html>";

      final WebDriver driver = loadPage2(html);
      driver.findElement(By.id("c1")).click();
      driver.findElement(By.id("c2")).click();
      driver.findElement(By.id("c3")).click();

      driver.findElement(By.id("clickMe")).click();
      verifyAlerts(driver, getExpectedAlerts());
  }

}
