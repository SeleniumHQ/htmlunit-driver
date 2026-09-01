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

import java.util.BitSet;

import org.openqa.selenium.Keys;

/**
 * Tracks the pressed/released state of keyboard modifier keys (Shift, Ctrl,
 * Alt) and arbitrary special keys during a WebDriver input sequence.
 *
 * <p><b>Alias handling:</b> WebDriver defines both a primary constant and a
 * "left-hand" alias for each modifier key (e.g. {@link Keys#SHIFT} and
 * {@link Keys#LEFT_SHIFT}). These aliases may have distinct Unicode Private
 * Use Area code points. Both the primary code point and the alias code point
 * are recognised by {@link #storeKeyDown(char)} and {@link #storeKeyUp(char)},
 * so that pressing {@code LEFT_SHIFT} sets {@link #isShiftPressed()} to
 * {@code true} and releasing it correctly clears the flag.</p>
 *
 * <p><b>Single source of truth:</b> modifier state is stored solely in the
 * {@link BitSet} {@code pressedKeys_}, indexed on the {@code char} value.
 * {@link #isShiftPressed()}, {@link #isCtrlPressed()}, and
 * {@link #isAltPressed()} are derived from that set rather than maintained as
 * separate boolean fields, eliminating the possibility of the two
 * representations diverging.</p>
 *
 * <p><b>Performance:</b> {@link BitSet} is used instead of
 * {@code HashSet<Character>} to avoid boxing a primitive {@code char} on
 * every key-event call. The set is sized to cover the Unicode Private Use
 * Area ({@code \uE000–\uF8FF}) where all WebDriver {@link Keys} constants
 * reside, keeping memory usage minimal.</p>
 *
 * <p>This class is not thread-safe; all calls must originate from the
 * WebDriver/test thread.</p>
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
class KeyboardModifiersState {

    // Pre-compute the code points for all modifier primary keys and their
    // left-hand aliases so we don't call charAt(0) on every key event.
    private static final int SHIFT_KEY   = Keys.SHIFT.charAt(0);
    private static final int LEFT_SHIFT  = Keys.LEFT_SHIFT.charAt(0);
    private static final int CTRL_KEY    = Keys.CONTROL.charAt(0);
    private static final int LEFT_CTRL   = Keys.LEFT_CONTROL.charAt(0);
    private static final int ALT_KEY     = Keys.ALT.charAt(0);
    private static final int LEFT_ALT    = Keys.LEFT_ALT.charAt(0);

    /**
     * Tracks which special-key characters are currently in the "pressed" state.
     * Indexed directly by the {@code char} value (treated as an unsigned int),
     * avoiding {@code Character} boxing overhead.
     *
     * <p>The set is sized to {@code 0xF900} (one past the last PUA code point
     * {@code \uF8FF}) so that all WebDriver {@link Keys} values can be stored
     * without a bounds check.</p>
     */
    private final BitSet pressedKeys_ = new BitSet(0xF900);

    /**
     * Records that the given key has been pressed. Both the primary modifier
     * code point and its left-hand alias are treated as the same logical key:
     * pressing either one marks the corresponding modifier as active.
     *
     * @param key the character code point of the key that was pressed
     */
    void storeKeyDown(final char key) {
        pressedKeys_.set(key);
        // If an alias was pressed, also set the primary so that isPressed()
        // queries against either code point return true.
        if (key == LEFT_SHIFT) {
            pressedKeys_.set(SHIFT_KEY);
        }
        else if (key == LEFT_CTRL) {
            pressedKeys_.set(CTRL_KEY);
        }
        else if (key == LEFT_ALT) {
            pressedKeys_.set(ALT_KEY);
        }
    }

    /**
     * Records that the given key has been released. Both the primary modifier
     * code point and its left-hand alias are cleared so that releasing either
     * one correctly clears the modifier flag — preventing permanently "stuck"
     * modifiers when the alias key was used to press and the primary was used
     * to query (or vice versa).
     *
     * @param key the character code point of the key that was released
     */
    void storeKeyUp(final char key) {
        pressedKeys_.clear(key);
        // Mirror the alias <-> primary relationship on key-up.
        if (key == LEFT_SHIFT || key == SHIFT_KEY) {
            pressedKeys_.clear(SHIFT_KEY);
            pressedKeys_.clear(LEFT_SHIFT);
        }
        else if (key == LEFT_CTRL || key == CTRL_KEY) {
            pressedKeys_.clear(CTRL_KEY);
            pressedKeys_.clear(LEFT_CTRL);
        }
        else if (key == LEFT_ALT || key == ALT_KEY) {
            pressedKeys_.clear(ALT_KEY);
            pressedKeys_.clear(LEFT_ALT);
        }
    }

    /**
     * Returns {@code true} if the Shift key (primary or left-hand alias) is
     * currently pressed.
     *
     * @return {@code true} if Shift is down
     */
    boolean isShiftPressed() {
        return pressedKeys_.get(SHIFT_KEY) || pressedKeys_.get(LEFT_SHIFT);
    }

    /**
     * Returns {@code true} if the Control key (primary or left-hand alias) is
     * currently pressed.
     *
     * @return {@code true} if Ctrl is down
     */
    boolean isCtrlPressed() {
        return pressedKeys_.get(CTRL_KEY) || pressedKeys_.get(LEFT_CTRL);
    }

    /**
     * Returns {@code true} if the Alt key (primary or left-hand alias) is
     * currently pressed.
     *
     * @return {@code true} if Alt is down
     */
    boolean isAltPressed() {
        return pressedKeys_.get(ALT_KEY) || pressedKeys_.get(LEFT_ALT);
    }

    /**
     * Returns {@code true} if the key represented by {@code keys} is currently
     * pressed.
     *
     * @param keys the key to query; must not be {@code null}
     * @return {@code true} if the key is down
     */
    boolean isPressed(final Keys keys) {
        return isPressed(keys.charAt(0));
    }

    /**
     * Returns {@code true} if the key with the given character code point is
     * currently pressed.
     *
     * @param ch the character code point to query
     * @return {@code true} if the key is down
     */
    boolean isPressed(final char ch) {
        return pressedKeys_.get(ch);
    }
}