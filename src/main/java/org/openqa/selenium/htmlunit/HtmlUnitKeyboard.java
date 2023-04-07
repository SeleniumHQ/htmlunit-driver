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

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlFileInput;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlNumberInput;
import org.htmlunit.html.Keyboard;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;

/**
 * Implements keyboard operations using the HtmlUnit WebDriver.
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Simon Stewart
 * @author Glib Briia
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitKeyboard {
    private final KeyboardModifiersState modifiersState_ = new KeyboardModifiersState();
    private final HtmlUnitDriver parent_;
    private HtmlElement lastElement_;

    HtmlUnitKeyboard(final HtmlUnitDriver parent) {
        parent_ = parent;
    }

    public void sendKeys(final CharSequence... keysToSend) {
        final HtmlUnitWebElement htmlElem = (HtmlUnitWebElement) parent_.switchTo().activeElement();
        sendKeys(htmlElem, false, keysToSend);
    }

    void sendKeys(final HtmlUnitWebElement htmlElem, final boolean releaseAllAtEnd, final CharSequence... value) {
        htmlElem.verifyCanInteractWithElement(false);

        final HtmlElement element = (HtmlElement) htmlElem.getElement();
        final boolean inputElementInsideForm = element instanceof HtmlInput && element.getEnclosingForm() != null;
        final InputKeysContainer keysContainer = new InputKeysContainer(inputElementInsideForm, value);

        htmlElem.switchFocusToThisIfNeeded();

        sendKeys(element, keysContainer, releaseAllAtEnd);

        if (inputElementInsideForm && keysContainer.wasSubmitKeyFound()) {
            htmlElem.submitImpl();
        }
    }

    private void sendKeys(final HtmlElement element,
            final InputKeysContainer keysToSend, final boolean releaseAllAtEnd) {
        keysToSend.setCapitalization(modifiersState_.isShiftPressed());
        final String keysSequence = keysToSend.toString();

        // HtmlElement.type doesn't modify the value of a file input element. Special
        // case.
        if (element instanceof HtmlFileInput) {
            final HtmlFileInput fileInput = (HtmlFileInput) element;
            fileInput.setValue(keysSequence);
            return;
        }

        try {
            final boolean startAtEnd = lastElement_ != element && !(element instanceof HtmlNumberInput);
            final Keyboard keyboard = asHtmlUnitKeyboard(startAtEnd, keysSequence, true);
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
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }
        lastElement_ = element;
    }

    private Keyboard asHtmlUnitKeyboard(final boolean startAtEnd, final CharSequence keysSequence,
            final boolean isPress) {
        final Keyboard keyboard = new Keyboard(startAtEnd);
        for (int i = 0; i < keysSequence.length(); i++) {
            final char ch = keysSequence.charAt(i);
            addToKeyboard(keyboard, ch, isPress);
        }
        return keyboard;
    }

    private void addToKeyboard(final Keyboard keyboard, final char ch, final boolean isPress) {
        if (HtmlUnitKeyboardMapping.isSpecialKey(ch)) {
            final int keyCode = HtmlUnitKeyboardMapping.getKeysMapping(ch);
            if (isPress) {
                keyboard.press(keyCode);
                modifiersState_.storeKeyDown(ch);
            }
            else {
                keyboard.release(keyCode);
                modifiersState_.storeKeyUp(ch);
            }
        }
        else {
            keyboard.type(ch);
        }
    }

    public void pressKey(final CharSequence keyToPress) {
        final HtmlUnitWebElement htmlElement = (HtmlUnitWebElement) parent_.switchTo().activeElement();
        final HtmlElement element = (HtmlElement) htmlElement.getElement();
        try {
            element.type(asHtmlUnitKeyboard(lastElement_ != element, keyToPress, true));
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }
        for (int i = 0; i < keyToPress.length(); i++) {
            final char ch = keyToPress.charAt(i);
            modifiersState_.storeKeyDown(ch);
        }
    }

    public void releaseKey(final CharSequence keyToRelease) {
        final HtmlUnitWebElement htmlElement = (HtmlUnitWebElement) parent_.switchTo().activeElement();
        final HtmlElement element = (HtmlElement) htmlElement.getElement();
        try {
            element.type(asHtmlUnitKeyboard(lastElement_ != element, keyToRelease, false));
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }
        for (int i = 0; i < keyToRelease.length(); i++) {
            final char ch = keyToRelease.charAt(i);
            modifiersState_.storeKeyUp(ch);
        }
    }

    public boolean isShiftPressed() {
        return modifiersState_.isShiftPressed();
    }

    public boolean isCtrlPressed() {
        return modifiersState_.isCtrlPressed();
    }

    public boolean isAltPressed() {
        return modifiersState_.isAltPressed();
    }

    public boolean isPressed(final char ch) {
        return modifiersState_.isPressed(ch);
    }

    public boolean isPressed(final Keys keys) {
        return modifiersState_.isPressed(keys);
    }
}
