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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.Keys;

/**
 * Tests for {@link KeyboardModifiersState}.
 *
 * @author Ronald Brill
 */
public class KeyboardModifiersStateTest {

    /**
     * Nothing is pressed initially.
     */
    @Test
    public void initial() {
        final KeyboardModifiersState state = new KeyboardModifiersState();
        assertFalse(state.isShiftPressed());
        assertFalse(state.isCtrlPressed());
        assertFalse(state.isAltPressed());
        assertFalse(state.isMetaPressed());
    }

    /**
     * The left-hand keys are the same as the primary ones.
     */
    @Test
    public void leftKeys() {
        final KeyboardModifiersState state = new KeyboardModifiersState();
        state.storeKeyDown(Keys.LEFT_SHIFT.charAt(0));
        state.storeKeyDown(Keys.LEFT_CONTROL.charAt(0));
        state.storeKeyDown(Keys.LEFT_ALT.charAt(0));
        assertTrue(state.isShiftPressed());
        assertTrue(state.isCtrlPressed());
        assertTrue(state.isAltPressed());
        assertTrue(state.isPressed(Keys.SHIFT));
        assertFalse(state.isMetaPressed());

        state.storeKeyUp(Keys.SHIFT.charAt(0));
        assertFalse(state.isShiftPressed());
        assertTrue(state.isCtrlPressed());
    }

    /**
     * The right-hand keys count for the modifier but are stored separately.
     */
    @Test
    public void rightKeys() {
        final KeyboardModifiersState state = new KeyboardModifiersState();
        state.storeKeyDown(Keys.RIGHT_SHIFT.charAt(0));
        assertTrue(state.isShiftPressed());
        assertTrue(state.isPressed(Keys.RIGHT_SHIFT));
        assertFalse(state.isPressed(Keys.SHIFT));

        state.storeKeyDown(Keys.RIGHT_CONTROL.charAt(0));
        state.storeKeyDown(Keys.RIGHT_ALT.charAt(0));
        state.storeKeyDown(Keys.RIGHT_COMMAND.charAt(0));
        assertTrue(state.isCtrlPressed());
        assertTrue(state.isAltPressed());
        assertTrue(state.isMetaPressed());

        // releasing the left key does not release the right one
        state.storeKeyUp(Keys.SHIFT.charAt(0));
        assertTrue(state.isShiftPressed());

        state.storeKeyUp(Keys.RIGHT_SHIFT.charAt(0));
        assertFalse(state.isShiftPressed());
    }

    /**
     * The meta key and its alias command.
     */
    @Test
    public void metaKey() {
        final KeyboardModifiersState state = new KeyboardModifiersState();
        state.storeKeyDown(Keys.COMMAND.charAt(0));
        assertTrue(state.isMetaPressed());
        assertTrue(state.isPressed(Keys.META));

        state.storeKeyUp(Keys.META.charAt(0));
        assertFalse(state.isMetaPressed());
    }

    /**
     * {@link KeyboardModifiersState#copyFrom(KeyboardModifiersState)} replaces the whole state.
     */
    @Test
    public void copyFrom() {
        final KeyboardModifiersState state = new KeyboardModifiersState();
        state.storeKeyDown(Keys.CONTROL.charAt(0));

        final KeyboardModifiersState other = new KeyboardModifiersState();
        other.storeKeyDown(Keys.RIGHT_SHIFT.charAt(0));

        state.copyFrom(other);
        assertTrue(state.isShiftPressed());
        assertFalse(state.isCtrlPressed());

        // the copy is independent
        other.storeKeyUp(Keys.RIGHT_SHIFT.charAt(0));
        assertTrue(state.isShiftPressed());
    }
}
