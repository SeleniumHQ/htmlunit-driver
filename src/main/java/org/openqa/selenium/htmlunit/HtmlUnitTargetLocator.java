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
import org.openqa.selenium.By;
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
 * HtmlUnit target locator.
 *
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public class HtmlUnitTargetLocator implements WebDriver.TargetLocator {
    private final HtmlUnitDriver driver_;

    public HtmlUnitTargetLocator(final HtmlUnitDriver driver) {
        driver_ = driver;
    }

    @Override
    public WebDriver newWindow(final WindowType typeHint) {
        // there is no difference between window and tab in htmlunit
        driver_.openNewWindow();
        return driver_;
    }

    @Override
    public WebDriver frame(final int index) {
        final Page page = driver_.getCurrentWindow().lastPage();
        if (page instanceof HtmlPage) {
            try {
                driver_.setCurrentWindow(((HtmlPage) page).getFrames().get(index));
            }
            catch (final IndexOutOfBoundsException ignored) {
                throw new NoSuchFrameException("Cannot find frame: " + index);
            }
        }
        return driver_;
    }

    @Override
    public WebDriver frame(final String nameOrId) {
        final Page page = driver_.getCurrentWindow().lastPage();
        if (page instanceof HtmlPage) {
            // First check for a frame with the matching name.
            for (final FrameWindow frameWindow : ((HtmlPage) page).getFrames()) {
                if (frameWindow.getName().equals(nameOrId)) {
                    driver_.setCurrentWindow(frameWindow);
                    return driver_;
                }
            }
        }

        // Next, check for a frame with a matching ID. For simplicity, assume the ID is
        // unique.
        // Users can still switch to frames with non-unique IDs using a WebElement
        // switch:
        // WebElement frameElement =
        // driver.findElement(By.xpath("//frame[@id=\"foo\"]"));
        // driver.switchTo().frame(frameElement);
        try {
            final HtmlUnitWebElement element = (HtmlUnitWebElement) driver_.findElement(By.id(nameOrId));
            final DomElement domElement = element.getElement();
            if (domElement instanceof BaseFrameElement) {
                driver_.setCurrentWindow(((BaseFrameElement) domElement).getEnclosedWindow());
                return driver_;
            }
        }
        catch (final NoSuchElementException ignored) {
        }

        throw new NoSuchFrameException("Unable to locate frame with name or ID: " + nameOrId);
    }

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

    @Override
    public WebDriver parentFrame() {
        driver_.setCurrentWindow(driver_.getCurrentWindow().getWebWindow().getParentWindow());
        return driver_;
    }

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

    private WebDriver finishSelecting(final WebWindow window) {
        driver_.getWebClient().setCurrentWindow(window);
        driver_.setCurrentWindow(window);
        driver_.getAlert().setAutoAccept(false);
        return driver_;
    }

    @Override
    public WebDriver defaultContent() {
        driver_.switchToDefaultContentOfWindow(driver_.getCurrentWindow().getWebWindow().getTopWindow());
        return driver_;
    }

    @Override
    public WebElement activeElement() {
        final Page page = driver_.getCurrentWindow().lastPage();
        if (page instanceof HtmlPage) {
            final DomElement element = ((HtmlPage) page).getFocusedElement();
            if (element == null || element instanceof HtmlHtml) {
                final List<? extends HtmlElement> allBodies = ((HtmlPage) page).getDocumentElement()
                        .getElementsByTagName("body");
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

    @Override
    public Alert alert() {
        final HtmlUnitAlert alert = driver_.getAlert();

        if (!alert.isLocked()) {
            for (int i = 0; i < 5; i++) {
                if (!alert.isLocked()) {
                    try {
                        Thread.sleep(50);
                    }
                    catch (final InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (!alert.isLocked()) {
                driver_.getCurrentWindow();
                throw new NoAlertPresentException();
            }
        }

        final WebWindow alertWindow = alert.getWebWindow();
        final WebWindow currentWindow = driver_.getCurrentWindow().getWebWindow();

        if (alertWindow != currentWindow && !isChild(currentWindow, alertWindow)
                && !isChild(alertWindow, currentWindow)) {
            throw new TimeoutException();
        }
        return alert;
    }

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
