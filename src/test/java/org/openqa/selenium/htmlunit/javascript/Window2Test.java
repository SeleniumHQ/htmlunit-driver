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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.htmlunit.BrowserRunner;
import org.openqa.selenium.htmlunit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.WebDriverTestCase;

@RunWith(BrowserRunner.class)
public class Window2Test extends WebDriverTestCase {

  /**
   * @throws Exception if the test fails
   */
  @Test
  @Alerts({"false", "false", "test2 alert"})
  public void objectCallOnFrameWindow() throws Exception {
    final String firstContent = "<html><head>\n"
        + "<script>\n"
        + "  function test1() {\n"
        + "    alert(window.frames[0].test2 === undefined);\n"
        + "    Object(window.frames[0]);\n"
        + "  }\n"
        + "</script>\n"
        + "</head>\n"
        + "<body>\n"
        + "  <iframe src='" + URL_SECOND + "'></iframe>\n"
        + "</body></html>\n";
    final String secondContent = "<html><head>\n"
        + "<script>\n"
        + "  function test2() {\n"
        + "    alert('test2 alert');\n"
        + "  }\n"
        + "  window.top.test1();\n"
        + "  alert(test2 === undefined);\n"
        + "</script>\n"
        + "</head>\n"
        + "<body onload='test2()'>\n"
        + "</body></html>\n";

    getMockWebConnection().setResponse(URL_SECOND, secondContent);

    loadPageWithAlerts2(firstContent);
  }

}
