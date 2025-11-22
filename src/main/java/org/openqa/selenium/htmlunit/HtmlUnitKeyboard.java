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

import java.io.IOException;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlFileInput;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlNumberInput;
import org.htmlunit.html.Keyboard;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;

/**
 * Provides keyboard input handling for {@link HtmlUnitDriver}. This class
 * manages modifier-key state, dispatches key events to HtmlUnit DOM elements,
 * and coordinates focus changes when sending keyboard input.
 * <p>
 * Instances of this class are typically accessed through the driver’s
 * input device system rather than created directly.
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Simon Stewart
 * @author Glib Briia
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitKeyboard {

    /**
     * Tracks the current state of keyboard modifier keys (Shift, Ctrl, Alt).
     * The state is updated as key sequences are processed.
     */
    private final KeyboardModifiersState modifiersState_ = new KeyboardModifiersState();

    /**
     * The owning {@link HtmlUnitDriver} instance used for resolving the
     * active element, managing focus changes, and integrating with other
     * input devices.
     */
    private final HtmlUnitDriver parent_;

    /**
     * The last element that received keyboard input, used to maintain focus
     * consistency and modifier-key behavior between successive operations.
     */
    private HtmlElement lastElement_;

    /**
     * Creates a new {@link HtmlUnitKeyboard} associated with the specified
     * {@link HtmlUnitDriver}.
     *
     * @param parent the driver instance that owns this keyboard implementation;
     *               must not be {@code null}
     */
    HtmlUnitKeyboard(final HtmlUnitDriver parent) {
        parent_ = parent;
    }

    /**
     * Sends the given key sequence to the element currently identified as the
     * active element within the driver. The keys are dispatched according to
     * HtmlUnit’s keyboard event model, and modifier-key state is updated as
     * needed.
     *
     * @param keysToSend one or more character sequences to send as key input;
     *                   must not be {@code null}
     * @throws NoSuchElementException if there is no active element
     */
    public void sendKeys(final CharSequence... keysToSend) {
        final HtmlUnitWebElement htmlElem = (HtmlUnitWebElement) parent_.switchTo().activeElement();
        sendKeys(htmlElem, false, keysToSend);
    }

    /**
     * Sends the specified key sequence to the given {@link HtmlUnitWebElement}.
     * <p>
     * This method verifies that the element is interactable, prepares an
     * {@link InputKeysContainer} describing the key sequence, manages focus
     * transitions, and dispatches key events to the underlying
     * {@link HtmlElement}. If the key sequence includes a submit key and the
     * element is an {@link HtmlInput} contained within a form, the form is
     * submitted automatically.
     *
     * @param htmlElem         the target element wrapper; must not be {@code null}
     * @param releaseAllAtEnd  whether all pressed modifier keys should be
     *                         released after processing the sequence
     * @param value            the keys to send to the element
     * @throws ElementNotInteractableException if the element cannot receive input
     */
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

    /**
     * Sends a key-down event for the given key sequence to the element currently
     * identified as the active element. This method triggers HtmlUnit's low-level
     * keyboard event processing and updates the internal modifier-key state.
     *
     * @param keyToPress the key or key sequence to press; must not be {@code null}
     * @throws WebDriverException if an {@link IOException} occurs while dispatching
     *                            the key event
     */
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

    /**
     * Sends a key-up event for the given key sequence to the element currently
     * identified as the active element. This method triggers HtmlUnit's low-level
     * keyboard event processing and updates the internal modifier-key state.
     *
     * @param keyToRelease the key or key sequence to release; must not be {@code null}
     * @throws WebDriverException if an {@link IOException} occurs while dispatching
     *                            the key event
     */
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

    /**
     * Returns whether the Shift key is currently pressed according to the
     * tracked modifier-key state.
     *
     * @return {@code true} if Shift is pressed, {@code false} otherwise
     */
    public boolean isShiftPressed() {
        return modifiersState_.isShiftPressed();
    }

    /**
     * Returns whether the Control key is currently pressed according to the
     * tracked modifier-key state.
     *
     * @return {@code true} if Ctrl is pressed, {@code false} otherwise
     */
    public boolean isCtrlPressed() {
        return modifiersState_.isCtrlPressed();
    }

    /**
     * Returns whether the Alt key is currently pressed according to the
     * tracked modifier-key state.
     *
     * @return {@code true} if Alt is pressed, {@code false} otherwise
     */
    public boolean isAltPressed() {
        return modifiersState_.isAltPressed();
    }

    /**
     * Returns whether the specified character key is currently pressed.
     *
     * @param ch the character representing the key
     * @return {@code true} if the key is pressed, {@code false} otherwise
     */
    public boolean isPressed(final char ch) {
        return modifiersState_.isPressed(ch);
    }

    /**
     * Returns whether the specified {@link Keys} value is currently pressed.
     *
     * @param keys the key to check; must not be {@code null}
     * @return {@code true} if the key is pressed, {@code false} otherwise
     */
    public boolean isPressed(final Keys keys) {
        return modifiersState_.isPressed(keys);
    }
}
