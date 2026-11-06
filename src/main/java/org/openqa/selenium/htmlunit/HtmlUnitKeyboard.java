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
     * Tracks the current state of keyboard modifier keys (Shift, Ctrl, Alt, Meta).
     * Updated only after a key event has been successfully dispatched.
     */
    private final KeyboardModifiersState modifiersState_ = new KeyboardModifiersState();

    /**
     * The owning {@link HtmlUnitDriver} used to resolve the active element,
     * manage focus changes, and integrate with other input devices.
     */
    private final HtmlUnitDriver htmlunitDriver_;

    /**
     * The last element that received keyboard input. Used to determine whether
     * cursor placement should start at the end of the element's value
     * ({@code startAtEnd = true}) or stay in place ({@code startAtEnd = false}).
     */
    private HtmlElement lastElement_;

    /**
     * Creates a new {@link HtmlUnitKeyboard} associated with the specified driver.
     *
     * @param driver the driver instance that owns this keyboard; must not be {@code null}
     */
    HtmlUnitKeyboard(final HtmlUnitDriver driver) {
        htmlunitDriver_ = driver;
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
        final Object activeElement = htmlunitDriver_.switchTo().activeElement();
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
     * The modifier keys in the order they are released at the end of
     * {@code sendKeys} or by {@link Keys#NULL} (like in Chrome: shift, control, alt, meta;
     * each with the left-hand and the right-hand key).
     */
    private static final char[] MODIFIERS_RELEASE_ORDER = {
        Keys.SHIFT.charAt(0), Keys.RIGHT_SHIFT.charAt(0),
        Keys.CONTROL.charAt(0), Keys.RIGHT_CONTROL.charAt(0),
        Keys.ALT.charAt(0), Keys.RIGHT_ALT.charAt(0),
        Keys.META.charAt(0), Keys.RIGHT_COMMAND.charAt(0)
    };

    /** How the keys of a sequence are dispatched. */
    private enum Mode {
        /** Type the keys (sendKeys); modifiers stay pressed, all other keys are pressed and released. */
        TYPE,
        /** Only press the keys (Actions keyDown). */
        DOWN,
        /** Only release the keys (Actions keyUp). */
        UP
    }

    /**
     * Core dispatch method: builds the HtmlUnit {@link Keyboard} from the key
     * container, appends any modifier-release events if requested, dispatches
     * the whole sequence to {@code element}, and then — only on success —
     * commits the modifier-state changes and updates {@link #lastElement_}.
     */
    private void sendKeys(final HtmlElement element,
            final InputKeysContainer keysToSend, final boolean releaseAllAtEnd) {
        final String keysSequence = keysToSend.toString();

        // HtmlElement.type() does not modify HtmlFileInput values — handle separately.
        if (element instanceof HtmlFileInput fileInput) {
            fileInput.setValue(keysSequence);
            // Release modifiers even for file inputs if requested, so the modifier
            // state stays consistent with what the caller intended.
            if (releaseAllAtEnd) {
                releaseAllModifiers();
            }
            lastElement_ = element;
            return;
        }

        final boolean startAtEnd = lastElement_ != element && !(element instanceof HtmlNumberInput);
        dispatch(element, startAtEnd, keysSequence, Mode.TYPE, releaseAllAtEnd);
    }

    /**
     * Sends a key-down event for each character in {@code keyToPress} to the
     * currently active element; the keys stay pressed until they are released
     * by {@link #releaseKey(CharSequence)}.
     *
     * <p>Modifier state is updated only after the key events have been
     * successfully dispatched. {@link #lastElement_} is updated on success so
     * that subsequent calls compute {@code startAtEnd} correctly.</p>
     *
     * @param keyToPress the key or key sequence to press; must not be {@code null}
     * @throws WebDriverException if an {@link IOException} occurs during dispatch
     */
    public void pressKey(final CharSequence keyToPress) {
        final HtmlUnitWebElement htmlElement = (HtmlUnitWebElement) htmlunitDriver_.switchTo().activeElement();
        final HtmlElement element = (HtmlElement) htmlElement.getElement();
        dispatch(element, lastElement_ != element, keyToPress, Mode.DOWN, false);
    }

    /**
     * Sends a key-up event for each character in {@code keyToRelease} to the
     * currently active element.
     *
     * <p>Modifier state is updated only after the key events have been
     * successfully dispatched. {@link #lastElement_} is updated on success so
     * that subsequent calls compute {@code startAtEnd} correctly.</p>
     *
     * @param keyToRelease the key or key sequence to release; must not be {@code null}
     * @throws WebDriverException if an {@link IOException} occurs during dispatch
     */
    public void releaseKey(final CharSequence keyToRelease) {
        final HtmlUnitWebElement htmlElement = (HtmlUnitWebElement) htmlunitDriver_.switchTo().activeElement();
        final HtmlElement element = (HtmlElement) htmlElement.getElement();
        dispatch(element, lastElement_ != element, keyToRelease, Mode.UP, false);
    }

    /**
     * Builds the HtmlUnit {@link Keyboard} for the given key sequence, dispatches it
     * to the element and commits the modifier state afterwards.
     *
     * <p><b>Modifier-state integrity:</b> the keyboard is built using a copy of
     * {@link #modifiersState_}. The copy is committed only after
     * {@code element.type()} returns normally, so a failed dispatch leaves the
     * modifier state unchanged.</p>
     *
     * @param element         the element to type into
     * @param startAtEnd      whether the keyboard should position the cursor at
     *                        the end of the element's current value before typing
     * @param keysSequence    the sequence of characters to process
     * @param mode            how the keys are dispatched
     * @param releaseAllAtEnd whether all pressed modifiers are released at the end
     */
    private void dispatch(final HtmlElement element, final boolean startAtEnd, final CharSequence keysSequence,
            final Mode mode, final boolean releaseAllAtEnd) {
        final KeyboardModifiersState state = new KeyboardModifiersState();
        state.copyFrom(modifiersState_);

        final Keyboard keyboard = new Keyboard(startAtEnd);
        // the modifiers pressed before are still pressed, but there are no new events for them
        keyboard.setPressedModifiers(state.isShiftPressed(), state.isCtrlPressed(),
                state.isAltPressed(), state.isMetaPressed());

        for (int i = 0; i < keysSequence.length(); i++) {
            addToKeyboard(keyboard, keysSequence.charAt(i), mode, state);
        }
        if (releaseAllAtEnd) {
            releaseModifiers(keyboard, state);
        }

        try {
            element.type(keyboard);
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }

        // Dispatch succeeded — now commit the modifier state changes.
        modifiersState_.copyFrom(state);
        lastElement_ = element;
    }

    /**
     * Appends a single character to the given {@link Keyboard} and updates the
     * given modifier state.
     *
     * @param keyboard the keyboard to append to
     * @param ch       the character to process
     * @param mode     how the key is dispatched
     * @param state    the modifier state of the sequence
     */
    private static void addToKeyboard(final Keyboard keyboard, final char ch, final Mode mode,
            final KeyboardModifiersState state) {

        // NULL signals release all modifiers
        if (ch == Keys.NULL.charAt(0)) {
            releaseModifiers(keyboard, state);
            return;
        }

        if (HtmlUnitKeyboardMapping.isSpecialKey(ch)) {
            final int keyCode = HtmlUnitKeyboardMapping.getKeysMapping(ch);
            if (keyCode == 0) {
                // no HtmlUnit equivalent
                return;
            }
            final int location = HtmlUnitKeyboardMapping.getLocation(ch);

            if (mode == Mode.UP) {
                keyboard.keyUp(keyCode, location);
                state.storeKeyUp(ch);
            }
            else if (mode == Mode.DOWN) {
                keyboard.keyDown(keyCode, location);
                state.storeKeyDown(ch);
            }
            else if (HtmlUnitKeyboardMapping.isModifier(ch)) {
                // a modifier stays pressed until the end of sendKeys or a NULL
                if (!state.isPressed(ch)) {
                    keyboard.keyDown(keyCode, location);
                    state.storeKeyDown(ch);
                }
            }
            else {
                keyboard.press(keyCode);
            }
            return;
        }

        final char toType = state.isShiftPressed() ? Character.toUpperCase(ch) : ch;
        if (mode == Mode.UP) {
            keyboard.keyUp(toType);
        }
        else if (mode == Mode.DOWN) {
            keyboard.keyDown(toType);
        }
        else {
            keyboard.type(toType);
        }
    }

    /**
     * Releases all pressed modifier keys in the order of {@link #MODIFIERS_RELEASE_ORDER}.
     *
     * @param keyboard the keyboard to append the releases to
     * @param state    the modifier state of the sequence
     */
    private static void releaseModifiers(final Keyboard keyboard, final KeyboardModifiersState state) {
        for (final char modifier : MODIFIERS_RELEASE_ORDER) {
            if (state.isPressed(modifier)) {
                keyboard.keyUp(HtmlUnitKeyboardMapping.getKeysMapping(modifier),
                        HtmlUnitKeyboardMapping.getLocation(modifier));
                state.storeKeyUp(modifier);
            }
        }
    }

    /**
     * Releases all currently pressed modifier keys directly on the modifier state.
     * Used for the {@link HtmlFileInput} path where no {@link Keyboard} dispatch
     * occurs but modifiers still need to be cleared when {@code releaseAllAtEnd}
     * is requested.
     */
    private void releaseAllModifiers() {
        for (final char modifier : MODIFIERS_RELEASE_ORDER) {
            modifiersState_.storeKeyUp(modifier);
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
     * Returns whether the Meta key is currently pressed.
     *
     * @return {@code true} if Meta is pressed
     */
    public boolean isMetaPressed() {
        return modifiersState_.isMetaPressed();
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
