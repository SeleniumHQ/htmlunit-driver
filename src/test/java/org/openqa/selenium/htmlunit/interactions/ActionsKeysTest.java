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

import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.HtmlUnitNYI;
import org.openqa.selenium.interactions.Actions;

/**
 * Tests for the key events fired for the modifier keys and for the Actions
 * keyDown()/keyUp() of the HtmlUnitDriver keyboard. The same tests are part
 * of the HtmlUnit KeyboardEventTest.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class ActionsKeysTest extends WebDriverTestCase {

    /**
     * Logs the key events for the right modifier keys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"keydown:Shift,ShiftLeft,16,0,true,false,false,false",
                       "keydown:A,KeyA,65,0,true,false,false,false", "keypress:A,KeyA,65,0,true,false,false,false",
                       "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                       "keyup:Shift,ShiftLeft,16,0,false,false,false,false",
                       "keydown:Control,ControlLeft,17,0,false,true,false,false",
                       "keydown:Home,Home,36,0,false,true,false,false", "keyup:Home,Home,36,0,false,true,false,false",
                       "keyup:Control,ControlLeft,17,0,false,false,false,false",
                       "keydown:Alt,AltLeft,18,0,false,false,true,false",
                       "keyup:Alt,AltLeft,18,0,false,false,false,false"},
            FF = {"keydown:Shift,ShiftRight,16,2,true,false,false,false",
                  "keyup:Shift,ShiftRight,16,2,false,false,false,false", "keydown:A,KeyA,65,0,true,false,false,false",
                  "keypress:A,KeyA,65,0,true,false,false,false", "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                  "keydown:Unidentified,,0,0,true,false,false,false", "keyup:Unidentified,,0,0,true,false,false,false",
                  "keydown:Control,ControlRight,17,2,true,true,false,false",
                  "keyup:Control,ControlRight,17,2,true,false,false,false",
                  "keydown:Home,Home,36,0,true,true,false,false", "keyup:Home,Home,36,0,true,true,false,false",
                  "keydown:Unidentified,,0,0,true,true,false,false", "keyup:Unidentified,,0,0,true,true,false,false",
                  "keydown:Alt,AltRight,18,2,true,true,true,false", "keyup:Alt,AltRight,18,2,true,true,false,false",
                  "keydown:Unidentified,,0,0,true,true,true,false", "keyup:Unidentified,,0,0,true,true,true,false"},
            FF_ESR = {"keydown:Shift,ShiftRight,16,2,true,false,false,false",
                      "keyup:Shift,ShiftRight,16,2,false,false,false,false",
                      "keydown:A,KeyA,65,0,true,false,false,false", "keypress:A,KeyA,65,0,true,false,false,false",
                      "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                      "keydown:Unidentified,,0,0,true,false,false,false",
                      "keyup:Unidentified,,0,0,true,false,false,false",
                      "keydown:Control,ControlRight,17,2,true,true,false,false",
                      "keyup:Control,ControlRight,17,2,true,false,false,false",
                      "keydown:Home,Home,36,0,true,true,false,false", "keyup:Home,Home,36,0,true,true,false,false",
                      "keydown:Unidentified,,0,0,true,true,false,false",
                      "keyup:Unidentified,,0,0,true,true,false,false",
                      "keydown:Alt,AltRight,18,2,true,true,true,false",
                      "keyup:Alt,AltRight,18,2,true,true,false,false",
                      "keydown:Unidentified,,0,0,true,true,true,false", "keyup:Unidentified,,0,0,true,true,true,false"})
    // chromedriver sends the left keys without location for the right modifier keys
    // geckodriver releases the modifiers immediately and sends additional unidentified keys
    @HtmlUnitNYI(CHROME = {"keydown:Shift,ShiftRight,16,2,true,false,false,false",
                           "keydown:A,KeyA,65,0,true,false,false,false", "keypress:A,KeyA,65,0,true,false,false,false",
                           "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                           "keyup:Shift,ShiftRight,16,2,false,false,false,false",
                           "keydown:Control,ControlRight,17,2,false,true,false,false",
                           "keydown:Home,Home,36,0,false,true,false,false",
                           "keyup:Home,Home,36,0,false,true,false,false",
                           "keyup:Control,ControlRight,17,2,false,false,false,false",
                           "keydown:Alt,AltRight,18,2,false,false,true,false",
                           "keyup:Alt,AltRight,18,2,false,false,false,false"},
            EDGE = {"keydown:Shift,ShiftRight,16,2,true,false,false,false",
                    "keydown:A,KeyA,65,0,true,false,false,false", "keypress:A,KeyA,65,0,true,false,false,false",
                    "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                    "keyup:Shift,ShiftRight,16,2,false,false,false,false",
                    "keydown:Control,ControlRight,17,2,false,true,false,false",
                    "keydown:Home,Home,36,0,false,true,false,false", "keyup:Home,Home,36,0,false,true,false,false",
                    "keyup:Control,ControlRight,17,2,false,false,false,false",
                    "keydown:Alt,AltRight,18,2,false,false,true,false",
                    "keyup:Alt,AltRight,18,2,false,false,false,false"},
            FF = {"keydown:Shift,ShiftRight,16,2,true,false,false,false", "keydown:A,KeyA,65,0,true,false,false,false",
                  "keypress:A,KeyA,65,0,true,false,false,false", "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                  "keyup:Shift,ShiftRight,16,2,false,false,false,false",
                  "keydown:Control,ControlRight,17,2,false,true,false,false",
                  "keydown:Home,Home,36,0,false,true,false,false", "keyup:Home,Home,36,0,false,true,false,false",
                  "keyup:Control,ControlRight,17,2,false,false,false,false",
                  "keydown:Alt,AltRight,18,2,false,false,true,false",
                  "keyup:Alt,AltRight,18,2,false,false,false,false"},
            FF_ESR = {"keydown:Shift,ShiftRight,16,2,true,false,false,false",
                      "keydown:A,KeyA,65,0,true,false,false,false", "keypress:A,KeyA,65,0,true,false,false,false",
                      "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                      "keyup:Shift,ShiftRight,16,2,false,false,false,false",
                      "keydown:Control,ControlRight,17,2,false,true,false,false",
                      "keydown:Home,Home,36,0,false,true,false,false", "keyup:Home,Home,36,0,false,true,false,false",
                      "keyup:Control,ControlRight,17,2,false,false,false,false",
                      "keydown:Alt,AltRight,18,2,false,false,true,false",
                      "keyup:Alt,AltRight,18,2,false,false,false,false"})
    public void sendKeys_RightModifiers() throws Exception {
        sendKeysEvents(Keys.RIGHT_SHIFT, "a", Keys.NULL,
                Keys.RIGHT_CONTROL, Keys.HOME, Keys.NULL,
                Keys.RIGHT_ALT, Keys.NULL);
    }

    /**
     * Logs the key events for modifiers pressed in reverse order and released at the end of sendKeys.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"keydown:Meta,MetaLeft,91,0,false,false,false,true",
                       "keydown:Alt,AltLeft,18,0,false,false,true,true",
                       "keydown:Control,ControlLeft,17,0,false,true,true,true",
                       "keydown:Shift,ShiftLeft,16,0,true,true,true,true", "keydown:A,KeyA,65,0,true,true,true,true",
                       "keyup:A,KeyA,65,0,true,true,true,true", "keyup:Shift,ShiftLeft,16,0,false,false,false,false",
                       "keyup:Control,ControlLeft,17,0,false,false,false,false",
                       "keyup:Alt,AltLeft,18,0,false,false,false,false",
                       "keyup:Meta,MetaLeft,91,0,false,false,false,false"},
            FF = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                  "keyup:Meta,MetaLeft,91,1,false,false,false,false", "keydown:Alt,AltLeft,18,1,false,false,true,true",
                  "keyup:Alt,AltLeft,18,1,false,false,false,true",
                  "keydown:Control,ControlLeft,17,1,false,true,true,true",
                  "keyup:Control,ControlLeft,17,1,false,false,true,true",
                  "keydown:Shift,ShiftLeft,16,1,true,true,true,true",
                  "keyup:Shift,ShiftLeft,16,1,false,true,true,true", "keydown:A,KeyA,65,0,true,true,true,true",
                  "keyup:A,KeyA,65,0,true,true,true,true"},
            FF_ESR = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                      "keyup:Meta,MetaLeft,91,1,false,false,false,false",
                      "keydown:Alt,AltLeft,18,1,false,false,true,true",
                      "keyup:Alt,AltLeft,18,1,false,false,false,true",
                      "keydown:Control,ControlLeft,17,1,false,true,true,true",
                      "keyup:Control,ControlLeft,17,1,false,false,true,true",
                      "keydown:Shift,ShiftLeft,16,1,true,true,true,true",
                      "keyup:Shift,ShiftLeft,16,1,false,true,true,true", "keydown:A,KeyA,65,0,true,true,true,true",
                      "keyup:A,KeyA,65,0,true,true,true,true"})
    // chromedriver does not set the location of the key events
    // chromedriver clears all modifier flags before the keyup events at the end of sendKeys
    // geckodriver releases the modifiers immediately and sends additional unidentified keys
    @HtmlUnitNYI(CHROME = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                           "keydown:Alt,AltLeft,18,1,false,false,true,true",
                           "keydown:Control,ControlLeft,17,1,false,true,true,true",
                           "keydown:Shift,ShiftLeft,16,1,true,true,true,true",
                           "keydown:A,KeyA,65,0,true,true,true,true", "keyup:A,KeyA,65,0,true,true,true,true",
                           "keyup:Shift,ShiftLeft,16,1,false,true,true,true",
                           "keyup:Control,ControlLeft,17,1,false,false,true,true",
                           "keyup:Alt,AltLeft,18,1,false,false,false,true",
                           "keyup:Meta,MetaLeft,91,1,false,false,false,false"},
            EDGE = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                    "keydown:Alt,AltLeft,18,1,false,false,true,true",
                    "keydown:Control,ControlLeft,17,1,false,true,true,true",
                    "keydown:Shift,ShiftLeft,16,1,true,true,true,true", "keydown:A,KeyA,65,0,true,true,true,true",
                    "keyup:A,KeyA,65,0,true,true,true,true", "keyup:Shift,ShiftLeft,16,1,false,true,true,true",
                    "keyup:Control,ControlLeft,17,1,false,false,true,true",
                    "keyup:Alt,AltLeft,18,1,false,false,false,true",
                    "keyup:Meta,MetaLeft,91,1,false,false,false,false"},
            FF = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                  "keydown:Alt,AltLeft,18,1,false,false,true,true",
                  "keydown:Control,ControlLeft,17,1,false,true,true,true",
                  "keydown:Shift,ShiftLeft,16,1,true,true,true,true", "keydown:A,KeyA,65,0,true,true,true,true",
                  "keyup:A,KeyA,65,0,true,true,true,true", "keyup:Shift,ShiftLeft,16,1,false,true,true,true",
                  "keyup:Control,ControlLeft,17,1,false,false,true,true",
                  "keyup:Alt,AltLeft,18,1,false,false,false,true", "keyup:Meta,MetaLeft,91,1,false,false,false,false"},
            FF_ESR = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                      "keydown:Alt,AltLeft,18,1,false,false,true,true",
                      "keydown:Control,ControlLeft,17,1,false,true,true,true",
                      "keydown:Shift,ShiftLeft,16,1,true,true,true,true", "keydown:A,KeyA,65,0,true,true,true,true",
                      "keyup:A,KeyA,65,0,true,true,true,true", "keyup:Shift,ShiftLeft,16,1,false,true,true,true",
                      "keyup:Control,ControlLeft,17,1,false,false,true,true",
                      "keyup:Alt,AltLeft,18,1,false,false,false,true",
                      "keyup:Meta,MetaLeft,91,1,false,false,false,false"})
    public void sendKeys_ModifiersReleaseOrder() throws Exception {
        sendKeysEvents(Keys.META, Keys.ALT, Keys.CONTROL, Keys.SHIFT, "a");
    }

    /**
     * Logs the key events for modifiers released by Keys.NULL.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"keydown:Control,ControlLeft,17,0,false,true,false,false",
                       "keydown:Shift,ShiftLeft,16,0,true,true,false,false",
                       "keydown:A,KeyA,65,0,true,true,false,false", "keyup:A,KeyA,65,0,true,true,false,false",
                       "keyup:Shift,ShiftLeft,16,0,false,false,false,false",
                       "keyup:Control,ControlLeft,17,0,false,false,false,false",
                       "keydown:b,KeyB,66,0,false,false,false,false", "keypress:b,KeyB,98,0,false,false,false,false",
                       "input:b", "keyup:b,KeyB,66,0,false,false,false,false"},
            FF = {"keydown:Control,ControlLeft,17,1,false,true,false,false",
                  "keyup:Control,ControlLeft,17,1,false,false,false,false",
                  "keydown:Shift,ShiftLeft,16,1,true,true,false,false",
                  "keyup:Shift,ShiftLeft,16,1,false,true,false,false", "keydown:A,KeyA,65,0,true,true,false,false",
                  "keyup:A,KeyA,65,0,true,true,false,false", "keydown:Unidentified,,0,0,true,true,false,false",
                  "keyup:Unidentified,,0,0,true,true,false,false", "keydown:B,KeyB,66,0,true,true,false,false",
                  "keyup:B,KeyB,66,0,true,true,false,false"},
            FF_ESR = {"keydown:Control,ControlLeft,17,1,false,true,false,false",
                      "keyup:Control,ControlLeft,17,1,false,false,false,false",
                      "keydown:Shift,ShiftLeft,16,1,true,true,false,false",
                      "keyup:Shift,ShiftLeft,16,1,false,true,false,false", "keydown:A,KeyA,65,0,true,true,false,false",
                      "keyup:A,KeyA,65,0,true,true,false,false", "keydown:Unidentified,,0,0,true,true,false,false",
                      "keyup:Unidentified,,0,0,true,true,false,false", "keydown:B,KeyB,66,0,true,true,false,false",
                      "keyup:B,KeyB,66,0,true,true,false,false"})
    // chromedriver does not set the location of the key events
    // chromedriver clears all modifier flags before the keyup events at the end of sendKeys
    // geckodriver releases the modifiers immediately and sends additional unidentified keys
    @HtmlUnitNYI(CHROME = {"keydown:Control,ControlLeft,17,1,false,true,false,false",
                           "keydown:Shift,ShiftLeft,16,1,true,true,false,false",
                           "keydown:A,KeyA,65,0,true,true,false,false", "keyup:A,KeyA,65,0,true,true,false,false",
                           "keyup:Shift,ShiftLeft,16,1,false,true,false,false",
                           "keyup:Control,ControlLeft,17,1,false,false,false,false",
                           "keydown:b,KeyB,66,0,false,false,false,false",
                           "keypress:b,KeyB,98,0,false,false,false,false", "input:b",
                           "keyup:b,KeyB,66,0,false,false,false,false"},
            EDGE = {"keydown:Control,ControlLeft,17,1,false,true,false,false",
                    "keydown:Shift,ShiftLeft,16,1,true,true,false,false", "keydown:A,KeyA,65,0,true,true,false,false",
                    "keyup:A,KeyA,65,0,true,true,false,false", "keyup:Shift,ShiftLeft,16,1,false,true,false,false",
                    "keyup:Control,ControlLeft,17,1,false,false,false,false",
                    "keydown:b,KeyB,66,0,false,false,false,false", "keypress:b,KeyB,98,0,false,false,false,false",
                    "input:b", "keyup:b,KeyB,66,0,false,false,false,false"},
            FF = {"keydown:Control,ControlLeft,17,1,false,true,false,false",
                  "keydown:Shift,ShiftLeft,16,1,true,true,false,false", "keydown:A,KeyA,65,0,true,true,false,false",
                  "keyup:A,KeyA,65,0,true,true,false,false", "keyup:Shift,ShiftLeft,16,1,false,true,false,false",
                  "keyup:Control,ControlLeft,17,1,false,false,false,false",
                  "keydown:b,KeyB,66,0,false,false,false,false", "keypress:b,KeyB,98,0,false,false,false,false",
                  "input:b", "keyup:b,KeyB,66,0,false,false,false,false"},
            FF_ESR = {"keydown:Control,ControlLeft,17,1,false,true,false,false",
                      "keydown:Shift,ShiftLeft,16,1,true,true,false,false",
                      "keydown:A,KeyA,65,0,true,true,false,false", "keyup:A,KeyA,65,0,true,true,false,false",
                      "keyup:Shift,ShiftLeft,16,1,false,true,false,false",
                      "keyup:Control,ControlLeft,17,1,false,false,false,false",
                      "keydown:b,KeyB,66,0,false,false,false,false", "keypress:b,KeyB,98,0,false,false,false,false",
                      "input:b", "keyup:b,KeyB,66,0,false,false,false,false"})
    public void sendKeys_ModifiersNull() throws Exception {
        sendKeysEvents(Keys.CONTROL, Keys.SHIFT, "a", Keys.NULL, "b");
    }

    /**
     * Logs the key events for the meta key pressed together with a char.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"keydown:Meta,MetaLeft,91,0,false,false,false,true",
                       "keydown:a,KeyA,65,0,false,false,false,true", "keypress:a,KeyA,97,0,false,false,false,true",
                       "input:a", "keyup:a,KeyA,65,0,false,false,false,true",
                       "keyup:Meta,MetaLeft,91,0,false,false,false,false"},
            FF = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                  "keyup:Meta,MetaLeft,91,1,false,false,false,false", "keydown:a,KeyA,65,0,false,false,false,true",
                  "keyup:a,KeyA,65,0,false,false,false,true", "keydown:Unidentified,,0,0,false,false,false,true",
                  "keyup:Unidentified,,0,0,false,false,false,true"},
            FF_ESR = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                      "keyup:Meta,MetaLeft,91,1,false,false,false,false", "keydown:a,KeyA,65,0,false,false,false,true",
                      "keyup:a,KeyA,65,0,false,false,false,true", "keydown:Unidentified,,0,0,false,false,false,true",
                      "keyup:Unidentified,,0,0,false,false,false,true"})
    // chromedriver does not set the location of the key events
    // geckodriver releases the modifiers immediately and sends additional unidentified keys
    @HtmlUnitNYI(CHROME = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                           "keydown:a,KeyA,65,0,false,false,false,true", "keypress:a,KeyA,97,0,false,false,false,true",
                           "input:a", "keyup:a,KeyA,65,0,false,false,false,true",
                           "keyup:Meta,MetaLeft,91,1,false,false,false,false"},
            EDGE = {"keydown:Meta,MetaLeft,91,1,false,false,false,true", "keydown:a,KeyA,65,0,false,false,false,true",
                    "keypress:a,KeyA,97,0,false,false,false,true", "input:a",
                    "keyup:a,KeyA,65,0,false,false,false,true", "keyup:Meta,MetaLeft,91,1,false,false,false,false"},
            FF = {"keydown:Meta,MetaLeft,91,1,false,false,false,true", "keydown:a,KeyA,65,0,false,false,false,true",
                  "keyup:a,KeyA,65,0,false,false,false,true", "keyup:Meta,MetaLeft,91,1,false,false,false,false"},
            FF_ESR = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                      "keydown:a,KeyA,65,0,false,false,false,true", "keyup:a,KeyA,65,0,false,false,false,true",
                      "keyup:Meta,MetaLeft,91,1,false,false,false,false"})
    public void sendKeys_MetaKey() throws Exception {
        sendKeysEvents(Keys.chord(Keys.META, "a"));
    }

    /**
     * Logs the key events for interleaved key down and key up of chars.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"keydown:a,KeyA,65,0,false,false,false,false", "keypress:a,KeyA,97,0,false,false,false,false", "input:a",
             "keydown:b,KeyB,66,0,false,false,false,false", "keypress:b,KeyB,98,0,false,false,false,false", "input:ab",
             "keyup:a,KeyA,65,0,false,false,false,false", "keyup:b,KeyB,66,0,false,false,false,false"})
    public void actions_KeyDownUpChars() throws Exception {
        actionsEvents(a -> a.keyDown("a").keyDown("b").keyUp("a").keyUp("b"));
    }

    /**
     * Logs the key events for a special key held down while typing a char.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"keydown:ArrowLeft,ArrowLeft,37,0,false,false,false,false", "keydown:x,KeyX,88,0,false,false,false,false",
             "keypress:x,KeyX,120,0,false,false,false,false", "input:x", "keyup:x,KeyX,88,0,false,false,false,false",
             "keyup:ArrowLeft,ArrowLeft,37,0,false,false,false,false"})
    public void actions_KeyDownUpSpecialKey() throws Exception {
        actionsEvents(a -> a.keyDown(Keys.ARROW_LEFT).sendKeys("x").keyUp(Keys.ARROW_LEFT));
    }

    /**
     * Logs the key events for key down and key up of the same special key.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"keydown:ArrowLeft,ArrowLeft,37,0,false,false,false,false",
             "keyup:ArrowLeft,ArrowLeft,37,0,false,false,false,false"})
    public void actions_KeyDownUpSameKey() throws Exception {
        actionsEvents(a -> a.keyDown(Keys.ARROW_LEFT).keyUp(Keys.ARROW_LEFT));
    }

    /**
     * Logs the key events for shift held down while typing.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"keydown:Shift,ShiftLeft,16,1,true,false,false,false", "keydown:A,KeyA,65,0,true,false,false,false",
             "keypress:A,KeyA,65,0,true,false,false,false", "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
             "keydown:B,KeyB,66,0,true,false,false,false", "keypress:B,KeyB,66,0,true,false,false,false", "input:AB",
             "keyup:B,KeyB,66,0,true,false,false,false", "keyup:Shift,ShiftLeft,16,1,false,false,false,false"})
    public void actions_ShiftSendKeys() throws Exception {
        actionsEvents(a -> a.keyDown(Keys.SHIFT).sendKeys("ab").keyUp(Keys.SHIFT));
    }

    /**
     * Logs the key events for meta held down while typing.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                       "keydown:a,KeyA,65,0,false,false,false,true", "keypress:a,KeyA,97,0,false,false,false,true",
                       "input:a", "keyup:a,KeyA,65,0,false,false,false,true",
                       "keyup:Meta,MetaLeft,91,1,false,false,false,false"},
            FF = {"keydown:Meta,MetaLeft,91,1,false,false,false,true", "keydown:a,KeyA,65,0,false,false,false,true",
                  "keyup:a,KeyA,65,0,false,false,false,true", "keyup:Meta,MetaLeft,91,1,false,false,false,false"},
            FF_ESR = {"keydown:Meta,MetaLeft,91,1,false,false,false,true",
                      "keydown:a,KeyA,65,0,false,false,false,true", "keyup:a,KeyA,65,0,false,false,false,true",
                      "keyup:Meta,MetaLeft,91,1,false,false,false,false"})
    public void actions_MetaSendKeys() throws Exception {
        actionsEvents(a -> a.keyDown(Keys.META).sendKeys("a").keyUp(Keys.META));
    }

    /**
     * Logs the key events for the right shift key held down while typing.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"keydown:Shift,ShiftRight,161,2,true,false,false,false",
                       "keydown:A,KeyA,65,0,true,false,false,false", "keypress:A,KeyA,65,0,true,false,false,false",
                       "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                       "keyup:Shift,ShiftRight,161,2,false,false,false,false"},
            FF = {"keydown:Shift,ShiftRight,16,2,true,false,false,false", "keydown:A,KeyA,65,0,true,false,false,false",
                  "keypress:A,KeyA,65,0,true,false,false,false", "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                  "keyup:Shift,ShiftRight,16,2,false,false,false,false"},
            FF_ESR = {"keydown:Shift,ShiftRight,16,2,true,false,false,false",
                      "keydown:A,KeyA,65,0,true,false,false,false", "keypress:A,KeyA,65,0,true,false,false,false",
                      "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                      "keyup:Shift,ShiftRight,16,2,false,false,false,false"})
    // chromedriver reports the windows key code 161 (VK_RSHIFT) for the right shift key
    @HtmlUnitNYI(CHROME = {"keydown:Shift,ShiftRight,16,2,true,false,false,false",
                           "keydown:A,KeyA,65,0,true,false,false,false", "keypress:A,KeyA,65,0,true,false,false,false",
                           "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                           "keyup:Shift,ShiftRight,16,2,false,false,false,false"},
            EDGE = {"keydown:Shift,ShiftRight,16,2,true,false,false,false",
                    "keydown:A,KeyA,65,0,true,false,false,false", "keypress:A,KeyA,65,0,true,false,false,false",
                    "input:A", "keyup:A,KeyA,65,0,true,false,false,false",
                    "keyup:Shift,ShiftRight,16,2,false,false,false,false"})
    public void actions_RightShiftSendKeys() throws Exception {
        actionsEvents(a -> a.keyDown(Keys.RIGHT_SHIFT).sendKeys("a").keyUp(Keys.RIGHT_SHIFT));
    }

    /**
     * Logs the key events for modifiers pressed by one perform() and released by the next one.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"keydown:Shift,ShiftLeft,16,1,true,false,false,false",
             "keydown:Control,ControlLeft,17,1,true,true,false,false", "keydown:A,KeyA,65,0,true,true,false,false",
             "keyup:A,KeyA,65,0,true,true,false,false", "keyup:Shift,ShiftLeft,16,1,false,true,false,false",
             "keyup:Control,ControlLeft,17,1,false,false,false,false"})
    public void actions_ModifiersNotReleased() throws Exception {
        actionsEvents(a -> a.keyDown(Keys.SHIFT).keyDown(Keys.CONTROL),
                a -> a.sendKeys("a").keyUp(Keys.SHIFT).keyUp(Keys.CONTROL));
    }

    private static final String LOG_KEY_EVENTS = "<input type='text' id='t'>\n"
            + LOG_TEXTAREA
            + "<script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "function handler(e) {\n"
            + "  var msg = e.type + ':' + e.target.value;\n"
            + "  if (e.type != 'input') {\n"
            + "    msg = e.type + ':' + e.key + ',' + e.code + ',' + e.keyCode + ',' + e.location + ',' "
                            + "+ e.shiftKey + ',' + e.ctrlKey + ',' + e.altKey + ',' + e.metaKey;\n"
            + "  }\n"
            + "  log(msg);\n"
            + "}\n"
            + "var t = document.getElementById('t');\n"
            + "t.addEventListener('keydown', handler);\n"
            + "t.addEventListener('keypress', handler);\n"
            + "t.addEventListener('input', handler);\n"
            + "t.addEventListener('keyup', handler);\n"
            + "</script>\n";

    private void sendKeysEvents(final CharSequence... keys) throws Exception {
        final WebDriver driver = loadPage2("<!DOCTYPE html>\n<html><head></head><body>\n"
                + LOG_KEY_EVENTS + "</body></html>");
        driver.findElement(By.id("t")).sendKeys(keys);

        verifyLog(driver);
    }

    @SafeVarargs
    private void actionsEvents(final Consumer<Actions>... performs) throws Exception {
        final WebDriver driver = loadPage2("<!DOCTYPE html>\n<html><head></head><body>\n"
                + LOG_KEY_EVENTS + "</body></html>");
        driver.findElement(By.id("t")).click();
        for (final Consumer<Actions> perform : performs) {
            final Actions actions = new Actions(driver);
            perform.accept(actions);
            actions.perform();
        }

        verifyLog(driver);
    }

    private void verifyLog(final WebDriver driver) throws Exception {
        final StringBuilder expected = new StringBuilder();
        for (final String alert : getExpectedAlerts()) {
            expected.append(alert).append('\u00A7');
        }
        assertEquals(expected.toString(), driver.findElement(By.id("myLog")).getDomProperty("value"));
    }
}
