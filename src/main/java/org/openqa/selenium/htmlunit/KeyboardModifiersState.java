// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit;

import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.Keys;

/**
 * Holds the state of the modifier keys (Shift, ctrl, alt).
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
class KeyboardModifiersState {
    private final Set<Character> set_ = new HashSet<>();
    private boolean shiftPressed_;
    private boolean ctrlPressed_;
    private boolean altPressed_;

    public boolean isShiftPressed() {
        return shiftPressed_;
    }

    public boolean isCtrlPressed() {
        return ctrlPressed_;
    }

    public boolean isAltPressed() {
        return altPressed_;
    }

    public void storeKeyDown(final char key) {
        storeIfEqualsShift(key, true);
        storeIfEqualsCtrl(key, true);
        storeIfEqualsAlt(key, true);
        set_.add(key);
    }

    public void storeKeyUp(final char key) {
        storeIfEqualsShift(key, false);
        storeIfEqualsCtrl(key, false);
        storeIfEqualsAlt(key, false);
        set_.remove(key);
    }

    private void storeIfEqualsShift(final char key, final boolean keyState) {
        if (key == Keys.SHIFT.charAt(0)) {
            shiftPressed_ = keyState;
        }
    }

    private void storeIfEqualsCtrl(final char key, final boolean keyState) {
        if (key == Keys.CONTROL.charAt(0)) {
            ctrlPressed_ = keyState;
        }
    }

    private void storeIfEqualsAlt(final char key, final boolean keyState) {
        if (key == Keys.ALT.charAt(0)) {
            altPressed_ = keyState;
        }
    }

    boolean isPressed(final Keys keys) {
        return isPressed(keys.charAt(0));
    }

    boolean isPressed(final char ch) {
        return set_.contains(ch);
    }
}
