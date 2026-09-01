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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.htmlunit.javascript.host.event.KeyboardEvent;
import org.openqa.selenium.Keys;

/**
 * Maps WebDriver {@link Keys} values to their HtmlUnit {@link KeyboardEvent}
 * virtual-key constants ({@code DOM_VK_*}).
 *
 * <p>WebDriver's {@link Keys} enum encodes special keys as characters in the
 * Unicode Private Use Area (PUA), specifically the range {@code \uE000–\uF8FF}.
 * {@link #isSpecialKey(char)} detects characters in this range. Each detected
 * character is looked up in the internal mapping table; if a mapping exists,
 * the corresponding {@code DOM_VK_*} integer constant is returned. If no
 * mapping exists (e.g. {@link Keys#COMMAND} or {@link Keys#ZENKAKU_HANKAKU},
 * which have no HtmlUnit equivalent), {@link #getKeysMapping(char)} returns
 * {@code 0} as a sentinel value and logs a warning. Callers should treat a
 * return value of {@code 0} as "no applicable key code" and skip the
 * press/release.</p>
 *
 * <p>The mapping table is built once at class-load time and is thereafter
 * immutable. This class is a pure utility class; it cannot be instantiated.</p>
 *
 * @author Ahmed Ashour
 * @author Luke Inman-Semerau
 * @author Ronald Brill
 */
public final class HtmlUnitKeyboardMapping {

    private static final Logger LOG = Logger.getLogger(HtmlUnitKeyboardMapping.class.getName());

    /**
     * Immutable mapping from a {@link Keys} character (PUA code point) to its
     * HtmlUnit {@code DOM_VK_*} constant. Built once in the static initialiser
     * and wrapped as unmodifiable to prevent accidental mutation.
     */
    private static final Map<Character, Integer> SPECIAL_KEYS_MAP;

    static {
        final Map<Character, Integer> map = new HashMap<>();

        map.put(Keys.CANCEL.charAt(0),        KeyboardEvent.DOM_VK_CANCEL);
        map.put(Keys.HELP.charAt(0),          KeyboardEvent.DOM_VK_HELP);
        map.put(Keys.BACK_SPACE.charAt(0),    KeyboardEvent.DOM_VK_BACK_SPACE);
        map.put(Keys.TAB.charAt(0),           KeyboardEvent.DOM_VK_TAB);
        map.put(Keys.CLEAR.charAt(0),         KeyboardEvent.DOM_VK_CLEAR);
        map.put(Keys.RETURN.charAt(0),        KeyboardEvent.DOM_VK_RETURN);

        // Keys.ENTER and Keys.RETURN share the same DOM_VK_RETURN constant.
        // If they have distinct code points, both are mapped; if they share one,
        // the second put is a harmless no-op.
        map.put(Keys.ENTER.charAt(0),         KeyboardEvent.DOM_VK_RETURN);

        // Keys.SHIFT and Keys.LEFT_SHIFT both map to DOM_VK_SHIFT.
        map.put(Keys.SHIFT.charAt(0),         KeyboardEvent.DOM_VK_SHIFT);
        map.put(Keys.LEFT_SHIFT.charAt(0),    KeyboardEvent.DOM_VK_SHIFT);

        // Keys.CONTROL and Keys.LEFT_CONTROL both map to DOM_VK_CONTROL.
        map.put(Keys.CONTROL.charAt(0),       KeyboardEvent.DOM_VK_CONTROL);
        map.put(Keys.LEFT_CONTROL.charAt(0),  KeyboardEvent.DOM_VK_CONTROL);

        // Keys.ALT and Keys.LEFT_ALT both map to DOM_VK_ALT.
        map.put(Keys.ALT.charAt(0),           KeyboardEvent.DOM_VK_ALT);
        map.put(Keys.LEFT_ALT.charAt(0),      KeyboardEvent.DOM_VK_ALT);

        map.put(Keys.PAUSE.charAt(0),         KeyboardEvent.DOM_VK_PAUSE);
        map.put(Keys.ESCAPE.charAt(0),        KeyboardEvent.DOM_VK_ESCAPE);
        map.put(Keys.SPACE.charAt(0),         KeyboardEvent.DOM_VK_SPACE);
        map.put(Keys.PAGE_UP.charAt(0),       KeyboardEvent.DOM_VK_PAGE_UP);
        map.put(Keys.PAGE_DOWN.charAt(0),     KeyboardEvent.DOM_VK_PAGE_DOWN);
        map.put(Keys.END.charAt(0),           KeyboardEvent.DOM_VK_END);
        map.put(Keys.HOME.charAt(0),          KeyboardEvent.DOM_VK_HOME);

        // Keys.LEFT and Keys.ARROW_LEFT are aliases; both map to DOM_VK_LEFT.
        map.put(Keys.LEFT.charAt(0),          KeyboardEvent.DOM_VK_LEFT);
        map.put(Keys.ARROW_LEFT.charAt(0),    KeyboardEvent.DOM_VK_LEFT);

        // Keys.UP and Keys.ARROW_UP are aliases; both map to DOM_VK_UP.
        map.put(Keys.UP.charAt(0),            KeyboardEvent.DOM_VK_UP);
        map.put(Keys.ARROW_UP.charAt(0),      KeyboardEvent.DOM_VK_UP);

        // Keys.RIGHT and Keys.ARROW_RIGHT are aliases; both map to DOM_VK_RIGHT.
        map.put(Keys.RIGHT.charAt(0),         KeyboardEvent.DOM_VK_RIGHT);
        map.put(Keys.ARROW_RIGHT.charAt(0),   KeyboardEvent.DOM_VK_RIGHT);

        // Keys.DOWN and Keys.ARROW_DOWN are aliases; both map to DOM_VK_DOWN.
        // NOTE: the original code mistakenly mapped Keys.DOWN to DOM_VK_CLEAR.
        map.put(Keys.DOWN.charAt(0),          KeyboardEvent.DOM_VK_DOWN);
        map.put(Keys.ARROW_DOWN.charAt(0),    KeyboardEvent.DOM_VK_DOWN);

        map.put(Keys.INSERT.charAt(0),        KeyboardEvent.DOM_VK_INSERT);
        map.put(Keys.DELETE.charAt(0),        KeyboardEvent.DOM_VK_DELETE);
        map.put(Keys.SEMICOLON.charAt(0),     KeyboardEvent.DOM_VK_SEMICOLON);
        map.put(Keys.EQUALS.charAt(0),        KeyboardEvent.DOM_VK_EQUALS);

        map.put(Keys.NUMPAD0.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD0);
        map.put(Keys.NUMPAD1.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD1);
        map.put(Keys.NUMPAD2.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD2);
        map.put(Keys.NUMPAD3.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD3);
        map.put(Keys.NUMPAD4.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD4);
        map.put(Keys.NUMPAD5.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD5);
        map.put(Keys.NUMPAD6.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD6);
        map.put(Keys.NUMPAD7.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD7);
        map.put(Keys.NUMPAD8.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD8);
        map.put(Keys.NUMPAD9.charAt(0),       KeyboardEvent.DOM_VK_NUMPAD9);

        map.put(Keys.MULTIPLY.charAt(0),      KeyboardEvent.DOM_VK_MULTIPLY);
        map.put(Keys.ADD.charAt(0),           KeyboardEvent.DOM_VK_ADD);
        map.put(Keys.SEPARATOR.charAt(0),     KeyboardEvent.DOM_VK_SEPARATOR);
        map.put(Keys.SUBTRACT.charAt(0),      KeyboardEvent.DOM_VK_SUBTRACT);
        map.put(Keys.DECIMAL.charAt(0),       KeyboardEvent.DOM_VK_DECIMAL);
        map.put(Keys.DIVIDE.charAt(0),        KeyboardEvent.DOM_VK_DIVIDE);

        map.put(Keys.F1.charAt(0),            KeyboardEvent.DOM_VK_F1);
        map.put(Keys.F2.charAt(0),            KeyboardEvent.DOM_VK_F2);
        map.put(Keys.F3.charAt(0),            KeyboardEvent.DOM_VK_F3);
        map.put(Keys.F4.charAt(0),            KeyboardEvent.DOM_VK_F4);
        map.put(Keys.F5.charAt(0),            KeyboardEvent.DOM_VK_F5);
        map.put(Keys.F6.charAt(0),            KeyboardEvent.DOM_VK_F6);
        map.put(Keys.F7.charAt(0),            KeyboardEvent.DOM_VK_F7);
        map.put(Keys.F8.charAt(0),            KeyboardEvent.DOM_VK_F8);
        map.put(Keys.F9.charAt(0),            KeyboardEvent.DOM_VK_F9);
        map.put(Keys.F10.charAt(0),           KeyboardEvent.DOM_VK_F10);
        map.put(Keys.F11.charAt(0),           KeyboardEvent.DOM_VK_F11);
        map.put(Keys.F12.charAt(0),           KeyboardEvent.DOM_VK_F12);

        map.put(Keys.META.charAt(0),          KeyboardEvent.DOM_VK_META);

        // Keys.COMMAND has no HtmlUnit DOM_VK_* equivalent and is intentionally omitted.
        // Keys.ZENKAKU_HANKAKU has no HtmlUnit DOM_VK_* equivalent and is intentionally omitted.

        SPECIAL_KEYS_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Returns {@code true} if {@code ch} is a WebDriver special-key character,
     * i.e. it falls within the Unicode Private Use Area range used by
     * {@link Keys}: {@code \uE000} through {@code \uF8FF} inclusive.
     *
     * <p>Note that this range is broader than the set of characters that have a
     * corresponding entry in the mapping table. Characters in the range that are
     * not mapped (e.g. {@link Keys#COMMAND}) will cause
     * {@link #getKeysMapping(char)} to return {@code 0}.</p>
     *
     * @param ch the character to test
     * @return {@code true} if the character is in the WebDriver PUA key range
     */
    static boolean isSpecialKey(final char ch) {
        return ch >= '\uE000' && ch <= '\uF8FF';
    }

    /**
     * Returns the HtmlUnit {@code DOM_VK_*} constant corresponding to the given
     * WebDriver special-key character.
     *
     * <p>If no mapping exists for {@code ch} (e.g. for {@link Keys#COMMAND} or
     * {@link Keys#ZENKAKU_HANKAKU}), this method returns {@code 0} as a sentinel
     * value and logs a WARNING. Callers should treat {@code 0} as "no applicable
     * key code" and skip the press/release operation.</p>
     *
     * @param ch a character for which {@link #isSpecialKey(char)} returns
     *           {@code true}
     * @return the {@code DOM_VK_*} constant, or {@code 0} if the key has no
     *         HtmlUnit equivalent
     */
    static int getKeysMapping(final char ch) {
        final Integer keyCode = SPECIAL_KEYS_MAP.get(ch);
        if (keyCode == null) {
            LOG.log(Level.WARNING,
                    "No DOM_VK_* mapping for special key character U+{0}; key event will be skipped.",
                    Integer.toHexString(ch).toUpperCase());
            return 0;
        }
        return keyCode;
    }

    /** Utility class; not instantiable. */
    private HtmlUnitKeyboardMapping() {
    }
}
