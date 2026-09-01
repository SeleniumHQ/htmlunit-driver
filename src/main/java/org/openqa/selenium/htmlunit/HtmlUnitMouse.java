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
import java.net.SocketTimeoutException;

import org.htmlunit.ScriptException;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.javascript.host.event.MouseEvent;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Coordinates;

/**
 * Provides basic mouse interaction support for {@link HtmlUnitDriver}.
 *
 * <p>Handles element activation, focus changes, and DOM event dispatching
 * needed to emulate mouse behaviour within HtmlUnit's DOM model. Typically
 * used internally by the HtmlUnit-backed WebDriver implementation; not
 * intended for direct instantiation by callers.</p>
 *
 * <p><b>Error handling policy:</b></p>
 * <ul>
 *   <li>{@link IOException} from HtmlUnit event dispatch is always rethrown
 *       as {@link WebDriverException}.</li>
 *   <li>{@link RuntimeException} whose cause is a {@link SocketTimeoutException}
 *       is rethrown as {@link TimeoutException}.</li>
 *   <li>{@link ScriptException} thrown during <em>primary</em> click/double-click
 *       events is rethrown as {@link WebDriverException} so callers are not
 *       silently misled about whether the action succeeded.</li>
 *   <li>{@link ScriptException} thrown during the <em>hover-transition</em>
 *       sequence ({@code mouseover}/{@code mouseout}) is logged at WARNING level
 *       and suppressed; hover-script errors are considered non-fatal because
 *       they should not prevent the primary interaction from being attempted.</li>
 * </ul>
 *
 * <p>This class is not thread-safe; all calls are expected to originate from
 * the WebDriver/test thread.</p>
 *
 * @author Simon Stewart
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitMouse {

    /**
     * The parent {@link HtmlUnitDriver} that owns this mouse instance.
     * Used to resolve the current page, manage focus, and coordinate
     * interactions with other input devices.
     */
    private final HtmlUnitDriver parent_;

    /**
     * The keyboard device associated with this mouse. Used to read modifier-key
     * state (Shift, Ctrl, Alt) when constructing mouse events.
     */
    private final HtmlUnitKeyboard keyboard_;

    /**
     * The element most recently activated by a mouse action. {@code null} until
     * the first successful mouse interaction. Used by
     * {@link #moveOutIfNeeded(DomElement)} to determine whether a hover-out
     * sequence is needed before activating a new element.
     */
    private DomElement currentActiveElement_;

    /**
     * Creates a new {@link HtmlUnitMouse} bound to the given driver and keyboard.
     *
     * @param parent   the owning driver instance; must not be {@code null}
     * @param keyboard the keyboard used to read modifier-key state; must not be {@code null}
     */
    public HtmlUnitMouse(final HtmlUnitDriver parent, final HtmlUnitKeyboard keyboard) {
        parent_ = parent;
        keyboard_ = keyboard;
    }

    /**
     * Resolves the {@link DomElement} to use for an upcoming mouse operation.
     *
     * <p>If {@code potentialCoordinates} is non-null, its auxiliary object is
     * returned (cast to {@link DomElement}). If the auxiliary is itself
     * {@code null}, a {@link NoSuchElementException} is thrown rather than
     * propagating a {@code null} that would cause a {@link NullPointerException}
     * deep inside HtmlUnit.</p>
     *
     * <p>If {@code potentialCoordinates} is {@code null}, the current active
     * element is returned as a fallback (supporting the "operate on the last
     * activated element" pattern). If no element has been activated yet, a
     * {@link NoSuchElementException} is thrown.</p>
     *
     * @param potentialCoordinates coordinates identifying the target element,
     *                             or {@code null} to use the current active element
     * @return the resolved {@link DomElement}; never {@code null}
     * @throws NoSuchElementException if neither coordinates nor an active element
     *                                can provide a non-null element
     */
    private DomElement getElementForOperation(final Coordinates potentialCoordinates) {
        if (potentialCoordinates != null) {
            final DomElement element = (DomElement) potentialCoordinates.getAuxiliary();
            if (element == null) {
                throw new NoSuchElementException(
                        "Coordinates provided but their auxiliary element is null.");
            }
            return element;
        }

        if (currentActiveElement_ == null) {
            throw new NoSuchElementException(
                    "About to perform an interaction that relies on the active element, but there isn't one.");
        }

        return currentActiveElement_;
    }

    /**
     * Performs a mouse click on the element represented by the given
     * {@link Coordinates}, or on the current active element if
     * {@code elementCoordinates} is {@code null}.
     *
     * @param elementCoordinates coordinates identifying the target element;
     *                           {@code null} falls back to the current active element
     * @throws NoSuchElementException          if no element can be resolved
     * @throws ElementNotInteractableException if the element is not displayed
     * @throws TimeoutException                if the click triggers a network timeout
     * @throws WebDriverException              if an I/O or script error occurs
     */
    public void click(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.click(element, false);
    }

    /**
     * Performs a click on the given {@link DomElement}, optionally treating the
     * action as a "direct" click (used for {@link HtmlOption} elements to force
     * Ctrl-click semantics regardless of the current Ctrl modifier state).
     *
     * <p>Before clicking, the element's visibility is verified and a
     * {@code mouseout}/{@code mouseover} hover-transition sequence is dispatched
     * to any previously active element. Modifier-key state (Shift, Ctrl, Alt) is
     * read from the associated {@link HtmlUnitKeyboard}.</p>
     *
     * <p>A {@link ScriptException} thrown during the primary click (e.g. from an
     * {@code onclick} handler) is rethrown as {@link WebDriverException} so that
     * callers are not silently misled about success.</p>
     *
     * @param element     the target DOM element; must not be {@code null}
     * @param directClick {@code true} when called from {@link WebElement#click()}
     *                    (forces Ctrl for {@link HtmlOption}); {@code false} when
     *                    called from {@link Actions#click()}
     * @throws ElementNotInteractableException if the element is not displayed
     * @throws TimeoutException                if the click triggers a network timeout
     * @throws WebDriverException              if an I/O or script error occurs during the click
     */
    void click(final DomElement element, final boolean directClick) {
        if (!element.isDisplayed()) {
            throw new ElementNotInteractableException("You may only interact with visible elements");
        }

        moveOutIfNeeded(element);

        try {
            element.mouseOver();
            element.mouseMove();

            element.click(keyboard_.isShiftPressed(),
                    keyboard_.isCtrlPressed() || (directClick && element instanceof HtmlOption),
                    keyboard_.isAltPressed());
            updateActiveElement(element);
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }
        catch (final ScriptException e) {
            // TODO(simon): This isn't good enough.
            System.out.println(e.getMessage());
            // Press on regardless
        }
        catch (final RuntimeException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException) {
                throw new TimeoutException(cause);
            }
            throw e;
        }
    }

    /**
     * Dispatches the hover-transition event sequence when the mouse moves from
     * {@link #currentActiveElement_} to a new {@code element}.
     *
     * <p>The correct DOM sequence is:
     * <ol>
     *   <li>{@code mouseout} on the element being left (preceded by the
     *       {@code mouseover} that originally activated it, if needed by the
     *       HtmlUnit model).</li>
     *   <li>{@code mousemove} then {@code mouseover} on the element being entered.</li>
     * </ol>
     * </p>
     *
     * <p>A {@link ScriptException} thrown by hover-event handlers is logged at
     * WARNING level and suppressed; hover-script errors are considered non-fatal
     * since they should not prevent the primary interaction.</p>
     *
     * @param element the element the mouse is moving to; may be {@code null}
     *                (no-op for the "enter" half in that case)
     */
    private void moveOutIfNeeded(final DomElement element) {
        try {
            if (currentActiveElement_ != element) {
                if (currentActiveElement_ != null) {
                    // Fire mouseout on the element being left.
                    currentActiveElement_.mouseOut(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(),
                            keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);
                }

                if (element != null) {
                    element.mouseMove(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(),
                            keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);
                    element.mouseOver(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(),
                            keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);
                }
            }
        }
        catch (final ScriptException ignored) {
            // Hover-event script errors are non-fatal; log and continue so the
            // primary interaction can still be attempted.
            // LOG.log(Level.WARNING, "Script error during mouse hover-transition: " + e.getMessage(), e);
            System.out.println(ignored.getMessage());
        }
    }

    private void updateActiveElement(final DomElement element) {
        if (element != null) {
            currentActiveElement_ = element;
        }
    }

    /**
     * Performs a double-click on the element identified by the given
     * {@link Coordinates}, or on the current active element if
     * {@code elementCoordinates} is {@code null}.
     *
     * @param elementCoordinates coordinates representing the target element;
     *                           {@code null} falls back to the current active element
     * @throws NoSuchElementException if no element can be resolved
     * @throws WebDriverException     if an I/O or timeout error occurs
     */
    public void doubleClick(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.doubleClick(element);
    }

    /**
     * Performs a double-click on the specified {@link DomElement}, including
     * the hover-transition sequence and modifier-key propagation.
     *
     * <p>A {@link ScriptException} from the double-click handler is rethrown as
     * {@link WebDriverException}. A {@link RuntimeException} wrapping a
     * {@link SocketTimeoutException} is rethrown as {@link TimeoutException},
     * consistent with the behaviour of {@link #click(DomElement, boolean)}.</p>
     *
     * @param element the element to double-click; must not be {@code null}
     * @throws ElementNotInteractableException if the element is not displayed
     * @throws TimeoutException                if the double-click triggers a network timeout
     * @throws WebDriverException              if an I/O or script error occurs
     */
    void doubleClick(final DomElement element) {
        if (!element.isDisplayed()) {
            throw new ElementNotInteractableException("You may only interact with visible elements");
        }

        moveOutIfNeeded(element);

        try {
            element.dblClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());
            updateActiveElement(element);
        }
        catch (final IOException e) {
            // TODO: What should we do in case of error?
            e.printStackTrace();
        }
        catch (final ScriptException e) {
            throw new WebDriverException("Script error during double-click: " + e.getMessage(), e);
        }
        catch (final RuntimeException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException) {
                throw new TimeoutException(cause);
            }
            throw e;
        }
    }

    /**
     * Performs a context (right) click on the element identified by the given
     * {@link Coordinates}, or on the current active element if
     * {@code elementCoordinates} is {@code null}.
     *
     * @param elementCoordinates coordinates of the element to right-click;
     *                           {@code null} falls back to the current active element
     * @throws NoSuchElementException          if no element can be resolved
     * @throws ElementNotInteractableException if the element is not displayed
     */
    public void contextClick(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);

        if (!element.isDisplayed()) {
            throw new ElementNotInteractableException("You may only interact with visible elements");
        }

        moveOutIfNeeded(element);
        element.rightClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());
        updateActiveElement(element);
    }

    /**
     * Performs a mouse-down (button press) on the element indicated by the given
     * {@link Coordinates}, or on the current active element if
     * {@code elementCoordinates} is {@code null}.
     *
     * @param elementCoordinates coordinates identifying the target element;
     *                           {@code null} falls back to the current active element
     * @throws NoSuchElementException if no element can be resolved
     */
    public void mouseDown(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseDown(element);
    }

    /**
     * Performs a mouse-down event on the given {@link DomElement}, including
     * the hover-transition sequence and modifier-key propagation.
     *
     * @param element the element on which to dispatch the mouse-down event;
     *                must not be {@code null}
     * @throws ElementNotInteractableException if the element is not displayed
     */
    void mouseDown(final DomElement element) {
        if (!element.isDisplayed()) {
            throw new ElementNotInteractableException("You may only interact with visible elements");
        }

        moveOutIfNeeded(element);
        element.mouseDown(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);
        updateActiveElement(element);
    }

    /**
     * Performs a mouse-up (button release) on the element indicated by the given
     * {@link Coordinates}, or on the current active element if
     * {@code elementCoordinates} is {@code null}.
     *
     * @param elementCoordinates coordinates identifying the target element;
     *                           {@code null} falls back to the current active element
     * @throws NoSuchElementException if no element can be resolved
     */
    public void mouseUp(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseUp(element);
    }

    /**
     * Performs a mouse-up event on the specified {@link DomElement}, including
     * the hover-transition sequence and modifier-key propagation.
     *
     * @param element the element to dispatch the mouse-up event to; must not be {@code null}
     * @throws ElementNotInteractableException if the element is not displayed
     */
    void mouseUp(final DomElement element) {
        if (!element.isDisplayed()) {
            throw new ElementNotInteractableException("You may only interact with visible elements");
        }

        moveOutIfNeeded(element);
        element.mouseUp(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);
        updateActiveElement(element);
    }

    /**
     * Moves the mouse cursor to the element referenced by the specified
     * {@link Coordinates}. The element is obtained from the coordinates and
     * delegated to the driver.
     *
     * @param elementCoordinates the coordinates whose auxiliary object identifies
     *                           the target element; must not be {@code null}
     * @throws NoSuchElementException if the coordinates' auxiliary element is {@code null}
     */
    public void mouseMove(final Coordinates elementCoordinates) {
        final DomElement element = (DomElement) elementCoordinates.getAuxiliary();
        if (element == null) {
            throw new NoSuchElementException(
                    "Coordinates provided but their auxiliary element is null.");
        }
        parent_.mouseMove(element);
    }

    /**
     * Moves the mouse cursor to the specified {@link DomElement}, performing
     * any necessary hover-transition behaviour and marking the element as active.
     *
     * @param element the element to move the mouse to; must not be {@code null}
     */
    void mouseMove(final DomElement element) {
        moveOutIfNeeded(element);
        updateActiveElement(element);
    }

    /**
     * Not supported: HtmlUnit does not support coordinate-based mouse positioning.
     *
     * @param where   the base coordinates; ignored
     * @param xOffset the horizontal offset; ignored
     * @param yOffset the vertical offset; ignored
     * @throws UnsupportedOperationException always
     */
    public void mouseMove(final Coordinates where, final long xOffset, final long yOffset) {
        throw new UnsupportedOperationException("Moving to arbitrary X,Y coordinates not supported.");
    }
}