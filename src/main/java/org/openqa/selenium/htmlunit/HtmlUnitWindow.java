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

import org.htmlunit.Page;
import org.htmlunit.WebWindow;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

/**
 * Implements {@link WebDriver.Window} for HtmlUnit-based drivers.
 *
 * <p>Wraps a {@link WebWindow} and exposes window-management operations
 * (resize, reposition, maximize, fullscreen) in terms of HtmlUnit's
 * inner/outer width and height model.</p>
 *
 * <p><b>Frame windows:</b> size operations are always applied to the
 * <em>top-level</em> window ({@link WebWindow#getTopWindow()}), regardless
 * of whether this instance was constructed around a frame sub-window. This
 * mirrors real browser behaviour where resizing a frame does not resize the
 * enclosing window. Callers that construct an {@code HtmlUnitWindow} around a
 * frame will observe that {@link #getSize()} reflects the top-level window's
 * dimensions, not the frame's.</p>
 *
 * <p><b>Maximize / fullscreen:</b> HtmlUnit is a headless engine with no
 * physical screen. {@link #maximize()} and {@link #fullscreen()} therefore
 * restore the window to the dimensions it had when this instance was
 * constructed rather than expanding to a real screen boundary. This is an
 * intentional, documented deviation from the WebDriver specification.</p>
 *
 * <p><b>Thread safety:</b> {@link #windowPosition_} is {@code volatile} so
 * that reads and single-reference writes are visible across threads without
 * additional locking. Compound operations such as {@link #maximize()} (which
 * updates both size and position) are not atomic; callers that require strict
 * atomicity must synchronise externally.</p>
 *
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public class HtmlUnitWindow implements WebDriver.Window {

    /**
     * Number of pixels reserved for a simulated vertical scrollbar when
     * computing inner width from outer width.
     */
    private static final int SCROLLBAR_WIDTH = 8;

    /**
     * Number of pixels reserved for a simulated browser chrome/header when
     * computing inner height from outer height.
     */
    private static final int HEADER_HEIGHT = 150;

    /**
     * Shared constant for the top-left window position {@code (0, 0)}.
     * Reused by the constructor and {@link #maximize()} to avoid allocating
     * a new object on every call.
     */
    private static final Point BASE_POINT = new Point(0, 0);

    /** The underlying {@link WebWindow} instance wrapped by this object. */
    private final WebWindow webWindow_;

    /**
     * The window dimensions captured at construction time, used by
     * {@link #maximize()} to restore the window to its initial size.
     */
    private final Dimension initialWindowDimension_;

    /**
     * The simulated position of this window on screen.
     *
     * <p>Declared {@code volatile} because it may be written by the WebDriver
     * thread (via {@link #setPosition(Point)}) and read by other threads
     * (via {@link #getPosition()} or {@link #maximize()}). {@code volatile}
     * guarantees single-reference read/write visibility without a full lock.</p>
     */
    private volatile Point windowPosition_;

    /**
     * Constructs a new {@link HtmlUnitWindow} wrapping the given {@link WebWindow}.
     *
     * <p>The initial position is set to {@code (0, 0)}. The initial dimensions
     * are captured from the top-level window at construction time and are used
     * by {@link #maximize()} to restore the window.</p>
     *
     * @param webWindow the {@link WebWindow} to wrap; must not be {@code null}
     */
    public HtmlUnitWindow(final WebWindow webWindow) {
        webWindow_ = webWindow;
        windowPosition_ = BASE_POINT;
        // Capture dimensions from the top-level window to stay consistent with
        // getSize(), which also reads from the top window.
        final WebWindow topWindow = webWindow_.getTopWindow();
        initialWindowDimension_ = new Dimension(topWindow.getOuterWidth(), topWindow.getOuterHeight());
    }

    /**
     * Returns the underlying {@link WebWindow} wrapped by this object.
     *
     * @return the wrapped {@link WebWindow} instance; never {@code null}
     */
    public WebWindow getWebWindow() {
        return webWindow_;
    }

    /**
     * Resizes the top-level window to the given dimensions.
     *
     * <p>Minimum values are enforced: width is floored at {@value #SCROLLBAR_WIDTH}
     * pixels and height at {@value #HEADER_HEIGHT} pixels so that inner dimensions
     * remain non-negative.</p>
     *
     * @param targetSize the desired outer window size; must not be {@code null}
     */
    @Override
    public void setSize(final Dimension targetSize) {
        final WebWindow topWindow = webWindow_.getTopWindow();

        final int width = Math.max(targetSize.getWidth(), SCROLLBAR_WIDTH);
        topWindow.setOuterWidth(width);
        topWindow.setInnerWidth(width - SCROLLBAR_WIDTH);

        final int height = Math.max(targetSize.getHeight(), HEADER_HEIGHT);
        topWindow.setOuterHeight(height);
        topWindow.setInnerHeight(height - HEADER_HEIGHT);
    }

    /**
     * Sets the simulated on-screen position of this window.
     *
     * @param targetPosition the new window position; must not be {@code null}
     */
    @Override
    public void setPosition(final Point targetPosition) {
        windowPosition_ = targetPosition;
    }

    /**
     * Returns the current outer size of the top-level window.
     *
     * @return the outer width and height of the top window
     */
    @Override
    public Dimension getSize() {
        final WebWindow topWindow = webWindow_.getTopWindow();
        return new Dimension(topWindow.getOuterWidth(), topWindow.getOuterHeight());
    }

    /**
     * Returns the current simulated position of this window.
     *
     * @return the window's position; never {@code null}
     */
    @Override
    public Point getPosition() {
        return windowPosition_;
    }

    /**
     * Restores the window to the dimensions it had when this instance was
     * constructed and moves it to position {@code (0, 0)}.
     *
     * <p><b>Note:</b> because HtmlUnit is a headless engine with no physical
     * screen, true screen-maximization is not possible. This method restores the
     * initial dimensions as a best-effort substitute, which is a deliberate
     * deviation from the WebDriver specification.</p>
     */
    @Override
    public void maximize() {
        setSize(initialWindowDimension_);
        setPosition(BASE_POINT);
    }

    /**
     * Not supported: HtmlUnit is a headless engine and cannot minimize windows.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void minimize() {
        throw new UnsupportedOperationException("Cannot minimize window: HtmlUnit is a headless engine");
    }

    /**
     * Delegates to {@link #maximize()} as a best-effort substitute for
     * fullscreen mode.
     *
     * <p><b>Note:</b> true fullscreen requires a physical display. Because
     * HtmlUnit is headless, this method behaves identically to {@link #maximize()},
     * which is a deliberate deviation from the WebDriver specification.</p>
     */
    @Override
    public void fullscreen() {
        maximize();
    }

    /**
     * Returns the last page loaded in the wrapped {@link WebWindow}.
     *
     * @return the most recently loaded {@link Page}; may be {@code null} if
     *         no page has been loaded yet
     */
    public Page lastPage() {
        return webWindow_.getEnclosedPage();
    }
}
