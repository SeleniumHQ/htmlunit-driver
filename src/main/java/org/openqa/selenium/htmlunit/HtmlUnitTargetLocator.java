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

import java.util.List;

import org.htmlunit.Page;
import org.htmlunit.WebWindow;
import org.htmlunit.WebWindowNotFoundException;
import org.htmlunit.html.BaseFrameElement;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.FrameWindow;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlHtml;
import org.htmlunit.html.HtmlPage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.WrapsElement;

/**
 * Provides target-location functionality for {@link HtmlUnitDriver}, allowing
 * navigation to frames, windows, alerts, and other browser contexts.
 *
 * <p>This implementation backs the {@link WebDriver.TargetLocator} interface
 * for HtmlUnit-based drivers. All methods delegate state changes to the owning
 * {@link HtmlUnitDriver} and are expected to be called from the WebDriver/test
 * thread.</p>
 *
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public class HtmlUnitTargetLocator implements WebDriver.TargetLocator {

    /**
     * The owning {@link HtmlUnitDriver} instance used to perform target
     * resolution and context-switching operations.
     */
    private final HtmlUnitDriver driver_;

    /**
     * Number of polling attempts made by {@link #alert()} while waiting for an
     * alert dialog to appear.
     */
    private static final int ALERT_POLL_ATTEMPTS = 5;

    /**
     * Duration in milliseconds to sleep between successive {@link #alert()}
     * polling attempts.
     */
    private static final long ALERT_POLL_SLEEP_MILLIS = 50L;

    /**
     * Creates a new {@link HtmlUnitTargetLocator} bound to the specified driver.
     *
     * @param driver the driver instance for which this target locator will
     *               perform context switching; must not be {@code null}
     */
    public HtmlUnitTargetLocator(final HtmlUnitDriver driver) {
        driver_ = driver;
    }

    /**
     * Opens a new browser window or tab and switches the driver to it.
     *
     * <p>HtmlUnit makes no distinction between windows and tabs; {@code typeHint}
     * is accepted but ignored.</p>
     *
     * @param typeHint the desired window type; ignored by this implementation
     * @return the driver, now focused on the newly opened window
     */
    @Override
    public WebDriver newWindow(final WindowType typeHint) {
        driver_.openNewWindow();
        return driver_;
    }

    /**
     * Switches focus to the frame at the given zero-based index within the
     * current page's frame list.
     *
     * @param index the zero-based index of the frame to switch to
     * @return the driver, now focused on the selected frame
     * @throws NoSuchFrameException if the current page is not an HTML page, or
     *                              if no frame exists at the given index
     */
    @Override
    public WebDriver frame(final int index) {
        final Page page = driver_.getCurrentWindow().lastPage();
        if (!(page instanceof HtmlPage)) {
            throw new NoSuchFrameException("Cannot find frame: " + index
                    + " (current page is not an HTML page)");
        }
        try {
            driver_.setCurrentWindow(((HtmlPage) page).getFrames().get(index));
        }
        catch (final IndexOutOfBoundsException ignored) {
            throw new NoSuchFrameException("Cannot find frame: " + index);
        }
        return driver_;
    }

    /**
     * Switches focus to the frame identified by the given name or ID.
     *
     * <p>The lookup order is:</p>
     * <ol>
     *   <li>Frame {@code name} attribute — searched on the current page's frame list.</li>
     *   <li>Frame {@code id} attribute — searched on the top-level document to match
     *       WebDriver spec behaviour (frame IDs are looked up in the top-level context.</li>
     * </ol>
     * If neither lookup produces a match, {@link NoSuchFrameException} is thrown.
     *
     * @param nameOrId the {@code name} or {@code id} attribute of the frame element
     * @return the driver, now focused on the matched frame
     * @throws NoSuchFrameException if no frame with the given name or ID can be found
     */
    @Override
    public WebDriver frame(final String nameOrId) {
        final Page page = driver_.getCurrentWindow().lastPage();
        if (page instanceof HtmlPage) {
            // First: match by frame name.
            for (final FrameWindow frameWindow : ((HtmlPage) page).getFrames()) {
                if (frameWindow.getName().equals(nameOrId)) {
                    driver_.setCurrentWindow(frameWindow);
                    return driver_;
                }
            }
        }

        // Second: match by frame element ID, searching the top-level document.
        // Per the WebDriver spec, the ID lookup should be performed against the
        // top-level browsing context, not the currently focused frame. This avoids
        // mistakenly finding a non-frame element with a matching ID in a nested frame.
        //
        // Users needing to switch to a frame with a non-unique ID can use the
        // WebElement overload:
        //   WebElement el = driver.findElement(By.xpath("//frame[@id='foo']"));
        //   driver.switchTo().frame(el);
        try {
            final Page topPage = driver_.getCurrentWindow().getWebWindow().getTopWindow().getEnclosedPage();
            if (topPage instanceof HtmlPage) {
                final HtmlUnitWebElement element =
                        (HtmlUnitWebElement) driver_.toWebElement(
                                ((HtmlPage) topPage).getElementById(nameOrId));
                if (element != null) {
                    final DomElement domElement = element.getElement();
                    if (domElement instanceof BaseFrameElement) {
                        driver_.setCurrentWindow(((BaseFrameElement) domElement).getEnclosedWindow());
                        return driver_;
                    }
                }
            }
        }
        catch (final NoSuchElementException ignored) {
        }

        throw new NoSuchFrameException("Unable to locate frame with name or ID: " + nameOrId);
    }

    /**
     * Switches focus to the frame represented by the given {@link WebElement}.
     * Unwraps any {@link WrapsElement} decorators before casting.
     *
     * @param frameElement a {@link WebElement} representing a {@code <frame>} or
     *                     {@code <iframe>} element; must not be {@code null}
     * @return the driver, now focused on the given frame
     * @throws NoSuchFrameException if the element is stale or is not a frame element
     */
    @Override
    public WebDriver frame(WebElement frameElement) {
        while (frameElement instanceof WrapsElement) {
            frameElement = ((WrapsElement) frameElement).getWrappedElement();
        }

        final HtmlUnitWebElement webElement = (HtmlUnitWebElement) frameElement;
        webElement.assertElementNotStale();

        final DomElement domElement = webElement.getElement();
        if (!(domElement instanceof BaseFrameElement)) {
            throw new NoSuchFrameException(webElement.getTagName() + " is not a frame element.");
        }

        driver_.setCurrentWindow(((BaseFrameElement) domElement).getEnclosedWindow());
        return driver_;
    }

    /**
     * Switches focus to the parent frame of the currently focused frame.
     * If the current context is already the top-level window, the call is a no-op
     * (HtmlUnit returns the window itself as its own parent at the top level).
     *
     * @return the driver, now focused on the parent frame
     */
    @Override
    public WebDriver parentFrame() {
        driver_.setCurrentWindow(driver_.getCurrentWindow().getWebWindow().getParentWindow());
        return driver_;
    }

    /**
     * Switches the driver to the window identified by {@code windowId}.
     *
     * <p>The lookup strategy is:</p>
     * <ol>
     *   <li>Window {@code name} — via {@link org.htmlunit.WebClient#getWebWindowByName}.</li>
     *   <li>Window handle — compared against the string representation of each top-level
     *       window's {@link System#identityHashCode}. Note that identity hash codes are
     *       not guaranteed to be globally unique; if two live top-level windows share the
     *       same hash, the first match is returned. Callers should treat window handles
     *       as opaque tokens and not construct them manually.</li>
     * </ol>
     *
     * @param windowId the window name or opaque window handle to switch to
     * @return the driver, now focused on the target window
     * @throws NoSuchWindowException if no window matching {@code windowId} can be found
     */
    @Override
    public WebDriver window(final String windowId) {
        try {
            final WebWindow window = driver_.getWebClient().getWebWindowByName(windowId);
            return finishSelecting(window);
        }
        catch (final WebWindowNotFoundException e) {
            final List<WebWindow> allWindows = driver_.getWebClient().getWebWindows();
            for (final WebWindow current : allWindows) {
                final WebWindow top = current.getTopWindow();
                if (String.valueOf(System.identityHashCode(top)).equals(windowId)) {
                    return finishSelecting(top);
                }
            }
            throw new NoSuchWindowException("Cannot find window: " + windowId);
        }
    }

    /**
     * Completes a window-selection operation by updating both the WebClient's current
     * window and the driver's current window, and re-enabling alert handling.
     *
     * @param window the window to switch to; must not be {@code null}
     * @return the driver, now focused on {@code window}
     */
    private WebDriver finishSelecting(final WebWindow window) {
        driver_.getWebClient().setCurrentWindow(window);
        driver_.setCurrentWindow(window);
        driver_.getAlert().setAutoAccept(false);
        return driver_;
    }

    /**
     * Switches the driver to the top-level document of the current window,
     * leaving any nested frame context.
     *
     * @return the driver, now focused on the default (top-level) content
     */
    @Override
    public WebDriver defaultContent() {
        driver_.switchToDefaultContentOfWindow(driver_.getCurrentWindow().getWebWindow().getTopWindow());
        return driver_;
    }

    /**
     * Returns the currently focused element, or the {@code <body>} element if
     * nothing has focus (or if the focused element is the root {@code <html>} node).
     *
     * @return the active {@link WebElement}
     * @throws NoSuchElementException if no focused element and no {@code <body>}
     *                                element can be found on the current page
     */
    @Override
    public WebElement activeElement() {
        final Page page = driver_.getCurrentWindow().lastPage();
        if (page instanceof HtmlPage) {
            final DomElement element = ((HtmlPage) page).getFocusedElement();
            if (element == null || element instanceof HtmlHtml) {
                final List<? extends HtmlElement> allBodies =
                        ((HtmlPage) page).getDocumentElement().getElementsByTagName("body");
                if (!allBodies.isEmpty()) {
                    return driver_.toWebElement(allBodies.get(0));
                }
            }
            else {
                return driver_.toWebElement(element);
            }
        }

        throw new NoSuchElementException("Unable to locate element with focus or body tag");
    }

    /**
     * Returns the currently active {@link Alert} dialog.
     *
     * <p>Because HtmlUnit's JavaScript execution is not always synchronous with
     * the WebDriver thread, this method polls for up to
     * {@value #ALERT_POLL_ATTEMPTS} × {@value #ALERT_POLL_SLEEP_MILLIS}ms before
     * concluding that no alert is present. Polling stops as soon as an alert is
     * detected or all attempts are exhausted.</p>
     *
     * <p>The interrupt flag is preserved: if {@link InterruptedException} is thrown
     * during a sleep, the flag is restored before rethrowing as
     * {@link RuntimeException}.</p>
     *
     * @return the active {@link Alert}
     * @throws NoAlertPresentException if no alert appears within the polling window
     * @throws TimeoutException        if an alert is present but belongs to a window
     *                                 that is neither the current window nor a
     *                                 parent/child of it
     */
    @Override
    public Alert alert() {
        final HtmlUnitAlert alert = driver_.getAlert();

        // Poll until the alert appears or all attempts are exhausted.
        // The loop condition is the sole check: if isLocked() becomes true
        // during a sleep, the next iteration exits immediately without sleeping.
        for (int i = 0; i < ALERT_POLL_ATTEMPTS && !alert.isLocked(); i++) {
            try {
                Thread.sleep(ALERT_POLL_SLEEP_MILLIS);
            }
            catch (final InterruptedException e) {
                // Restore the interrupt flag before rethrowing so callers that
                // inspect Thread.currentThread().isInterrupted() are not misled.
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        if (!alert.isLocked()) {
            throw new NoAlertPresentException();
        }

        final WebWindow alertWindow = alert.getWebWindow();
        final WebWindow currentWindow = driver_.getCurrentWindow().getWebWindow();

        if (alertWindow != currentWindow
                && !isChild(currentWindow, alertWindow)
                && !isChild(alertWindow, currentWindow)) {
            throw new TimeoutException(
                    "Alert belongs to a window that is not related to the current window context");
        }
        return alert;
    }

    /**
     * Determines whether {@code potentialChild} is a descendant of {@code parent}
     * in the window hierarchy.
     *
     * <p>Traversal walks up via {@link WebWindow#getParentWindow()} and stops when
     * the top window is reached (identified by {@code child == child.getTopWindow()})
     * to avoid infinite loops in circular or degenerate hierarchies.</p>
     *
     * @param parent         the candidate ancestor window
     * @param potentialChild the window whose ancestry is being tested
     * @return {@code true} if {@code potentialChild} is the same as or a descendant
     *         of {@code parent}; {@code false} otherwise
     */
    private static boolean isChild(final WebWindow parent, final WebWindow potentialChild) {
        for (WebWindow child = potentialChild; child != null; child = child.getParentWindow()) {
            if (child == parent) {
                return true;
            }
            if (child == child.getTopWindow()) {
                break;
            }
        }
        return false;
    }
}
