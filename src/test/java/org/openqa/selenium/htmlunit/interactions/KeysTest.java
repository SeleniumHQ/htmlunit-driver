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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.BuggyWebDriver;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.HtmlUnitNYI;
import org.openqa.selenium.interactions.Actions;

/**
 * Comprehensive compatibility tests for keyboard interaction pathways.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class KeysTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_DirectInput() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("abc");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("first\\nsecond")
    public void sendKeys_DirectTextarea() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION_NORMALIZE
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <textarea id='t'></textarea>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("first\nsecond");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_ActionsTargetElement() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver).sendKeys(t, "abc").perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_ActionsActiveElement() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.click();
        new Actions(driver).sendKeys("abc").perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_ActionsChainedFocusAndType() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .moveToElement(t)
                .click()
                .sendKeys("abc")
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that sendKeys appends to an input that already has a value
     * (start-at-end behaviour).
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("prefixabc")
    public void sendKeys_AppendsToPreexistingValue() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' value='prefix'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys("abc");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that sendKeys with an empty string is a no-op on value.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_EmptyStringIsNoop() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' value='abc'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys("");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that sendKeys fires keydown, keypress, and keyup events in
     * that order for a single printable character.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("keydown,keypress,keyup")
    public void sendKeys_EventOrderForSingleChar() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    const t = document.getElementById('t');\n"
            + "    const events = [];\n"
            + "    t.addEventListener('keydown',  function() { events.push('keydown');  });\n"
            + "    t.addEventListener('keypress', function() { events.push('keypress'); });\n"
            + "    t.addEventListener('keyup',    function() { events.push('keyup');    });\n"
            + "    t.addEventListener('keyup', function() { log(events.join(',')); });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys("a");

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that the input event fires after the value is updated by sendKeys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("input:a")
    public void sendKeys_InputEventFires() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('input', function(e) {\n"
            + "      log('input:' + e.target.value);\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys("a");

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that the change event fires when focus leaves a field
     * whose value was changed by sendKeys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("changed:abc")
    public void sendKeys_ChangeEventOnBlur() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('change', function(e) {\n"
            + "      log('changed:' + e.target.value);\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <input type='text' id='other'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys("abc");
        driver.findElement(By.id("other")).click();   // move focus away

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that maxlength on an input is respected — characters beyond the
     * limit are silently discarded.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("ab")
    public void sendKeys_MaxLengthRespected() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' maxlength='2'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys("abcdef");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that textarea respects maxlength.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("ab")
    @Ignore
    public void sendKeys_TextareaMaxLengthRespected() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <textarea id='t' maxlength='2'></textarea>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys("abcdef");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that HOME in a textarea moves the caret to the start of the
     * current line, not the start of the whole text.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("line1\\nXline2")
    public void sendKeys_TextareaHomeMovesToLineStart() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION_NORMALIZE
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <textarea id='t'></textarea>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // type two lines, move to start of second line with HOME, insert 'X'
        t.sendKeys("line1\nline2", Keys.HOME, "X");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that ARROW_UP in a textarea moves the caret to the previous line.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("line1X\\nline2")
    public void sendKeys_TextareaArrowUpMovesToPreviousLine() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION_NORMALIZE
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <textarea id='t'></textarea>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // caret is at end of line2; move up to end of line1 and insert 'X'
        t.sendKeys("line1\nline2", Keys.ARROW_UP, Keys.END, "X");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that ARROW_DOWN in a textarea moves the caret to the next line.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("line1\\nXline2")
    public void sendKeys_TextareaArrowDownMovesToNextLine() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION_NORMALIZE
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <textarea id='t'></textarea>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // start at end of line1, move down to line2 start, insert 'X'
        t.sendKeys("line1\nline2", Keys.HOME, Keys.ARROW_UP, Keys.END, Keys.ARROW_DOWN, Keys.HOME, "X");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that CTRL+HOME in a textarea moves the caret to the very start
     * of the text (across all lines).
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("Xline1\\nline2")
    public void sendKeys_TextareaCtrlHomeMovesToDocumentStart() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION_NORMALIZE
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <textarea id='t'></textarea>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .click(t)
                .sendKeys("line1\nline2")
                .keyDown(Keys.CONTROL)
                .sendKeys(Keys.HOME)
                .keyUp(Keys.CONTROL)
                .sendKeys("X")
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that CTRL+END in a textarea moves the caret to the very end
     * of the text (across all lines).
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("line1\\nline2X")
    public void sendKeys_TextareaCtrlEndMovesToDocumentEnd() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION_NORMALIZE
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <textarea id='t'></textarea>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .click(t)
                .sendKeys("line1\nline2")
                .keyDown(Keys.CONTROL)
                .sendKeys(Keys.HOME)    // go to start
                .keyUp(Keys.CONTROL)
                .keyDown(Keys.CONTROL)
                .sendKeys(Keys.END)     // go to end
                .keyUp(Keys.CONTROL)
                .sendKeys("X")
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that ENTER does NOT submit the form when sent to a textarea
     * (it only inserts a newline).
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("line1\\nline2")
    @Ignore
    public void sendKeys_TextareaEnterDoesNotSubmitForm() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION_NORMALIZE
            + "  function init() {\n"
            + "    document.getElementById('f').addEventListener('submit', function(e) {\n"
            + "      log('SUBMITTED');\n"
            + "      e.preventDefault();\n"
            + "    });\n"
            + "  }\n"
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <form id='f'>\n"
            + "    <textarea id='t'></textarea>\n"
            + "    <input type='submit' id='sub'/>\n"
            + "  </form>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys("line1", Keys.ENTER, "line2");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("ab")
    public void sendKeys_Backspace() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("abc", Keys.BACK_SPACE);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("ac")
    public void sendKeys_DeleteWithArrowKeys() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("abc", Keys.ARROW_LEFT, Keys.ARROW_LEFT, Keys.DELETE);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_ArrowNavigationAndInsertion() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("ac", Keys.ARROW_LEFT, "b");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abcd")
    public void sendKeys_HomeAndEndKeys() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("bc", Keys.HOME, "a", Keys.END, "d");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("line1\\nline2")
    @Ignore
    public void sendKeys_EnterInTextarea() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION_NORMALIZE
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <textarea id='t'></textarea>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("line1", Keys.ENTER, "line2");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"first", "second"})
    @Ignore
    public void sendKeys_TabNavigationBetweenFields() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t1').value);\n"
            + "    log(document.getElementById('t2').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t1'/>\n"
            + "  <input type='text' id='t2'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t1 = driver.findElement(By.id("t1"));

        t1.sendKeys("first", Keys.TAB, "second");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that CTRL+Z (undo) restores the previous value.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("")
    @Ignore
    public void sendKeys_ControlUndo() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .click(t)
                .sendKeys("abc")
                .keyDown(Keys.CONTROL)
                .sendKeys("z")
                .keyUp(Keys.CONTROL)
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies CTRL+SHIFT+END selects from the current caret position to the
     * end of the input and that typing replaces the selection.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("aX")
    public void sendKeys_CtrlShiftEndSelectsToEnd() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // type 'abcd', go to start, move one right, select to end, replace
        new Actions(driver)
                .click(t)
                .sendKeys("abcd")
                .sendKeys(Keys.HOME)
                .sendKeys(Keys.ARROW_RIGHT)
                .keyDown(Keys.SHIFT)
                .sendKeys(Keys.END)
                .keyUp(Keys.SHIFT)
                .sendKeys("X")
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies CTRL+SHIFT+HOME selects from the current caret position to
     * the start of the input and that typing replaces the selection.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("Xd")
    public void sendKeys_CtrlShiftHomeSelectsToStart() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // type 'abcd', move one left, select back to start, replace
        new Actions(driver)
                .click(t)
                .sendKeys("abcd")
                .sendKeys(Keys.ARROW_LEFT)
                .keyDown(Keys.SHIFT)
                .sendKeys(Keys.HOME)
                .keyUp(Keys.SHIFT)
                .sendKeys("X")
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that CTRL+BACKSPACE deletes the word immediately before the caret.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("hello ")
    public void sendKeys_CtrlBackspaceDeletesWord() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .click(t)
                .sendKeys("hello world")
                .keyDown(Keys.CONTROL)
                .sendKeys(Keys.BACK_SPACE)
                .keyUp(Keys.CONTROL)
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that CTRL+DELETE deletes the word immediately after the caret.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("hello ")
    public void sendKeys_CtrlDeleteDeletesWordForward() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .click(t)
                .sendKeys("hello world")
                .sendKeys(Keys.HOME)
                .sendKeys(Keys.ARROW_RIGHT) // move past "hello "
                .sendKeys(Keys.ARROW_RIGHT)
                .sendKeys(Keys.ARROW_RIGHT)
                .sendKeys(Keys.ARROW_RIGHT)
                .sendKeys(Keys.ARROW_RIGHT)
                .sendKeys(Keys.ARROW_RIGHT)
                .keyDown(Keys.CONTROL)
                .sendKeys(Keys.DELETE)
                .keyUp(Keys.CONTROL)
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies CTRL+ARROW_RIGHT (word jump) moves the caret one word forward.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("hello Xworld")
    public void sendKeys_CtrlArrowRightJumpsWord() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // type, go to start, jump one word right, insert 'X'
        new Actions(driver)
                .click(t)
                .sendKeys("hello world")
                .sendKeys(Keys.HOME)
                .keyDown(Keys.CONTROL)
                .sendKeys(Keys.ARROW_RIGHT)
                .keyUp(Keys.CONTROL)
                .sendKeys("X")
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies CTRL+ARROW_LEFT (word jump) moves the caret one word backward.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("Xhello world")
    public void sendKeys_CtrlArrowLeftJumpsWord() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // at end of "hello world", jump two words left, insert 'X'
        new Actions(driver)
                .click(t)
                .sendKeys("hello world")
                .keyDown(Keys.CONTROL)
                .sendKeys(Keys.ARROW_LEFT)
                .sendKeys(Keys.ARROW_LEFT)
                .keyUp(Keys.CONTROL)
                .sendKeys("X")
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("HellO")
    public void sendKeys_ActionsShiftModifier() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .moveToElement(t)
                .click()
                .sendKeys("Hell")
                .keyDown(Keys.SHIFT)
                .sendKeys("o")
                .keyUp(Keys.SHIFT)
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("Replaced")
    public void sendKeys_ActionsControlSelectAllAndReplace() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' value='Initial Text'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .moveToElement(t)
                .click()
                .keyDown(Keys.CONTROL)
                .sendKeys("a")
                .keyUp(Keys.CONTROL)
                .sendKeys("Replaced")
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("alt:true,ctrl:false,shift:false")
    public void sendKeys_ActionsAltModifier() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key === 'x' || e.keyCode === 88) {\n"
            + "        log('alt:' + e.altKey + ',ctrl:' + e.ctrlKey + ',shift:' + e.shiftKey);\n"
            + "      }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .moveToElement(t)
                .click()
                .keyDown(Keys.ALT)
                .sendKeys("x")
                .keyUp(Keys.ALT)
                .perform();

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("ABCd")
    @BuggyWebDriver(FF = "ABCD", FF_ESR = "ABCD")
    public void sendKeys_ChordShiftAndAutoRelease() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.chord(Keys.SHIFT, "abc"), "d");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("Replaced")
    @BuggyWebDriver(FF = "Initial Text", FF_ESR = "Initial Text")
    public void sendKeys_ChordControlSelectAllAndReplace() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' value='Initial Text'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.chord(Keys.CONTROL, "a"), "Replaced");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"alt:true", "alt:false"},
            FF = {"alt:true", "alt:true"},
            FF_ESR = {"alt:true", "alt:true"})
    @HtmlUnitNYI(
            FF = {"alt:true", "alt:false"},
            FF_ESR = {"alt:true", "alt:false"})
    public void sendKeys_ChordAltAndAutoRelease() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key === 'x' || e.keyCode === 88) {\n"
            + "        log('alt:' + e.altKey);\n"
            + "      } else if (e.key === 'y' || e.keyCode === 89) {\n"
            + "        log('alt:' + e.altKey);\n"
            + "      }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.chord(Keys.ALT, "x"), "y");

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("")
    public void sendKeys_ChordSelectAllAndDelete() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' value='Clear Me'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("HELLO WORLD")
    @BuggyWebDriver(FF = "HELLO world", FF_ESR = "HELLO world")
    public void sendKeys_MultipleChords() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.chord(Keys.SHIFT, "hello"), " ", Keys.chord(Keys.SHIFT, "world"));

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that sending Keys.ENTER to a form text input triggers form submission
     * with the input value present up to the ENTER key.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"submit:before"})
    @Ignore
    public void sendKeys_EnterInFormInputTriggersSubmit() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('f').addEventListener('submit', function(e) {\n"
            + "      log('submit:' + document.getElementById('t').value);\n"
            + "      e.preventDefault();\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <form id='f'>\n"
            + "    <input type='text' id='t'/>\n"
            + "  </form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("before", Keys.ENTER, "after");

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that embedding a newline character ('\n') inside a sendKeys string
     * in a form text input acts as ENTER and triggers form submission.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"submit:before"},
            FF = {},
            FF_ESR = {})
    @Ignore
    public void sendKeys_NewlineInFormInputTriggersSubmit() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('f').addEventListener('submit', function(e) {\n"
            + "      log('submit:' + document.getElementById('t').value);\n"
            + "      e.preventDefault();\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <form id='f'>\n"
            + "    <input type='text' id='t'/>\n"
            + "  </form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("before\nafter");

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that using Actions to send Keys.ENTER to a form input triggers form submission.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"submit:before"})
    @Ignore
    public void sendKeys_ActionsEnterInFormInputTriggersSubmit() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('f').addEventListener('submit', function(e) {\n"
            + "      log('submit:' + document.getElementById('t').value);\n"
            + "      e.preventDefault();\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <form id='f'>\n"
            + "    <input type='text' id='t'/>\n"
            + "  </form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .moveToElement(t)
                .click()
                .sendKeys("before")
                .sendKeys(Keys.ENTER)
                .sendKeys("after")
                .perform();

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that ENTER in a form input implicitly clicks the form's submit button
     * prior to firing the form submit event.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"btn-clicked", "form-submitted:textValue"})
    public void sendKeys_EnterTriggersSubmitButtonClick() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('sub').addEventListener('click', function(e) {\n"
            + "      log('btn-clicked');\n"
            + "    });\n"
            + "    document.getElementById('f').addEventListener('submit', function(e) {\n"
            + "      log('form-submitted:' + document.getElementById('t').value);\n"
            + "      e.preventDefault();\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <form id='f'>\n"
            + "    <input type='text' id='t'/>\n"
            + "    <input type='submit' id='sub' value='Submit'/>\n"
            + "  </form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("textValue", Keys.ENTER);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that the backspace control character ('\b') embedded in a string
     * removes the preceding character during typing.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "abd",
            FF = "abc\bd",
            FF_ESR = "abc\bd")
    @HtmlUnitNYI(
            FF = "abd",
            FF_ESR = "abd")
    public void sendKeys_BackspaceControlCharacter() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION_NORMALIZE
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("abc\bd");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies Ctrl+A (Select All) using lowercase 'a' in a chord.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("Replaced")
    @BuggyWebDriver(FF = "Initial Text", FF_ESR = "Initial Text")
    public void sendKeys_ControlSelectAllLowercase() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' value='Initial Text'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.chord(Keys.CONTROL, "a"), "Replaced");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies clipboard cut (Ctrl+X) and paste (Ctrl+V) across elements.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"t1:", "t2:CutMe"},
            FF = {"t1:x", "t2:Initial Text"},
            FF_ESR = {"t1:x", "t2:Initial Text"})
    @HtmlUnitNYI(
            FF = {"t1:", "t2:CutMe"},
            FF_ESR = {"t1:", "t2:CutMe"})
    public void sendKeys_ControlCutAndPaste() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log('t1:' + document.getElementById('t1').value);\n"
            + "    log('t2:' + document.getElementById('t2').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t1' value='CutMe'/>\n"
            + "  <input type='text' id='t2'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t1 = driver.findElement(By.id("t1"));
        final WebElement t2 = driver.findElement(By.id("t2"));

        // Select all and Cut from t1
        t1.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.chord(Keys.CONTROL, "x"));
        // Paste into t2
        t2.sendKeys(Keys.chord(Keys.CONTROL, "v"));

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies Pos1 (Keys.HOME) and Keys.END caret movement and text insertion.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("start-middle-end")
    public void sendKeys_Pos1HomeAndEndCaretMovement() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // Type 'middle', move caret to start (Pos1/HOME), type 'start-', move caret to end, type '-end'
        t.sendKeys("middle", Keys.HOME, "start-", Keys.END, "-end");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that Keys.LEFT_SHIFT sets shiftKey=true and location=0 (STANDARD) in sendKeys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "shift:true,location:0",
            FF = "shift:true,location:1",
            FF_ESR = "shift:true,location:1")
    @HtmlUnitNYI(
            FF = "shift:true,location:0",
            FF_ESR = "shift:true,location:0")
    public void sendKeys_LeftShiftLocationAndFlag() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key === 'Shift' || e.keyCode === 16) {\n"
            + "        log('shift:' + e.shiftKey + ',location:' + e.location);\n"
            + "      }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.LEFT_SHIFT);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that Keys.LEFT_CONTROL sets ctrlKey=true and location=0 (STANDARD) in sendKeys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "ctrl:true,location:0",
            FF = "ctrl:true,location:1",
            FF_ESR = "ctrl:true,location:1")
    @HtmlUnitNYI(
            FF = "ctrl:true,location:0",
            FF_ESR = "ctrl:true,location:0")
    public void sendKeys_LeftControlLocationAndFlag() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key === 'Control' || e.keyCode === 17) {\n"
            + "        log('ctrl:' + e.ctrlKey + ',location:' + e.location);\n"
            + "      }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.LEFT_CONTROL);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that Keys.LEFT_ALT sets altKey=true and location=0 (STANDARD) in sendKeys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "alt:true,location:0",
            FF = "alt:true,location:1",
            FF_ESR = "alt:true,location:1")
    @HtmlUnitNYI(
            FF = "alt:true,location:0",
            FF_ESR = "alt:true,location:0")
    public void sendKeys_LeftAltLocationAndFlag() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key === 'Alt' || e.keyCode === 18) {\n"
            + "        log('alt:' + e.altKey + ',location:' + e.location);\n"
            + "      }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.LEFT_ALT);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies Actions API with Keys.LEFT_SHIFT holds the modifier and releases it correctly.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("HellO")
    public void sendKeys_ActionsLeftShiftModifier() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        new Actions(driver)
                .moveToElement(t)
                .click()
                .sendKeys("Hell")
                .keyDown(Keys.LEFT_SHIFT)
                .sendKeys("o")
                .keyUp(Keys.LEFT_SHIFT)
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that Numpad keys insert characters and report e.location = 0 (STANDARD) in sendKeys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"key:1,location:0", "1"},
            FF = {"key:1,location:3", "1"},
            FF_ESR = {"key:1,location:3", "1"})
    @Ignore
    public void sendKeys_NumpadLocationAndValue() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      log('key:' + e.key + ',location:' + e.location);\n"
            + "    });\n"
            + "  }\n"
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.NUMPAD1);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that sending Keys.SPACE to a checkbox toggles its checked state.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"checked:true", "checked:false"})
    public void sendKeys_SpaceTogglesCheckbox() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log('checked:' + document.getElementById('c').checked);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='checkbox' id='c'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement c = driver.findElement(By.id("c"));

        // Press Space to check
        c.sendKeys(Keys.SPACE);
        driver.findElement(By.id("clickMe")).click();

        // Press Space again to uncheck
        c.sendKeys(Keys.SPACE);
        driver.findElement(By.id("clickMe")).click();

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that sending Keys.ESCAPE triggers the DOM keydown event with e.key = 'Escape'.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("key:Escape")
    @Ignore
    public void sendKeys_EscapeKey() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key === 'Escape' || e.keyCode === 27) {\n"
            + "        log('key:' + e.key);\n"
            + "      }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.ESCAPE);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that non-ASCII and Unicode characters type correctly into form fields.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("äöüß€")
    public void sendKeys_UnicodeCharacters() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("äöüß€");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies Shift + Left Arrow selects text and overwrites it on subsequent keypress.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("aX")
    public void sendKeys_ShiftArrowSelectionReplacement() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // Type 'abc', hold Shift + Arrow Left twice to highlight 'bc', type 'X' to replace
        new Actions(driver)
                .moveToElement(t)
                .click()
                .sendKeys("abc")
                .keyDown(Keys.SHIFT)
                .sendKeys(Keys.ARROW_LEFT)
                .sendKeys(Keys.ARROW_LEFT)
                .keyUp(Keys.SHIFT)
                .sendKeys("X")
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that pressing BACK_SPACE when the cursor is already at position 0 (left edge)
     * does not delete any characters or alter the text value.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_BackspaceAtLeftBoundary() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' value='abc'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // Move caret to start and press Backspace
        t.sendKeys(Keys.HOME, Keys.BACK_SPACE);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that pressing DELETE when the cursor is at the end (right edge)
     * does not alter the text value.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_DeleteAtRightBoundary() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // Type 'abc', move to end, press DELETE
        t.sendKeys("abc", Keys.END, Keys.DELETE);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that pressing BACK_SPACE when a selection range is active deletes
     * the entire highlighted selection rather than just a single character.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("ac")
    public void sendKeys_BackspaceWithActiveSelection() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // Type 'abc', move back, hold shift to select 'b', press backspace
        new Actions(driver)
                .moveToElement(t)
                .click()
                .sendKeys("abc")
                .sendKeys(Keys.ARROW_LEFT)
                .keyDown(Keys.SHIFT)
                .sendKeys(Keys.ARROW_LEFT)
                .keyUp(Keys.SHIFT)
                .sendKeys(Keys.BACK_SPACE)
                .perform();

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that sending keys to a readonly input executes without error
     * but does not alter its value.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("fixed")
    @Ignore
    public void sendKeys_ReadonlyInputDoesNotChangeValue() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t1').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t1' value='fixed' readonly/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t1 = driver.findElement(By.id("t1"));

        t1.sendKeys("extra", Keys.BACK_SPACE);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that calling sendKeys on a disabled input throws an ElementNotInteractableException.
     *
     * @throws Exception if the test fails
     */
    @Test(expected = ElementNotInteractableException.class)
    @Ignore
    public void sendKeys_DisabledInputThrowsException() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head></head><body>\n"
            + "  <input type='text' id='t2' value='fixed' disabled/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t2 = driver.findElement(By.id("t2"));

        t2.sendKeys("extra");
    }

    /**
     * Verifies reverse tab navigation using Shift + Tab moves focus backwards,
     * selecting existing text in the targeted input so typing overwrites it.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"t1:modified", "t2:second"},
            FF = {"t1:MODIFIED", "t2:second"},
            FF_ESR = {"t1:MODIFIED", "t2:second"})
    @Ignore
    public void sendKeys_ShiftTabReverseNavigation() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log('t1:' + document.getElementById('t1').value);\n"
            + "    log('t2:' + document.getElementById('t2').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t1' value='first'/>\n"
            + "  <input type='text' id='t2' value='second'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t2 = driver.findElement(By.id("t2"));

        // Focus t2, press Shift+Tab to move back to t1 (selecting its contents), and type
        t2.click();
        t2.sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB), "modified");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies implicit form submission on ENTER when a form contains only a SINGLE
     * text input and NO submit button (HTML standard behavior).
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("submit:singleInputText")
    @Ignore
    public void sendKeys_EnterInSingleInputFormWithoutSubmitButton() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('f').addEventListener('submit', function(e) {\n"
            + "      log('submit:' + document.getElementById('t').value);\n"
            + "      e.preventDefault();\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <form id='f'>\n"
            + "    <input type='text' id='t'/>\n"
            + "  </form>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys("singleInputText", Keys.ENTER);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies typing into a contenteditable element updates innerText / textContent.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("hello")
    public void sendKeys_ContentEditable() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('e').innerText.trim());\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <div id='e' contenteditable='true'></div>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement e = driver.findElement(By.id("e"));

        e.sendKeys("hello");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that non-printable navigation keys (e.g. PageDown) dispatch the correct e.key event.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("key:PageDown")
    @Ignore
    public void sendKeys_PageDownKey() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      log('key:' + e.key);\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.PAGE_DOWN);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that F1–F12 dispatch keydown events with the correct e.key values.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("F1,F2,F5,F12")
    @Ignore
    public void sendKeys_FunctionKeys() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    const keys = [];\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key && e.key.startsWith('F')) { keys.push(e.key); }\n"
            + "      if (keys.length === 4) { log(keys.join(',')); }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.F1, Keys.F2, Keys.F5, Keys.F12);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that Keys.INSERT dispatches a keydown event with e.key = 'Insert'.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("key:Insert")
    @Ignore
    public void sendKeys_InsertKey() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key === 'Insert') { log('key:' + e.key); }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys(Keys.INSERT);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that Keys.RIGHT_SHIFT sets shiftKey=true.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "shift:true,location:0",
            FF = "shift:true,location:2",
            FF_ESR = "shift:true,location:2")
    @HtmlUnitNYI(
            FF = "shift:true,location:0",
            FF_ESR = "shift:true,location:0")
    public void sendKeys_RightShiftLocationAndFlag() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key === 'Shift') {\n"
            + "        log('shift:' + e.shiftKey + ',location:' + e.location);\n"
            + "      }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys(Keys.RIGHT_SHIFT);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that Keys.RIGHT_CONTROL sets ctrlKey=true.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "ctrl:true,location:0",
            FF = "ctrl:true,location:2",
            FF_ESR = "ctrl:true,location:2")
    @Ignore
    public void sendKeys_RightControlLocationAndFlag() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function init() {\n"
            + "    document.getElementById('t').addEventListener('keydown', function(e) {\n"
            + "      if (e.key === 'Control') {\n"
            + "        log('ctrl:' + e.ctrlKey + ',location:' + e.location);\n"
            + "      }\n"
            + "    });\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='init()'>\n"
            + "  <input type='text' id='t'/>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys(Keys.RIGHT_CONTROL);

        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that pressing ARROW_LEFT when the caret is already at position 0
     * is a no-op (does not underflow or error).
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_ArrowLeftAtLeftBoundaryIsNoop() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' value='abc'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.HOME, Keys.ARROW_LEFT, Keys.ARROW_LEFT);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that pressing ARROW_RIGHT when the caret is already at the end
     * is a no-op.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abc")
    public void sendKeys_ArrowRightAtRightBoundaryIsNoop() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t' value='abc'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        t.sendKeys(Keys.END, Keys.ARROW_RIGHT, Keys.ARROW_RIGHT);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that modifier keys are implicitly released between separate
     * element.sendKeys() calls — a second sendKeys must not inherit the
     * modifier state of a prior Actions sequence.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("ABCdef")
    public void sendKeys_ModifierNotLeakedAcrossSendKeysCalls() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        // first sendKeys with SHIFT held for "abc" -> "ABC"
        new Actions(driver)
                .click(t)
                .keyDown(Keys.SHIFT)
                .sendKeys("abc")
                .keyUp(Keys.SHIFT)
                .perform();

        // separate sendKeys call — Shift must NOT still be active
        t.sendKeys("def");

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies that a supplementary-plane Unicode character (emoji, U+1F600)
     * round-trips correctly through sendKeys and appears in the input value.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("a\uD83D\uDE00b")
    public void sendKeys_EmojiSurrogatePair() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log(document.getElementById('t').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t'/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("t")).sendKeys("a\uD83D\uDE00b");   // a😀b

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }
}
