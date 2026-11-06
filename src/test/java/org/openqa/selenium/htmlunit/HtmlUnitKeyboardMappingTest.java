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
import static org.junit.Assert.assertTrue;

import org.htmlunit.html.Keyboard;
import org.htmlunit.javascript.host.event.KeyboardEvent;
import org.junit.Test;
import org.openqa.selenium.Keys;

/**
 * Tests for {@link HtmlUnitKeyboardMapping}.
 *
 * @author Ronald Brill
 */
public class HtmlUnitKeyboardMappingTest {

    /**
     * The key codes of the modifier keys.
     */
    @Test
    public void modifierKeyCodes() {
        assertEquals(KeyboardEvent.DOM_VK_SHIFT, HtmlUnitKeyboardMapping.getKeysMapping(Keys.SHIFT.charAt(0)));
        assertEquals(KeyboardEvent.DOM_VK_SHIFT, HtmlUnitKeyboardMapping.getKeysMapping(Keys.RIGHT_SHIFT.charAt(0)));
        assertEquals(KeyboardEvent.DOM_VK_CONTROL, HtmlUnitKeyboardMapping.getKeysMapping(Keys.CONTROL.charAt(0)));
        assertEquals(KeyboardEvent.DOM_VK_CONTROL,
                HtmlUnitKeyboardMapping.getKeysMapping(Keys.RIGHT_CONTROL.charAt(0)));
        assertEquals(KeyboardEvent.DOM_VK_ALT, HtmlUnitKeyboardMapping.getKeysMapping(Keys.ALT.charAt(0)));
        assertEquals(KeyboardEvent.DOM_VK_ALT, HtmlUnitKeyboardMapping.getKeysMapping(Keys.RIGHT_ALT.charAt(0)));
        assertEquals(KeyboardEvent.DOM_VK_META, HtmlUnitKeyboardMapping.getKeysMapping(Keys.META.charAt(0)));
        assertEquals(KeyboardEvent.DOM_VK_META, HtmlUnitKeyboardMapping.getKeysMapping(Keys.COMMAND.charAt(0)));
        assertEquals(KeyboardEvent.DOM_VK_META,
                HtmlUnitKeyboardMapping.getKeysMapping(Keys.RIGHT_COMMAND.charAt(0)));
    }

    /**
     * Keys without an HtmlUnit equivalent are not mapped.
     */
    @Test
    public void unmappedKey() {
        assertTrue(HtmlUnitKeyboardMapping.isSpecialKey(Keys.ZENKAKU_HANKAKU.charAt(0)));
        assertEquals(0, HtmlUnitKeyboardMapping.getKeysMapping(Keys.ZENKAKU_HANKAKU.charAt(0)));
    }

    /**
     * Only Shift, Control, Alt and Meta are modifiers.
     */
    @Test
    public void isModifier() {
        for (final Keys key : new Keys[] {Keys.SHIFT, Keys.RIGHT_SHIFT, Keys.CONTROL, Keys.RIGHT_CONTROL,
                                          Keys.ALT, Keys.RIGHT_ALT, Keys.META, Keys.COMMAND, Keys.RIGHT_COMMAND}) {
            assertTrue(key.name(), HtmlUnitKeyboardMapping.isModifier(key.charAt(0)));
        }
        for (final Keys key : new Keys[] {Keys.ARROW_LEFT, Keys.ENTER, Keys.TAB, Keys.F1, Keys.NUMPAD1,
                                          Keys.ZENKAKU_HANKAKU}) {
            assertFalse(key.name(), HtmlUnitKeyboardMapping.isModifier(key.charAt(0)));
        }
    }

    /**
     * Only the right-hand modifiers are located on the right side.
     */
    @Test
    public void location() {
        for (final Keys key : new Keys[] {Keys.RIGHT_SHIFT, Keys.RIGHT_CONTROL, Keys.RIGHT_ALT, Keys.RIGHT_COMMAND}) {
            assertEquals(key.name(), KeyboardEvent.DOM_KEY_LOCATION_RIGHT,
                    HtmlUnitKeyboardMapping.getLocation(key.charAt(0)));
        }
        for (final Keys key : new Keys[] {Keys.SHIFT, Keys.CONTROL, Keys.ALT, Keys.META, Keys.ARROW_LEFT,
                                          Keys.NUMPAD1}) {
            assertEquals(key.name(), Keyboard.DEFAULT_LOCATION, HtmlUnitKeyboardMapping.getLocation(key.charAt(0)));
        }
    }
}
