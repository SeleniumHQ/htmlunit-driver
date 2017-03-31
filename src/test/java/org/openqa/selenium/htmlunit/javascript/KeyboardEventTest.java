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

package org.openqa.selenium.htmlunit.javascript;

import static org.openqa.selenium.htmlunit.BrowserRunner.Browser.FF;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.BrowserRunner;
import org.openqa.selenium.htmlunit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.BrowserRunner.NotYetImplemented;
import org.openqa.selenium.htmlunit.WebDriverTestCase;

@RunWith(BrowserRunner.class)
public class KeyboardEventTest extends WebDriverTestCase {

  /**
   * @throws Exception if the test fails
   */
  @Test
  @Alerts(DEFAULT = {"keydown:16,0,16",
                  "keydown:65,0,65",
                  "keypress:65,65,65",
                  "keyup:65,0,65",
                  "keyup:16,0,16",
                  "keydown:65,0,65",
                  "keypress:97,97,97",
                  "keyup:65,0,65",
                  "keydown:190,0,190",
                  "keypress:46,46,46",
                  "keyup:190,0,190",
                  "keydown:13,0,13",
                  "keypress:13,13,13",
                  "keyup:13,0,13"},
          FF = {  "keydown:65,0,65",
                  "keypress:0,65,65",
                  "keyup:65,0,65",
                  "keydown:65,0,65",
                  "keypress:0,97,97",
                  "keyup:65,0,65",
                  "keydown:190,0,190",
                  "keypress:0,46,46",
                  "keyup:190,0,190",
                  "keydown:13,0,13",
                  "keypress:13,0,13",
                  "keyup:13,0,13"})
  @NotYetImplemented(FF)
  public void which() throws Exception {
      final String html
          = "<html><head></head><body>\n"
          + "<input type='text' id='keyId'>\n"
          + "<script>\n"
          + "function handler(e) {\n"
          + "  e = e ? e : window.event;\n"
          + "  document.getElementById('myTextarea').value "
          + "+= e.type + ':' + e.keyCode + ',' + e.charCode + ',' + e.which + '\\n';\n"
          + "}\n"
          + "document.getElementById('keyId').onkeyup = handler;\n"
          + "document.getElementById('keyId').onkeydown = handler;\n"
          + "document.getElementById('keyId').onkeypress = handler;\n"
          + "</script>\n"
          + "<textarea id='myTextarea' cols=80 rows=20></textarea>\n"
          + "</body></html>";
      final String keysToSend = "Aa." + Keys.RETURN;
      final WebDriver driver = loadPage2(html);
      driver.findElement(By.id("keyId")).sendKeys(keysToSend);

      final String[] actual = driver.findElement(By.id("myTextarea")).getAttribute("value").split("\r\n|\n");
      assertEquals(Arrays.asList(getExpectedAlerts()).toString(), Arrays.asList(actual).toString());
  }

}
