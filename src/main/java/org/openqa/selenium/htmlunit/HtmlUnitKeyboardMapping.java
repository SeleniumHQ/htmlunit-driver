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

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.Keys;

import com.gargoylesoftware.htmlunit.javascript.host.event.KeyboardEvent;

/**
 * Maps {@link Keys} to their {@link KeyboardEvent} constant.
 */
public class HtmlUnitKeyboardMapping {

  private static final Map<Character, Integer> specialKeysMap = new HashMap<>();

  static {
    addMapping(Keys.CANCEL, KeyboardEvent.DOM_VK_CANCEL);
    addMapping(Keys.HELP, KeyboardEvent.DOM_VK_HELP);
    addMapping(Keys.BACK_SPACE, KeyboardEvent.DOM_VK_BACK_SPACE);
    addMapping(Keys.TAB, KeyboardEvent.DOM_VK_TAB);
    addMapping(Keys.CLEAR, KeyboardEvent.DOM_VK_CLEAR);
    addMapping(Keys.RETURN, KeyboardEvent.DOM_VK_RETURN);

    // No match for Keys.ENTER
    addMapping(Keys.ENTER, KeyboardEvent.DOM_VK_RETURN);
    addMapping(Keys.SHIFT, KeyboardEvent.DOM_VK_SHIFT);
    // No match for Keys.LEFT_SHIFT
    addMapping(Keys.LEFT_SHIFT, KeyboardEvent.DOM_VK_SHIFT);
    addMapping(Keys.CONTROL, KeyboardEvent.DOM_VK_CONTROL);
    // No match for Keys.LEFT_CONTROL
    addMapping(Keys.LEFT_CONTROL, KeyboardEvent.DOM_VK_CONTROL);
    addMapping(Keys.ALT, KeyboardEvent.DOM_VK_ALT);
    // No match for Keys.LEFT_ALT
    addMapping(Keys.LEFT_ALT, KeyboardEvent.DOM_VK_ALT);
    addMapping(Keys.PAUSE, KeyboardEvent.DOM_VK_PAUSE);
    addMapping(Keys.ESCAPE, KeyboardEvent.DOM_VK_ESCAPE);
    addMapping(Keys.SPACE, KeyboardEvent.DOM_VK_SPACE);
    addMapping(Keys.PAGE_UP, KeyboardEvent.DOM_VK_PAGE_UP);
    addMapping(Keys.PAGE_DOWN, KeyboardEvent.DOM_VK_PAGE_DOWN);
    addMapping(Keys.END, KeyboardEvent.DOM_VK_END);
    addMapping(Keys.HOME, KeyboardEvent.DOM_VK_HOME);
    addMapping(Keys.LEFT, KeyboardEvent.DOM_VK_LEFT);
    addMapping(Keys.ARROW_LEFT, KeyboardEvent.DOM_VK_LEFT);
    addMapping(Keys.UP, KeyboardEvent.DOM_VK_UP);
    addMapping(Keys.ARROW_UP, KeyboardEvent.DOM_VK_UP);
    addMapping(Keys.RIGHT, KeyboardEvent.DOM_VK_RIGHT);
    addMapping(Keys.ARROW_RIGHT, KeyboardEvent.DOM_VK_RIGHT);
    addMapping(Keys.DOWN, KeyboardEvent.DOM_VK_CLEAR);
    addMapping(Keys.ARROW_DOWN, KeyboardEvent.DOM_VK_DOWN);
    addMapping(Keys.INSERT, KeyboardEvent.DOM_VK_INSERT);
    addMapping(Keys.DELETE, KeyboardEvent.DOM_VK_DELETE);
    addMapping(Keys.SEMICOLON, KeyboardEvent.DOM_VK_SEMICOLON);
    addMapping(Keys.EQUALS, KeyboardEvent.DOM_VK_EQUALS);
    addMapping(Keys.NUMPAD0, KeyboardEvent.DOM_VK_NUMPAD0);
    addMapping(Keys.NUMPAD1, KeyboardEvent.DOM_VK_NUMPAD1);
    addMapping(Keys.NUMPAD2, KeyboardEvent.DOM_VK_NUMPAD2);
    addMapping(Keys.NUMPAD3, KeyboardEvent.DOM_VK_NUMPAD3);
    addMapping(Keys.NUMPAD4, KeyboardEvent.DOM_VK_NUMPAD4);
    addMapping(Keys.NUMPAD5, KeyboardEvent.DOM_VK_NUMPAD5);
    addMapping(Keys.NUMPAD6, KeyboardEvent.DOM_VK_NUMPAD6);
    addMapping(Keys.NUMPAD7, KeyboardEvent.DOM_VK_NUMPAD7);
    addMapping(Keys.NUMPAD8, KeyboardEvent.DOM_VK_NUMPAD8);
    addMapping(Keys.NUMPAD9, KeyboardEvent.DOM_VK_NUMPAD9);
    addMapping(Keys.MULTIPLY, KeyboardEvent.DOM_VK_MULTIPLY);
    addMapping(Keys.ADD, KeyboardEvent.DOM_VK_ADD);
    addMapping(Keys.SEPARATOR, KeyboardEvent.DOM_VK_SEPARATOR);
    addMapping(Keys.SUBTRACT, KeyboardEvent.DOM_VK_SUBTRACT);
    addMapping(Keys.DECIMAL, KeyboardEvent.DOM_VK_DECIMAL);
    addMapping(Keys.DIVIDE, KeyboardEvent.DOM_VK_DIVIDE);
    addMapping(Keys.F1, KeyboardEvent.DOM_VK_F1);
    addMapping(Keys.F2, KeyboardEvent.DOM_VK_F2);
    addMapping(Keys.F3, KeyboardEvent.DOM_VK_F3);
    addMapping(Keys.F4, KeyboardEvent.DOM_VK_F4);
    addMapping(Keys.F5, KeyboardEvent.DOM_VK_F5);
    addMapping(Keys.F6, KeyboardEvent.DOM_VK_F6);
    addMapping(Keys.F7, KeyboardEvent.DOM_VK_F7);
    addMapping(Keys.F8, KeyboardEvent.DOM_VK_F8);
    addMapping(Keys.F9, KeyboardEvent.DOM_VK_F9);
    addMapping(Keys.F10, KeyboardEvent.DOM_VK_F10);
    addMapping(Keys.F11, KeyboardEvent.DOM_VK_F11);
    addMapping(Keys.F12, KeyboardEvent.DOM_VK_F12);
    addMapping(Keys.META, KeyboardEvent.DOM_VK_META);
    // No match for Keys.COMMAND
    // No match for Keys.ZENKAKU_HANKAKU
  }

  private static void addMapping(Keys keys, int value) {
    specialKeysMap.put(keys.charAt(0), value);
  }

  static boolean isSpecialKey(char ch) {
    return ch >= '\uE000' && ch <= '\uF8FF';
  }

  /**
   * Returns the equivalent constant in {@link KeyboardEvent}.
   */
  static int getKeysMapping(char ch) {
    Integer i = specialKeysMap.get(ch);
    if (i == null) {
      return 0;
    }
    return i;
  }
}