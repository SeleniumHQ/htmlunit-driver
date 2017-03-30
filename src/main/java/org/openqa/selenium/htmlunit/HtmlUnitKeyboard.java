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

import java.io.IOException;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.Keyboard;

/**
 * Implements keyboard operations using the HtmlUnit WebDriver.
 */
public class HtmlUnitKeyboard implements org.openqa.selenium.interactions.Keyboard {
  private KeyboardModifiersState modifiersState = new KeyboardModifiersState();
  private final HtmlUnitDriver parent;
  private HtmlElement lastElement;

  HtmlUnitKeyboard(HtmlUnitDriver parent) {
    this.parent = parent;
  }

  @Override
  public void sendKeys(CharSequence... keysToSend) {
    HtmlUnitWebElement htmlElem = (HtmlUnitWebElement) parent.switchTo().activeElement();
    sendKeys(htmlElem, false, keysToSend);
  }

  void sendKeys(HtmlUnitWebElement htmlElem, boolean releaseAllAtEnd, CharSequence... value) {
    htmlElem.verifyCanInteractWithElement(false);

    final HtmlElement element = (HtmlElement) htmlElem.element;
    final boolean inputElementInsideForm = element instanceof HtmlInput
        && ((HtmlInput) element).getEnclosingForm() != null;
    InputKeysContainer keysContainer = new InputKeysContainer(inputElementInsideForm, value);

    htmlElem.switchFocusToThisIfNeeded();

    sendKeys(element, keysContainer, releaseAllAtEnd);

    if (inputElementInsideForm && keysContainer.wasSubmitKeyFound()) {
      htmlElem.submitImpl();
    }
  }

  private void sendKeys(HtmlElement element, InputKeysContainer keysToSend, boolean releaseAllAtEnd) {
    keysToSend.setCapitalization(modifiersState.isShiftPressed());
    String keysSequence = keysToSend.toString();

    // HtmlElement.type doesn't modify the value of a file input element. Special case.
    if (element instanceof HtmlFileInput) {
      HtmlFileInput fileInput = (HtmlFileInput) element;
      fileInput.setValueAttribute(keysSequence);
      return;
    }

    try {
      Keyboard keyboard = asHtmlUnitKeyboard(lastElement != element, keysSequence, true);
      if (releaseAllAtEnd) {
        if (isShiftPressed()) {
          addToKeyboard(keyboard, Keys.SHIFT.charAt(0), false);
        }
        if (isAltPressed()) {
          addToKeyboard(keyboard, Keys.ALT.charAt(0), false);
        }
        if (isCtrlPressed()) {
          addToKeyboard(keyboard, Keys.CONTROL.charAt(0), false);
        }
      }
      element.type(keyboard);
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
    lastElement = element;
  }

  private Keyboard asHtmlUnitKeyboard(final boolean startAtEnd, final CharSequence keysSequence, final boolean isPress) {
    Keyboard keyboard = new Keyboard(startAtEnd);
    for (int i = 0; i < keysSequence.length(); i++) {
      char ch = keysSequence.charAt(i);
      addToKeyboard(keyboard, ch, isPress);
    }
    return keyboard;
  }

  private void addToKeyboard(final Keyboard keyboard, char ch, final boolean isPress) {
    if (HtmlUnitKeyboardMapping.isSpecialKey(ch)) {
      int keyCode = HtmlUnitKeyboardMapping.getKeysMapping(ch);
      if (isPress) {
        keyboard.press(keyCode);
        modifiersState.storeKeyDown(ch);
      }
      else {
        keyboard.release(keyCode);
        modifiersState.storeKeyUp(ch);
      }
    }
    else {
      keyboard.type(ch);
    }
  }

  @Override
  public void pressKey(CharSequence keyToPress) {
    HtmlUnitWebElement htmlElement = (HtmlUnitWebElement) parent.switchTo().activeElement();
    HtmlElement element = (HtmlElement) htmlElement.element;
    try {
      element.type(asHtmlUnitKeyboard(lastElement != element, keyToPress, true));
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
    for (int i = 0; i < keyToPress.length(); i++) {
      char ch = keyToPress.charAt(i);
      modifiersState.storeKeyDown(ch);
    }
  }

  @Override
  public void releaseKey(CharSequence keyToRelease) {
    HtmlUnitWebElement htmlElement = (HtmlUnitWebElement) parent.switchTo().activeElement();
    HtmlElement element = (HtmlElement) htmlElement.element;
    try {
      element.type(asHtmlUnitKeyboard(lastElement != element, keyToRelease, false));
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
    for (int i = 0; i < keyToRelease.length(); i++) {
      char ch = keyToRelease.charAt(i);
      modifiersState.storeKeyUp(ch);
    }
  }

  public boolean isShiftPressed() {
    return modifiersState.isShiftPressed();
  }

  public boolean isCtrlPressed() {
    return modifiersState.isCtrlPressed();
  }

  public boolean isAltPressed() {
    return modifiersState.isAltPressed();
  }

  public boolean isPressed(char ch) {
    return modifiersState.isPressed(ch);
  }

  public boolean isPressed(Keys keys) {
    return modifiersState.isPressed(keys);
  }
}
