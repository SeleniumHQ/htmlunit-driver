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

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import org.htmlunit.corejs.javascript.JavaScriptException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ScriptTimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;

/**
 * General tests for the HtmlUnitDriver.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class ExecutingAsyncJavascriptTest extends WebDriverTestCase {

    @Test
    public void shouldNotTimeoutIfCallbackInvokedImmediately() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final Object result = executor.executeAsyncScript("arguments[arguments.length - 1](123);");
        assertEquals(123L, result);
    }

    @Test
    public void shouldBeAbleToReturnJavascriptPrimitivesFromAsyncScripts_NeitherNullNorUndefined() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        Object result = executor.executeAsyncScript("arguments[arguments.length - 1](123);");
        assertEquals(123L, result);

        result = executor.executeAsyncScript("arguments[arguments.length - 1]('abc');");
        assertEquals("abc", result);

        result = executor.executeAsyncScript("arguments[arguments.length - 1](false);");
        assertEquals(false, result);

        result = executor.executeAsyncScript("arguments[arguments.length - 1](true);");
        assertEquals(true, result);
    }

    @Test
    public void shouldBeAbleToReturnJavascriptPrimitivesFromAsyncScripts_NullAndUndefined() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        Object result = executor.executeAsyncScript("arguments[arguments.length - 1](null);");
        assertEquals(null, result);

        result = executor.executeAsyncScript("arguments[arguments.length - 1](undefined);");
        assertEquals(null, result);

        result = executor.executeAsyncScript("arguments[arguments.length - 1]();");
        assertEquals(null, result);
    }

    @Test
    public void shouldBeAbleToReturnAnArrayLiteralFromAnAsyncScript() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final Object result = executor.executeAsyncScript("arguments[arguments.length - 1]([]);");
        assertTrue(result instanceof List);
        assertEquals(0, ((List<?>) result).size());
    }

    @Test
    public void shouldBeAbleToReturnAnArrayObjectFromAnAsyncScript() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final Object result = executor.executeAsyncScript("arguments[arguments.length - 1](new Array());");
        assertTrue(result instanceof List);
        assertEquals(0, ((List<?>) result).size());
    }

    @Test
    public void shouldBeAbleToReturnArraysOfPrimitivesFromAsyncScripts() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final Object result =
                executor.executeAsyncScript("arguments[arguments.length - 1]([null, 123, 'abc', true, false]);");

        assertTrue(result instanceof List);
        assertEquals(5, ((List<?>) result).size());

        final Iterator<?> results = ((List<?>) result).iterator();
        assertNull(results.next());
        assertEquals(123L, ((Number) results.next()).longValue());
        assertEquals("abc", results.next());
        assertEquals(true, results.next());
        assertEquals(false, results.next());
        assertFalse(results.hasNext());
    }

    @Test
    public void shouldBeAbleToReturnWebElementsFromAsyncScripts() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final Object result = executor.executeAsyncScript("arguments[arguments.length - 1](document.body);");
        assertTrue(result instanceof WebElement);
        assertEquals("body", ((WebElement) result).getTagName());
    }

    @Test
    public void shouldBeAbleToReturnArraysOfWebElementsFromAsyncScripts() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final Object result = executor.executeAsyncScript(
                                    "arguments[arguments.length - 1]([document.body, document.body]);");

        assertTrue(result instanceof List);
        assertEquals(2, ((List<?>) result).size());

        final List<?> results = (List<?>) result;
        assertTrue(results.get(0) instanceof WebElement);
        assertTrue(results.get(1) instanceof WebElement);
        assertEquals("body", ((WebElement) results.get(1)).getTagName());
        assertEquals(results.get(0), results.get(1));
    }

    @Test
    public void shouldTimeoutIfScriptDoesNotInvokeCallback() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        // Script is expected to be async and explicitly callback, so this should timeout.
        Assert.assertThrows(
                ScriptTimeoutException.class,
                () -> executor.executeAsyncScript("return 1 + 2;"));
    }

    @Test
    public void shouldTimeoutIfScriptDoesNotInvokeCallbackWithAZeroTimeout() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        Assert.assertThrows(
                ScriptTimeoutException.class,
                () -> executor.executeAsyncScript("window.setTimeout(function() {}, 0);"));
    }

    @Test
    public void shouldNotTimeoutIfScriptCallsbackInsideAZeroTimeout() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeAsyncScript(
                    "var callback = arguments[arguments.length - 1];"
                        + "window.setTimeout(function() { callback(123); }, 0)");
    }

    @Test
    public void shouldTimeoutIfScriptDoesNotInvokeCallbackWithLongTimeout() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        Assert.assertThrows(
                ScriptTimeoutException.class,
                () -> executor.executeAsyncScript(
                            "var callback = arguments[arguments.length - 1];"
                                + "window.setTimeout(callback, 1500);"));
    }

    @Test
    public void shouldDetectPageLoadsWhileWaitingOnAnAsyncScriptAndReturnAnError() throws Exception {
        getMockWebConnection().setResponse(URL_SECOND, "<html><body></body></html>");

        final WebDriver driver = loadPage2("<html><body></body></html>");
        // driver.manage().timeouts().scriptTimeout(Duration.ofMillis(100));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        // TODO real FF creates JavascriptException, Chrome/Edge ScriptTimeoutException
        /*
        Assert.assertThrows(
                ScriptTimeoutException.class,
                () -> executor.executeAsyncScript("window.location = '" + URL_SECOND + "';"));
        */
        final Throwable t = Assert.assertThrows(
                Throwable.class,
                () -> executor.executeAsyncScript("window.location = '" + URL_SECOND + "';"));

        assertTrue(t instanceof ScriptTimeoutException || t instanceof JavascriptException);

        if (t instanceof ScriptTimeoutException) {
            assertTrue(t.getMessage().startsWith("script timeout"));
        }
        else if (t instanceof JavaScriptException) {
            assertEquals("Document was unloaded", t.getMessage());
        }
    }

    @Test
    public void shouldCatchErrorsWhenExecutingInitialScript() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        Assert.assertThrows(
                WebDriverException.class,
                () -> executor.executeAsyncScript("throw Error('you should catch this!');"));
    }

    @Test
    public void shouldNotTimeoutWithMultipleCallsTheFirstOneBeingSynchronous() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        Object result = executor.executeAsyncScript("arguments[arguments.length - 1](true);");
        assertEquals(true, result);

        result = executor.executeAsyncScript(
                        "var cb = arguments[arguments.length - 1];"
                                + " window.setTimeout(function(){cb(true);}, 9);");
        assertEquals(true, result);
    }

    @Test
    public void shouldCatchErrorsWithMessageAndStacktraceWhenExecutingInitialScript() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final String js =
                "function functionB() { throw Error('errormessage'); };"
                        + "function functionA() { functionB(); };"
                        + "functionA();";

        Object result = executor.executeAsyncScript("arguments[arguments.length - 1](true);");
        assertEquals(true, result);

        result = executor.executeAsyncScript(
                        "var cb = arguments[arguments.length - 1];"
                                + " window.setTimeout(function(){cb(true);}, 9);");

        final WebDriverException ex =
                Assert.assertThrows(WebDriverException.class, () -> executor.executeAsyncScript(js));

        assertTrue(ex.getMessage().contains("errormessage"));

        final Throwable rootCause = ex.getCause();
        assertTrue(rootCause instanceof Throwable);
        // does not work with real browsers because root cause is null
        assertTrue(rootCause.getMessage().contains("errormessage"));

        /* TODO
        final StackTraceElement[] trace = rootCause.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            final StackTraceElement st = trace[i];
            if (st.getMethodName().contains("functionB")) {
                return;
            }
        }
        fail("stacktrace");
        */
    }

    @Test
    public void shouldBeAbleToExecuteAsynchronousScripts() throws Exception {
        final String html = getFileContent("ajax_page.html");
        final WebDriver driver = loadPage2(html);
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        Object result = executor.executeAsyncScript("arguments[arguments.length - 1](true);");
        assertEquals(true, result);

        result = executor.executeAsyncScript(
                        "var cb = arguments[arguments.length - 1];"
                                + " window.setTimeout(function(){cb(true);}, 9);");
        assertEquals(true, result);

        final WebElement typer = driver.findElement(By.name("typer"));
        typer.sendKeys("bob");
        assertEquals("bob", typer.getAttribute("value"));

        driver.findElement(By.id("red")).click();
        driver.findElement(By.name("submit")).click();

        assertEquals(1L, getNumDivElements(driver));

        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(15));
        final String text =
            (String)
                executor.executeAsyncScript(
                    "var callback = arguments[arguments.length - 1];"
                        + "window.registerListener(arguments[arguments.length - 1]);");

        assertEquals("bob", text);
        assertEquals("", typer.getAttribute("value"));
        assertEquals(2L, getNumDivElements(driver));
    }

    @Test
    public void shouldBeAbleToPassMultipleArgumentsToAsyncScripts() throws Exception {
        final WebDriver driver = loadPage2("<html><body></body></html>");
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final Number result =
            (Number)
                executor.executeAsyncScript(
                    "arguments[arguments.length - 1](arguments[0] + arguments[1]);", 1, 2);
        assertEquals(3, result.intValue());
    }

    /* TODO
      @Test
      void shouldBeAbleToMakeXMLHttpRequestsAndWaitForTheResponse() {
        String script =
            "var url = arguments[0];"
                + "var callback = arguments[arguments.length - 1];"
                +
                // Adapted from http://www.quirksmode.org/js/xmlhttp.html
                "var XMLHttpFactories = ["
                + "  function () {return new XMLHttpRequest()},"
                + "  function () {return new ActiveXObject('Msxml2.XMLHTTP')},"
                + "  function () {return new ActiveXObject('Msxml3.XMLHTTP')},"
                + "  function () {return new ActiveXObject('Microsoft.XMLHTTP')}"
                + "];"
                + "var xhr = false;"
                + "while (!xhr && XMLHttpFactories.length) {"
                + "  try {"
                + "    xhr = XMLHttpFactories.shift().call();"
                + "  } catch (e) {}"
                + "}"
                + "if (!xhr) throw Error('unable to create XHR object');"
                + "xhr.open('GET', url, true);"
                + "xhr.onreadystatechange = function() {"
                + "  if (xhr.readyState == 4) callback(xhr.responseText);"
                + "};"
                + "xhr.send('');"; // empty string to stop firefox 3 from choking

        driver.get(pages.ajaxyPage);
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(3));
        String response = (String) executor.executeAsyncScript(script, pages.sleepingPage + "?time=2");
        assertThat(response.trim())
            .isEqualTo("<html><head><title>Done</title></head><body>Slept for 2s</body></html>");
      }

      @Test
      @Ignore(CHROME)
      @Ignore(EDGE)
      @Ignore(IE)
      @Ignore(FIREFOX)
      @Ignore(value = SAFARI, reason = "Does not support alerts yet")
      public void throwsIfScriptTriggersAlert() {
        driver.get(pages.simpleTestPage);
        driver.manage().timeouts().scriptTimeout(Duration.ofMillis(5000));
        assertThatExceptionOfType(UnhandledAlertException.class)
            .isThrownBy(
                () ->
                    executor.executeAsyncScript(
                        "setTimeout(arguments[0], 200) ; setTimeout(function() { window.alert('Look! An"
                            + " alert!'); }, 50);"));
        // Shouldn't throw
        driver.getTitle();
      }

      @Test
      @Ignore(CHROME)
      @Ignore(EDGE)
      @Ignore(IE)
      @Ignore(FIREFOX)
      @Ignore(value = SAFARI, reason = "Does not support alerts yet")
      public void throwsIfAlertHappensDuringScript() {
        driver.get(pages.slowLoadingAlertPage);
        driver.manage().timeouts().scriptTimeout(Duration.ofMillis(5000));
        assertThatExceptionOfType(UnhandledAlertException.class)
            .isThrownBy(() -> executor.executeAsyncScript("setTimeout(arguments[0], 1000);"));
        // Shouldn't throw
        driver.getTitle();
      }

      @Test
      @Ignore(CHROME)
      @Ignore(EDGE)
      @Ignore(IE)
      @Ignore(FIREFOX)
      @Ignore(value = SAFARI, reason = "Does not support alerts yet")
      public void throwsIfScriptTriggersAlertWhichTimesOut() {
        driver.get(pages.simpleTestPage);
        driver.manage().timeouts().scriptTimeout(Duration.ofMillis(5000));
        assertThatExceptionOfType(UnhandledAlertException.class)
            .isThrownBy(
                () ->
                    executor.executeAsyncScript(
                        "setTimeout(function() { window.alert('Look! An alert!'); }, 50);"));
        // Shouldn't throw
        driver.getTitle();
      }

      @Test
      @Ignore(CHROME)
      @Ignore(EDGE)
      @Ignore(IE)
      @Ignore(FIREFOX)
      @Ignore(value = SAFARI, reason = "Does not support alerts yet")
      public void throwsIfAlertHappensDuringScriptWhichTimesOut() {
        driver.get(pages.slowLoadingAlertPage);
        driver.manage().timeouts().scriptTimeout(Duration.ofMillis(5000));
        assertThatExceptionOfType(UnhandledAlertException.class)
            .isThrownBy(() -> executor.executeAsyncScript(""));
        // Shouldn't throw
        driver.getTitle();
      }

      @Test
      @Ignore(CHROME)
      @Ignore(EDGE)
      @Ignore(IE)
      @Ignore(FIREFOX)
      @Ignore(value = SAFARI, reason = "Does not support alerts yet")
      public void includesAlertTextInUnhandledAlertException() {
        driver.manage().timeouts().scriptTimeout(Duration.ofMillis(5000));
        String alertText = "Look! An alert!";
        assertThatExceptionOfType(UnhandledAlertException.class)
            .isThrownBy(
                () ->
                    executor.executeAsyncScript(
                        "setTimeout(arguments[0], 200) ; setTimeout(function() { window.alert('"
                            + alertText
                            + "'); }, 50);"))
            .satisfies(t -> assertThat(t.getAlertText()).isEqualTo(alertText));
      }

      private long getNumDivElements() {
        // Selenium does not support "findElements" yet, so we have to do this through a script.
        return (Long)
            ((JavascriptExecutor) driver)
                .executeScript("return document.getElementsByTagName('div').length;");
      }
    */

    private static long getNumDivElements(final WebDriver driver) {
        // Selenium does not support "findElements" yet, so we have to do this through a script.
        return (Long)
            ((JavascriptExecutor) driver)
                .executeScript("return document.getElementsByTagName('div').length;");
    }
}
