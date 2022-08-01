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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowNotFoundException;
import com.gargoylesoftware.htmlunit.html.BaseFrameElement;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHtml;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * HtmlUnit target locator
 */
public class HtmlUnitTargetLocator implements WebDriver.TargetLocator {
    protected final HtmlUnitDriver driver;

    public HtmlUnitTargetLocator(HtmlUnitDriver driver) {
        this.driver = driver;
    }

    @Override
    public WebDriver newWindow(WindowType typeHint) {
        return null;
    }

    @Override
    public WebDriver frame(int index) {
        Page page = driver.getCurrentWindow().lastPage();
        if (page instanceof HtmlPage) {
            try {
                driver.setCurrentWindow(((HtmlPage) page).getFrames().get(index));
            } catch (IndexOutOfBoundsException ignored) {
                throw new NoSuchFrameException("Cannot find frame: " + index);
            }
        }
        return driver;
    }

    @Override
    public WebDriver frame(final String nameOrId) {
        Page page = driver.getCurrentWindow().lastPage();
        if (page instanceof HtmlPage) {
            // First check for a frame with the matching name.
            for (final FrameWindow frameWindow : ((HtmlPage) page).getFrames()) {
                if (frameWindow.getName().equals(nameOrId)) {
                    driver.setCurrentWindow(frameWindow);
                    return driver;
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
            HtmlUnitWebElement element = (HtmlUnitWebElement) driver.findElement(By.id(nameOrId));
            DomElement domElement = element.getElement();
            if (domElement instanceof BaseFrameElement) {
                driver.setCurrentWindow(((BaseFrameElement) domElement).getEnclosedWindow());
                return driver;
            }
        } catch (NoSuchElementException ignored) {
        }

        throw new NoSuchFrameException("Unable to locate frame with name or ID: " + nameOrId);
    }

    @Override
    public WebDriver frame(WebElement frameElement) {
        while (frameElement instanceof WrapsElement) {
            frameElement = ((WrapsElement) frameElement).getWrappedElement();
        }

        HtmlUnitWebElement webElement = (HtmlUnitWebElement) frameElement;
        webElement.assertElementNotStale();

        DomElement domElement = webElement.getElement();
        if (!(domElement instanceof BaseFrameElement)) {
            throw new NoSuchFrameException(webElement.getTagName() + " is not a frame element.");
        }

        driver.setCurrentWindow(((BaseFrameElement) domElement).getEnclosedWindow());
        return driver;
    }

    @Override
    public WebDriver parentFrame() {
        driver.setCurrentWindow(driver.getCurrentWindow().getWebWindow().getParentWindow());
        return driver;
    }

    @Override
    public WebDriver window(String windowId) {
        try {
            WebWindow window = driver.getWebClient().getWebWindowByName(windowId);
            return finishSelecting(window);
        } catch (WebWindowNotFoundException e) {

            List<WebWindow> allWindows = driver.getWebClient().getWebWindows();
            for (WebWindow current : allWindows) {
                WebWindow top = current.getTopWindow();
                if (String.valueOf(System.identityHashCode(top)).equals(windowId)) {
                    return finishSelecting(top);
                }
            }
            throw new NoSuchWindowException("Cannot find window: " + windowId);
        }
    }

    private WebDriver finishSelecting(WebWindow window) {
        driver.getWebClient().setCurrentWindow(window);
        driver.setCurrentWindow(window);
        driver.getAlert().setAutoAccept(false);
        return driver;
    }

    @Override
    public WebDriver defaultContent() {
        driver.switchToDefaultContentOfWindow(driver.getCurrentWindow().getWebWindow().getTopWindow());
        return driver;
    }

    @Override
    public WebElement activeElement() {
        Page page = driver.getCurrentWindow().lastPage();
        if (page instanceof HtmlPage) {
            DomElement element = ((HtmlPage) page).getFocusedElement();
            if (element == null || element instanceof HtmlHtml) {
                List<? extends HtmlElement> allBodies = ((HtmlPage) page).getDocumentElement()
                        .getElementsByTagName("body");
                if (!allBodies.isEmpty()) {
                    return driver.toWebElement(allBodies.get(0));
                }
            } else {
                return driver.toWebElement(element);
            }
        }

        throw new NoSuchElementException("Unable to locate element with focus or body tag");
    }

    @Override
    public Alert alert() {
        final HtmlUnitAlert alert = driver.getAlert();

        if (!alert.isLocked()) {
            for (int i = 0; i < 5; i++) {
                if (!alert.isLocked()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (!alert.isLocked()) {
                driver.getCurrentWindow();
                throw new NoAlertPresentException();
            }
        }

        final WebWindow alertWindow = alert.getWebWindow();
        final WebWindow currentWindow = driver.getCurrentWindow().getWebWindow();

        if (alertWindow != currentWindow && !isChild(currentWindow, alertWindow)
                && !isChild(alertWindow, currentWindow)) {
            throw new TimeoutException();
        }
        return alert;
    }

    private static boolean isChild(WebWindow parent, WebWindow potentialChild) {
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