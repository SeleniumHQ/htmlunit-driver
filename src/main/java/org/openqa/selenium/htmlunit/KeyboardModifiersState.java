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
 */
class KeyboardModifiersState {
  private Set<Character> set = new HashSet<>();
  private boolean shiftPressed;
  private boolean ctrlPressed;
  private boolean altPressed;

  public boolean isShiftPressed() {
    return shiftPressed;
  }

  public boolean isCtrlPressed() {
    return ctrlPressed;
  }

  public boolean isAltPressed() {
    return altPressed;
  }

  public void storeKeyDown(char key) {
    storeIfEqualsShift(key, true);
    storeIfEqualsCtrl(key, true);
    storeIfEqualsAlt(key, true);
    set.add(key);
  }

  public void storeKeyUp(char key) {
    storeIfEqualsShift(key, false);
    storeIfEqualsCtrl(key, false);
    storeIfEqualsAlt(key, false);
    set.remove(key);
  }

  private void storeIfEqualsShift(char key, boolean keyState) {
    if (key == Keys.SHIFT.charAt(0))
      shiftPressed = keyState;
  }

  private void storeIfEqualsCtrl(char key, boolean keyState) {
    if (key == Keys.CONTROL.charAt(0))
      ctrlPressed = keyState;
  }

  private void storeIfEqualsAlt(char key, boolean keyState) {
    if (key == Keys.ALT.charAt(0))
      altPressed = keyState;
  }

  boolean isPressed(Keys keys) {
    return isPressed(keys.charAt(0));
  }

  boolean isPressed(char ch) {
    return set.contains(ch);
  }
}
