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

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.HtmlUnitNYI;

/**
 * General tests for the HtmlUnitDriver.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitDriver2Test extends WebDriverTestCase {

    @Test
    public void executeScriptWithoutPage() {
        final WebDriver webDriver = getWebDriver();
        final String text = (String) ((JavascriptExecutor) webDriver).executeScript("return 'test';");

        assertEquals("test", text);
    }

    @Test
    @Alerts({"1| [0]HtmlUnit string", "1| [0] string"})
    public void executeScriptParamString() throws Exception {
        executeScriptParam(getExpectedAlerts()[0], "HtmlUnit");
        executeScriptParam(getExpectedAlerts()[1], "");
    }

    @Test
    @Alerts({"1| [0]true boolean", "1| [0]false boolean", "1| [0]true boolean"})
    public void executeScriptParamBoolean() throws Exception {
        executeScriptParam(getExpectedAlerts()[0], true);
        executeScriptParam(getExpectedAlerts()[1], false);
        executeScriptParam(getExpectedAlerts()[2], Boolean.TRUE);
    }

    @Test
    @Alerts({"1| [0]4711 number",
             "1| [0]-1234567890155 number",
             "1| [0]0 number",
             "1| [0]3.141592653589793 number",
             "1| [0]4444 number"})
    @HtmlUnitNYI(
            CHROME = {"1| [0]4711 number",
                      "1| [0]-1234567890155 number",
                      "1| [0]0 number",
                      "1| [0]3.141592653589793 number",
                      "1| [0]4444 bigint"},
            EDGE = {"1| [0]4711 number",
                    "1| [0]-1234567890155 number",
                    "1| [0]0 number",
                    "1| [0]3.141592653589793 number",
                    "1| [0]4444 bigint"},
            FF = {"1| [0]4711 number",
                  "1| [0]-1234567890155 number",
                  "1| [0]0 number",
                  "1| [0]3.141592653589793 number",
                  "1| [0]4444 bigint"},
            FF_ESR = {"1| [0]4711 number",
                      "1| [0]-1234567890155 number",
                      "1| [0]0 number",
                      "1| [0]3.141592653589793 number",
                      "1| [0]4444 bigint"})
    public void executeScriptParamNumber() throws Exception {
        executeScriptParam(getExpectedAlerts()[0], 4711);
        executeScriptParam(getExpectedAlerts()[1], -1234567890155L);
        executeScriptParam(getExpectedAlerts()[2], 0f);
        executeScriptParam(getExpectedAlerts()[3], Math.PI);
        executeScriptParam(getExpectedAlerts()[4], BigInteger.valueOf(4444));
    }

    @Test
    @Alerts({"2| [0]Html string [1]Unit string", "0|"})
    public void executeScriptParamSimpleArray() throws Exception {
        executeScriptParam(getExpectedAlerts()[0], new String[] {"Html", "Unit"});
        executeScriptParam(getExpectedAlerts()[1], new String[] {});
    }

    @Test
    @Alerts({"1| [0]1,-2 array",
             "1| [0]1,-2 array",
             "1| [0]1,-2 array",
             "1| [0]1,-2 array",
             "1| [0]false array"})
    public void executeScriptParamPrimitiveArray() throws Exception {
        executeScriptParam(getExpectedAlerts()[0], new int[] {1, -2});
        executeScriptParam(getExpectedAlerts()[1], new long[] {1L, -2L});
        executeScriptParam(getExpectedAlerts()[2], new float[] {1f, -2f});
        executeScriptParam(getExpectedAlerts()[3], new double[] {1d, -2d});
        executeScriptParam(getExpectedAlerts()[4], new boolean[] {false});
    }

    @Test
    @Alerts({"3| [0]Html string [1]17 number [2]true boolean",
             "3| [0]Html string [1]42,7 array [2]true boolean"})
    public void executeScriptParamMixedArray() throws Exception {
        executeScriptParam(getExpectedAlerts()[0], new Object[] {"Html", 17, true});
        executeScriptParam(getExpectedAlerts()[1], new Object[] {"Html", new int[] {42, 7}, true});
    }

    @Test
    @Alerts({"1| [0][object Object] object ()",
             "1| [0][object Object] object (Html->Unit )",
             "1| [0][object Object] object (Zahl->17 )"})
    public void executeScriptParamSimpleMap() throws Exception {
        Map<String, Object> param = new HashMap<>();

        executeScriptParam(getExpectedAlerts()[0], param);

        param.put("Html", "Unit");
        executeScriptParam(getExpectedAlerts()[1], param);

        param = new HashMap<>();
        param.put("Zahl", 17);
        executeScriptParam(getExpectedAlerts()[2], param);
    }

    private void executeScriptParam(final Object expected, final Object... params) throws Exception {
        final String html = "<html><head><title>Tester</title></head></html>";
        final WebDriver webDriver = loadPage2(html);

        final String js = "let result = '';\n"
                + "result += arguments.length;\n"
                + "result += '|';\n"
                + "for (let i = 0; i < arguments.length; i++) {\n"
                + "  result += ' [' + i + ']' + arguments[i];"
                + "  if (Array.isArray(arguments[i])) {\n"
                + "    result += ' array';\n"
                + "  } else if (Object.getPrototypeOf(arguments[i]) === Map.prototype) {\n"
                + "    result += ' map';\n"
                + "  } else {\n"
                + "    result += ' ' + typeof arguments[i];\n"
                + "    if ('object' === typeof arguments[i]) {\n"
                + "      result += ' (';\n"
                + "      let props = Object.getOwnPropertyNames(arguments[i]);\n"
                + "      for (let p = 0; p < props.length; p++) {\n"
                + "        result += props[p];\n"
                + "        result += '->';\n"
                + "        result += arguments[i][props[p]] + ' ';\n"
                + "      }\n"
                + "      result += ')';\n"
                + "    }\n"
                + "  }\n"
                + "}\n"
                + "return result;\n";

        final String text = (String) ((JavascriptExecutor) webDriver).executeScript(js, params);
        assertEquals(expected, text);
    }

    @Test
    public void executeScriptParamWebElement() throws Exception {
        final String html = "<html><head><title>Tester</title></head>\n"
                + "<body><div id='myDivId'>diff</div></html>";
        final WebDriver webDriver = loadPage2(html);

        final WebElement webElement = webDriver.findElement(By.id("myDivId"));

        final String js = "return arguments[0].outerHTML;";
        final String text = (String) ((JavascriptExecutor) webDriver).executeScript(js, webElement);
        assertEquals("<div id=\"myDivId\">diff</div>", text);
    }

    @Test
    public void getNotExistingUrl() throws Exception {
        final WebDriver webDriver = getWebDriver();

        try {
            webDriver.get("https://getnotexistingurl_" + System.currentTimeMillis() + ".ace");
            fail("WebDriverException expected");
        }
        catch (final WebDriverException e) {
            // expected
            if (webDriver instanceof HtmlUnitDriver) {
                assertTrue(e.getMessage(),
                        e.getMessage()
                        .startsWith("java.net.UnknownHostException: No such host is known (getnotexistingurl_"));
            }
        }
    }

    @Test
    @Alerts(DEFAULT = "Privacy error",
            FF = "self-signed.badssl.com",
            FF_ESR = "self-signed.badssl.com")
    @HtmlUnitNYI(CHROME = "self-signed.badssl.com",
            EDGE = "self-signed.badssl.com")
    public void getSslSelfSigned() throws Exception {
        final WebDriver webDriver = getWebDriver();

        webDriver.get("https://self-signed.badssl.com");
        assertEquals(getExpectedAlerts()[0], webDriver.getTitle());
        assertEquals("https://self-signed.badssl.com/", webDriver.getCurrentUrl());
    }

    @Test
    @Alerts(DEFAULT = "Privacy error",
            FF = "wrong.host.badssl.com",
            FF_ESR = "wrong.host.badssl.com")
    @HtmlUnitNYI(CHROME = "wrong.host.badssl.com",
            EDGE = "wrong.host.badssl.com")
    public void getSslWrongHost() throws Exception {
        final WebDriver webDriver = getWebDriver();

        webDriver.get("https://wrong.host.badssl.com/");
        assertEquals(getExpectedAlerts()[0], webDriver.getTitle());
        assertEquals("https://wrong.host.badssl.com/", webDriver.getCurrentUrl());
    }

    @Test
    @Alerts("revoked.badssl.com")
    public void getSslRevoked() throws Exception {
        final WebDriver webDriver = getWebDriver();

        webDriver.get("https://revoked.badssl.com");
        assertEquals(getExpectedAlerts()[0], webDriver.getTitle());
        assertEquals("https://revoked.badssl.com/", webDriver.getCurrentUrl());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "Given xpath expression \"//svg:svg//svg:text\" is invalid",
            CHROME = "Something",
            EDGE = "Something")
    @HtmlUnitNYI(FF = "Something",
            FF_ESR = "Something")
    public void shouldBeAbleToFindElementByXPathInXmlDocument() throws Exception {
        final String html = "<?xml version='1.0' encoding='UTF-8'?>\n"
                + "<html xmlns='http://www.w3.org/1999/xhtml'\n"
                + "      xmlns:svg='http://www.w3.org/2000/svg'\n"
                + "      xmlns:xlink='http://www.w3.org/1999/xlink'>\n"
                + "<body>\n"
                + "  <svg:svg id='chart_container' height='220' width='400'>\n"
                + "    <svg:text y='16' x='200' text-anchor='middle'>Something</svg:text>\n"
                + "  </svg:svg>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html, URL_FIRST, "application/xhtml+xml", ISO_8859_1, null);
        String actual;
        try {
            final WebElement element = driver.findElement(By.xpath("//svg:svg//svg:text"));
            assertEquals(getExpectedAlerts()[0], element.getText());
        }
        catch (final InvalidSelectorException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(getExpectedAlerts()[0]));
        }
    }
}
