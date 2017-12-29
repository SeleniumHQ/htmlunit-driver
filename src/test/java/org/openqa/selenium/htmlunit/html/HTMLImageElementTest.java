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
import org.openqa.selenium.htmlunit.BrowserRunner;
import org.openqa.selenium.htmlunit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.WebDriverTestCase;

@RunWith(BrowserRunner.class)
public class HTMLImageElementTest extends WebDriverTestCase {

  /**
   * Test that image's width and height are numbers.
   * @throws Exception if the test fails
   */
  @Test
  @Alerts(DEFAULT = {"number: 300", "number: 200", "number: 24", "number: 24", "number: 24", "number: 24"},
          CHROME = {"number: 300", "number: 200", "number: 16", "number: 16", "number: 16", "number: 16"},
          IE = {"number: 300", "number: 200", "number: 28", "number: 30", "number: 28", "number: 30"})
  public void widthHeightInvalidSource() throws Exception {
      getMockWebConnection().setDefaultResponse("");

      final String html = "<html><head>\n"
          + "<script>\n"
          + "  function showInfo(imageId) {\n"
          + "    var img = document.getElementById(imageId);\n"
          + "    alert(typeof(img.width) + ': ' + img.width);\n"
          + "    alert(typeof(img.height) + ': ' + img.height);\n"
          + "  }\n"
          + "  function test() {\n"
          + "    showInfo('myImage1');\n"
          + "    showInfo('myImage2');\n"
          + "    showInfo('myImage3');\n"
          + "  }\n"
          + "</script>\n"
          + "</head><body onload='test()'>\n"
          + "  <img id='myImage1' src='" + URL_SECOND + "' width='300' height='200'>\n"
          + "  <img id='myImage2' src='" + URL_SECOND + "' >\n"
          + "  <img id='myImage3' src='" + URL_SECOND + "' width='hello' height='hello'>\n"
          + "</body></html>";

      loadPageWithAlerts2(html);
  }

}
