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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class HtmlUnitWindow implements WebDriver.Window {

    private final int SCROLLBAR_WIDTH = 8;
    private final int HEADER_HEIGHT = 150;
    private final HtmlUnitDriver driver;
    private final Dimension initialWindowDimension;

    public HtmlUnitWindow(HtmlUnitDriver driver) {
        this.driver = driver;
        this.initialWindowDimension = new Dimension(driver.getCurrentWindow().getOuterWidth(), driver.getCurrentWindow().getOuterHeight());
        initWindow();
    }

    private void initWindow() {
        getWebClient().addWebWindowListener(new WebWindowListener() {
            @Override
            public void webWindowOpened(WebWindowEvent webWindowEvent) {
                // Ignore
            }

            @Override
            public void webWindowContentChanged(WebWindowEvent event) {
                driver.getElementsMap().remove(event.getOldPage());
                WebWindow current = driver.getCurrentWindow();

                if (current == event.getWebWindow()) {
                    switchToDefaultContentOfWindow(current);
                }
            }

            @Override
            public void webWindowClosed(WebWindowEvent event) {
                driver.getElementsMap().remove(event.getOldPage());

                WebWindow current = getWebClient().getCurrentWindow();
                do {
                    // Instance equality is okay in this case
                    if (current == event.getWebWindow()) {
                        getWebClient().setCurrentWindow(current.getTopWindow());
                        return;
                    }
                    current = current.getParentWindow();
                } while (current != getWebClient().getCurrentWindow().getTopWindow());
            }
        });
    }

    private WebClient getWebClient() {
        return driver.getWebClient();
    }

    @Override
    public void setSize(Dimension targetSize) {
        WebWindow topWindow = driver.getCurrentWindow().getTopWindow();

        int width = targetSize.getWidth();
        if (width < SCROLLBAR_WIDTH) width = SCROLLBAR_WIDTH;
        topWindow.setOuterWidth(width);
        topWindow.setInnerWidth(width - SCROLLBAR_WIDTH);

        int height = targetSize.getHeight();
        if (height < HEADER_HEIGHT) height = HEADER_HEIGHT;
        topWindow.setOuterHeight(height);
        topWindow.setInnerHeight(height - HEADER_HEIGHT);
    }

    @Override
    public void setPosition(Point targetPosition) {
        //nop
    }

    @Override
    public Dimension getSize() {
        WebWindow topWindow = driver.getCurrentWindow().getTopWindow();
        return new Dimension(topWindow.getOuterWidth(), topWindow.getOuterHeight());
    }

    @Override
    public Point getPosition() {
        return getFictivePoint();
    }

    @Override
    public void maximize() {
        setSize(initialWindowDimension);
    }

    @Override
    public void minimize() {
        throw new UnsupportedOperationException("Cannot minimize window");
    }

    @Override
    public void fullscreen() {
        maximize();
    }

    public Page lastPage() {
        return driver.getCurrentWindow().getEnclosedPage();
    }

    protected void switchToDefaultContentOfWindow(WebWindow window) {
        Page page = window.getEnclosedPage();
        if (page instanceof HtmlPage) {
            driver.setCurrentWindow(window);
        }
    }

    private Point getFictivePoint() {
        return new Point(0, 0);
    }
}