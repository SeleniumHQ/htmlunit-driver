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
 *
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public class HtmlUnitWindow implements WebDriver.Window {

    private static final int SCROLLBAR_WIDTH = 8;
    private static final int HEADER_HEIGHT = 150;

    /** The underlying {@link WebWindow} instance. */
    private final WebWindow webWindow_;
    /** The position of the window. */
    private final Dimension initialWindowDimension_;
    /** The initial size of the window when created. */
    private Point windowPosition_;

    /**
     * Constructs a new {@link HtmlUnitWindow} wrapping the given {@link WebWindow}.
     * Initializes the window position and initial dimensions based on the wrapped window.
     *
     * @param webWindow the {@link WebWindow} to wrap
     */
    public HtmlUnitWindow(final WebWindow webWindow) {
        webWindow_ = webWindow;
        windowPosition_ = getBasePoint();
        initialWindowDimension_ = new Dimension(webWindow_.getOuterWidth(), webWindow_.getOuterHeight());
    }

    /**
     * Returns the underlying {@link WebWindow} wrapped by this object.
     *
     * @return the wrapped {@link WebWindow} instance
     */
    public WebWindow getWebWindow() {
        return webWindow_;
    }

    @Override
    public void setSize(final Dimension targetSize) {
        final WebWindow topWindow = webWindow_.getTopWindow();

        int width = targetSize.getWidth();
        if (width < SCROLLBAR_WIDTH) {
            width = SCROLLBAR_WIDTH;
        }
        topWindow.setOuterWidth(width);
        topWindow.setInnerWidth(width - SCROLLBAR_WIDTH);

        int height = targetSize.getHeight();
        if (height < HEADER_HEIGHT) {
            height = HEADER_HEIGHT;
        }
        topWindow.setOuterHeight(height);
        topWindow.setInnerHeight(height - HEADER_HEIGHT);
    }

    @Override
    public void setPosition(final Point targetPosition) {
        windowPosition_ = targetPosition;
    }

    @Override
    public Dimension getSize() {
        final WebWindow topWindow = webWindow_.getTopWindow();
        return new Dimension(topWindow.getOuterWidth(), topWindow.getOuterHeight());
    }

    @Override
    public Point getPosition() {
        return windowPosition_;
    }

    @Override
    public void maximize() {
        setSize(initialWindowDimension_);
        setPosition(getBasePoint());
    }

    @Override
    public void minimize() {
        throw new UnsupportedOperationException("Cannot minimize window");
    }

    @Override
    public void fullscreen() {
        maximize();
    }

    /**
     * Returns the last page loaded in the wrapped {@link WebWindow}.
     *
     * @return the last {@link Page} loaded in this window
     */
    public Page lastPage() {
        return webWindow_.getEnclosedPage();
    }

    private Point getBasePoint() {
        return new Point(0, 0);
    }
}
