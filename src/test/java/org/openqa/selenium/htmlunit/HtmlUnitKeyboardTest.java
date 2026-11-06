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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Tests for the modifier state of {@link HtmlUnitKeyboard}.
 *
 * @author Ronald Brill
 */
public class HtmlUnitKeyboardTest {

    private static final String PAGE = "data:text/html,<html><body>"
            + "<input id='t'>"
            + "<script>"
            + "document.getElementById('t').addEventListener('keydown', function(e) {"
            + "  if (e.key == 'X') { throw new Error('failed'); }"
            + "});"
            + "</script></body></html>";

    /**
     * The modifiers pressed by sendKeys are released at the end.
     */
    @Test
    public void sendKeysReleasesModifiers() {
        final HtmlUnitDriver driver = new HtmlUnitDriver(true);
        try {
            driver.get(PAGE);
            final WebElement input = driver.findElement(By.id("t"));
            input.sendKeys(Keys.SHIFT, Keys.RIGHT_ALT, "a");

            final HtmlUnitKeyboard keyboard = driver.getKeyboard();
            assertFalse(keyboard.isShiftPressed());
            assertFalse(keyboard.isAltPressed());
            assertEquals("A", input.getDomProperty("value"));

            input.sendKeys(Keys.RIGHT_CONTROL, Keys.META);
            assertFalse(keyboard.isCtrlPressed());
            assertFalse(keyboard.isMetaPressed());
        }
        finally {
            driver.quit();
        }
    }

    /**
     * The modifiers pressed by Actions stay pressed until they are released.
     */
    @Test
    public void actionsKeepModifiers() {
        final HtmlUnitDriver driver = new HtmlUnitDriver(true);
        try {
            driver.get(PAGE);
            driver.findElement(By.id("t")).click();

            new Actions(driver).keyDown(Keys.RIGHT_SHIFT).keyDown(Keys.META).perform();
            final HtmlUnitKeyboard keyboard = driver.getKeyboard();
            assertTrue(keyboard.isShiftPressed());
            assertTrue(keyboard.isMetaPressed());

            new Actions(driver).keyUp(Keys.RIGHT_SHIFT).perform();
            assertFalse(keyboard.isShiftPressed());
            assertTrue(keyboard.isMetaPressed());

            new Actions(driver).keyUp(Keys.META).perform();
            assertFalse(keyboard.isMetaPressed());
        }
        finally {
            driver.quit();
        }
    }

    /**
     * A failed dispatch does not change the modifier state.
     */
    @Test
    public void failedDispatchKeepsModifierState() {
        final HtmlUnitDriver driver = new HtmlUnitDriver(true);
        try {
            driver.get(PAGE);
            final WebElement input = driver.findElement(By.id("t"));
            input.click();

            new Actions(driver).keyDown(Keys.SHIFT).perform();
            final HtmlUnitKeyboard keyboard = driver.getKeyboard();
            assertTrue(keyboard.isShiftPressed());

            // the keydown handler throws for the (upper case) 'X'
            assertThrows(RuntimeException.class, () -> input.sendKeys(Keys.CONTROL, "x"));

            // control was not committed, shift was not released
            assertTrue(keyboard.isShiftPressed());
            assertFalse(keyboard.isCtrlPressed());
        }
        finally {
            driver.quit();
        }
    }
}
