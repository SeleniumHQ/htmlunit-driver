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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.BuggyWebDriver;
import org.openqa.selenium.interactions.Actions;

/**
 * Comprehensive compatibility tests for keyboard interaction pathways.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class KeysTest extends WebDriverTestCase {

    // =========================================================================
    // SECTION 1: Basic Text Input Across All Selenium API Pathways
    // =========================================================================

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

    // =========================================================================
    // SECTION 2: Special Editing and Navigation Keys
    // =========================================================================

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

    // =========================================================================
    // SECTION 3: Modifiers and Chords (SHIFT, CONTROL, ALT)
    // =========================================================================

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
    @Alerts({"alt:true", "alt:false"})
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

    // =========================================================================
    // SECTION 4: Form Submission via ENTER / Newline in Form Inputs
    // =========================================================================

    /**
     * Verifies that sending Keys.ENTER to a form text input triggers form submission
     * with the input value present up to the ENTER key.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"submit:before"})
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
    @Alerts({"submit:before"})
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
 // =========================================================================
    // SECTION 5: Character Control Sequences, Clipboard & Caret Navigation
    // =========================================================================

    /**
     * Verifies that the backspace control character ('\b') embedded in a string
     * removes the preceding character during typing.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("abd")
    public void sendKeys_BackspaceControlCharacter() throws Exception {
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
    @Alerts({"t1:", "t2:CutMe"})
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

    // =========================================================================
    // SECTION 6: Left / Right Specific Modifiers & Event Locations
    // =========================================================================

    /**
     * Verifies that Keys.LEFT_SHIFT sets shiftKey=true and location=0 (STANDARD) in sendKeys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("shift:true,location:0")
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
    @Alerts("ctrl:true,location:0")
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
    @Alerts("alt:true,location:0")
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

    // =========================================================================
    // SECTION 7: Numpad, Space, Escape, Unicode & Selection Modifiers
    // =========================================================================

    /**
     * Verifies that Numpad keys insert characters and report e.location = 3 (NUMPAD).
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"key:1,location:3", "1"})
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

 // =========================================================================
    // SECTION 8: Boundary & Edge Cases (Caret Limits, Readonly, Selection, Shift-Tab)
    // =========================================================================

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
     * Verifies that input into 'readonly' and 'disabled' fields is ignored by sendKeys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"readOnly:fixed", "disabled:fixed"})
    public void sendKeys_ReadonlyAndDisabledInputs() throws Exception {
        final String html =
            STANDARDS_MODE_PREFIX_
            + "<html><head>\n"
            + "<script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    log('readOnly:' + document.getElementById('t1').value);\n"
            + "    log('disabled:' + document.getElementById('t2').value);\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body>\n"
            + "  <input type='text' id='t1' value='fixed' readonly/>\n"
            + "  <input type='text' id='t2' value='fixed' disabled/>\n"
            + "  <button id='clickMe' onclick='test()'>do it</button>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t1 = driver.findElement(By.id("t1"));
        final WebElement t2 = driver.findElement(By.id("t2"));

        t1.sendKeys("extra", Keys.BACK_SPACE);
        t2.sendKeys("extra", Keys.BACK_SPACE);

        driver.findElement(By.id("clickMe")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }

    /**
     * Verifies reverse tab navigation using Shift + Tab moves focus backwards.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"t1:firstmodified", "t2:second"})
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

        // Focus t2, press Shift+Tab to move back to t1, and type
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
}
