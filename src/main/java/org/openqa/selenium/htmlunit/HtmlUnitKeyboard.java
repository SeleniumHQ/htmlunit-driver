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
import java.util.ArrayList;
import java.util.List;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlFileInput;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlNumberInput;
import org.htmlunit.html.Keyboard;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;

/**
 * Provides keyboard input handling for {@link HtmlUnitDriver}. Manages
 * modifier-key state, dispatches key events to HtmlUnit DOM elements, and
 * coordinates focus changes when sending keyboard input.
 *
 * <p>Instances are typically obtained through the driver's input-device
 * system rather than created directly.</p>
 *
 * <p><b>Modifier-state integrity:</b> modifier state ({@link KeyboardModifiersState})
 * is updated <em>only after</em> a key event has been successfully dispatched
 * to the underlying element. This ensures that a failed {@code element.type()}
 * call does not leave the modifier state out of sync with what the browser
 * actually processed.</p>
 *
 * <p>This class is not thread-safe; all calls are expected to originate from
 * the WebDriver/test thread.</p>
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
     * Updated only after a key event has been successfully dispatched.
     */
    private final KeyboardModifiersState modifiersState_ = new KeyboardModifiersState();

    /**
     * The owning {@link HtmlUnitDriver} used to resolve the active element,
     * manage focus changes, and integrate with other input devices.
     */
    private final HtmlUnitDriver parent_;

    /**
     * The last element that received keyboard input. Used to determine whether
     * cursor placement should start at the end of the element's value
     * ({@code startAtEnd = true}) or stay in place ({@code startAtEnd = false}).
     */
    private HtmlElement lastElement_;

    /**
     * Creates a new {@link HtmlUnitKeyboard} associated with the specified driver.
     *
     * @param parent the driver instance that owns this keyboard; must not be {@code null}
     */
    HtmlUnitKeyboard(final HtmlUnitDriver parent) {
        parent_ = parent;
    }

    /**
     * Sends the given key sequence to the currently active element.
     *
     * <p>The active element is resolved via {@link HtmlUnitDriver#switchTo()
     * switchTo().activeElement()}. The result must be an {@link HtmlUnitWebElement};
     * if a non-HtmlUnit element is active (e.g. in a decorated driver), an
     * {@link IllegalStateException} is thrown rather than an opaque
     * {@link ClassCastException}.</p>
     *
     * @param keysToSend one or more character sequences to send; must not be {@code null}
     * @throws IllegalStateException  if the active element is not an {@link HtmlUnitWebElement}
     * @throws WebDriverException     if an I/O error occurs while dispatching the keys
     */
    public void sendKeys(final CharSequence... keysToSend) {
        final Object activeElement = parent_.switchTo().activeElement();
        if (!(activeElement instanceof HtmlUnitWebElement huWebElement)) {
            throw new IllegalStateException(
                    "Active element is not an HtmlUnitWebElement: "
                    + (activeElement == null ? "null" : activeElement.getClass().getName()));
        }
        sendKeys(huWebElement, false, keysToSend);
    }

    /**
     * Sends the specified key sequence to the given {@link HtmlUnitWebElement}.
     *
     * <p>Verifies that the element is interactable, constructs an
     * {@link InputKeysContainer} describing the sequence, manages focus
     * transitions, and dispatches key events to the underlying {@link HtmlElement}.
     * If the sequence contains a submit key and the element is an {@link HtmlInput}
     * inside a form, the form is submitted after dispatch.</p>
     *
     * @param htmlElem        the target element wrapper; must not be {@code null}
     * @param releaseAllAtEnd whether all currently pressed modifier keys should be
     *                        released after the sequence is dispatched
     * @param value           the keys to send
     * @throws ElementNotInteractableException if the element cannot receive input
     * @throws WebDriverException              if an I/O error occurs during dispatch
     */
    void sendKeys(final HtmlUnitWebElement htmlElem, final boolean releaseAllAtEnd,
            final CharSequence... value) {
        htmlElem.verifyCanInteractWithElement(false);

        final HtmlElement element = (HtmlElement) htmlElem.getElement();
        final boolean inputElementInsideForm =
                element instanceof HtmlInput && element.getEnclosingForm() != null;
        final InputKeysContainer keysContainer = new InputKeysContainer(inputElementInsideForm, value);

        htmlElem.switchFocusToThisIfNeeded();

        sendKeys(element, keysContainer, releaseAllAtEnd);

        if (inputElementInsideForm && keysContainer.wasSubmitKeyFound()) {
            htmlElem.submitImpl();
        }
    }

    /**
     * Core dispatch method: builds the HtmlUnit {@link Keyboard} from the key
     * container, appends any modifier-release events if requested, dispatches
     * the whole sequence to {@code element}, and then — only on success —
     * commits the modifier-state changes and updates {@link #lastElement_}.
     *
     * <p><b>Modifier-state integrity:</b> keyboard construction collects pending
     * state changes in a {@link List} without touching {@link #modifiersState_}.
     * The changes are applied to {@link #modifiersState_} only after
     * {@code element.type()} returns normally, so a failed dispatch leaves the
     * modifier state unchanged.</p>
     */
    private void sendKeys(final HtmlElement element,
            final InputKeysContainer keysToSend, final boolean releaseAllAtEnd) {
        keysToSend.setCapitalization(modifiersState_.isShiftPressed());
        final String keysSequence = keysToSend.toString();

        // HtmlElement.type() does not modify HtmlFileInput values — handle separately.
        if (element instanceof HtmlFileInput) {
            final HtmlFileInput fileInput = (HtmlFileInput) element;
            fileInput.setValue(keysSequence);
            // Release modifiers even for file inputs if requested, so the modifier
            // state stays consistent with what the caller intended.
            if (releaseAllAtEnd) {
                releaseAllModifiers();
            }
            lastElement_ = element;
            return;
        }

        try {
            final boolean startAtEnd = lastElement_ != element && !(element instanceof HtmlNumberInput);
            final List<Runnable> pendingStateChanges = new ArrayList<>();
            final Keyboard keyboard = buildKeyboard(startAtEnd, keysSequence, true, pendingStateChanges);

            if (releaseAllAtEnd) {
                if (isShiftPressed()) {
                    addToKeyboard(keyboard, Keys.SHIFT.charAt(0), false, pendingStateChanges);
                }
                if (isAltPressed()) {
                    addToKeyboard(keyboard, Keys.ALT.charAt(0), false, pendingStateChanges);
                }
                if (isCtrlPressed()) {
                    addToKeyboard(keyboard, Keys.CONTROL.charAt(0), false, pendingStateChanges);
                }
            }

            element.type(keyboard);

            // Dispatch succeeded — now commit the modifier state changes.
            pendingStateChanges.forEach(Runnable::run);
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }

        lastElement_ = element;
    }

    /**
     * Sends a key-down event for each character in {@code keyToPress} to the
     * currently active element, then updates the modifier state.
     *
     * <p>Modifier state is updated only after the key events have been
     * successfully dispatched. {@link #lastElement_} is updated on success so
     * that subsequent calls compute {@code startAtEnd} correctly.</p>
     *
     * @param keyToPress the key or key sequence to press; must not be {@code null}
     * @throws WebDriverException if an {@link IOException} occurs during dispatch
     */
    public void pressKey(final CharSequence keyToPress) {
        final HtmlUnitWebElement htmlElement = (HtmlUnitWebElement) parent_.switchTo().activeElement();
        final HtmlElement element = (HtmlElement) htmlElement.getElement();

        final List<Runnable> pendingStateChanges = new ArrayList<>();
        final Keyboard keyboard = buildKeyboard(
                lastElement_ != element, keyToPress, true, pendingStateChanges);
        try {
            element.type(keyboard);
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }

        // Commit modifier state only after successful dispatch.
        pendingStateChanges.forEach(Runnable::run);
        lastElement_ = element;
    }

    /**
     * Sends a key-up event for each character in {@code keyToRelease} to the
     * currently active element, then updates the modifier state.
     *
     * <p>Modifier state is updated only after the key events have been
     * successfully dispatched. {@link #lastElement_} is updated on success so
     * that subsequent calls compute {@code startAtEnd} correctly.</p>
     *
     * @param keyToRelease the key or key sequence to release; must not be {@code null}
     * @throws WebDriverException if an {@link IOException} occurs during dispatch
     */
    public void releaseKey(final CharSequence keyToRelease) {
        final HtmlUnitWebElement htmlElement = (HtmlUnitWebElement) parent_.switchTo().activeElement();
        final HtmlElement element = (HtmlElement) htmlElement.getElement();

        final List<Runnable> pendingStateChanges = new ArrayList<>();
        final Keyboard keyboard = buildKeyboard(
                lastElement_ != element, keyToRelease, false, pendingStateChanges);
        try {
            element.type(keyboard);
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }

        // Commit modifier state only after successful dispatch.
        pendingStateChanges.forEach(Runnable::run);
        lastElement_ = element;
    }

    /**
     * Builds a HtmlUnit {@link Keyboard} from the given key sequence without
     * mutating {@link #modifiersState_} yet. Instead, each modifier-state change
     * that would result from dispatching the sequence is recorded as a
     * {@link Runnable} in {@code pendingStateChanges}. The caller is responsible
     * for invoking those runnables after a successful {@code element.type()} call.
     *
     * <p>This separation ensures that a failed dispatch does not leave the
     * modifier state partially updated.</p>
     *
     * @param startAtEnd          whether the keyboard should position the cursor at
     *                            the end of the element's current value before typing
     * @param keysSequence        the sequence of characters to process
     * @param isPress             {@code true} to generate key-press events;
     *                            {@code false} to generate key-release events
     * @param pendingStateChanges mutable list to which modifier-state update
     *                            runnables are appended; must not be {@code null}
     * @return the constructed {@link Keyboard} ready for dispatch
     */
    private Keyboard buildKeyboard(final boolean startAtEnd, final CharSequence keysSequence,
            final boolean isPress, final List<Runnable> pendingStateChanges) {
        final Keyboard keyboard = new Keyboard(startAtEnd);
        for (int i = 0; i < keysSequence.length(); i++) {
            addToKeyboard(keyboard, keysSequence.charAt(i), isPress, pendingStateChanges);
        }
        return keyboard;
    }

    /**
     * Appends a single character to the given {@link Keyboard}. For special
     * (modifier/function) keys, the appropriate press or release event is added
     * and the corresponding modifier-state update is deferred into
     * {@code pendingStateChanges}. For regular characters, a type event is added.
     *
     * @param keyboard            the keyboard to append to
     * @param ch                  the character to process
     * @param isPress             {@code true} for press; {@code false} for release
     * @param pendingStateChanges deferred modifier-state updates
     */
    private void addToKeyboard(final Keyboard keyboard, final char ch, final boolean isPress,
            final List<Runnable> pendingStateChanges) {
        if (HtmlUnitKeyboardMapping.isSpecialKey(ch)) {
            final int keyCode = HtmlUnitKeyboardMapping.getKeysMapping(ch);
            if (isPress) {
                keyboard.press(keyCode);
                pendingStateChanges.add(() -> modifiersState_.storeKeyDown(ch));
            }
            else {
                keyboard.release(keyCode);
                pendingStateChanges.add(() -> modifiersState_.storeKeyUp(ch));
            }
        }
        else {
            keyboard.type(ch);
        }
    }

    /**
     * Releases all currently pressed modifier keys directly on the modifier state.
     * Used for the {@link HtmlFileInput} path where no {@link Keyboard} dispatch
     * occurs but modifiers still need to be cleared when {@code releaseAllAtEnd}
     * is requested.
     */
    private void releaseAllModifiers() {
        if (isShiftPressed()) {
            modifiersState_.storeKeyUp(Keys.SHIFT.charAt(0));
        }
        if (isAltPressed()) {
            modifiersState_.storeKeyUp(Keys.ALT.charAt(0));
        }
        if (isCtrlPressed()) {
            modifiersState_.storeKeyUp(Keys.CONTROL.charAt(0));
        }
    }

    /**
     * Returns whether the Shift key is currently pressed.
     *
     * @return {@code true} if Shift is pressed
     */
    public boolean isShiftPressed() {
        return modifiersState_.isShiftPressed();
    }

    /**
     * Returns whether the Control key is currently pressed.
     *
     * @return {@code true} if Ctrl is pressed
     */
    public boolean isCtrlPressed() {
        return modifiersState_.isCtrlPressed();
    }

    /**
     * Returns whether the Alt key is currently pressed.
     *
     * @return {@code true} if Alt is pressed
     */
    public boolean isAltPressed() {
        return modifiersState_.isAltPressed();
    }

    /**
     * Returns whether the specified character key is currently pressed.
     *
     * @param ch the character representing the key
     * @return {@code true} if the key is pressed
     */
    public boolean isPressed(final char ch) {
        return modifiersState_.isPressed(ch);
    }

    /**
     * Returns whether the specified {@link Keys} value is currently pressed.
     *
     * @param keys the key to check; must not be {@code null}
     * @return {@code true} if the key is pressed
     */
    public boolean isPressed(final Keys keys) {
        return modifiersState_.isPressed(keys);
    }
}