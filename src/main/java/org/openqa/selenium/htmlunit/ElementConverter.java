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

import com.gargoylesoftware.htmlunit.html.DomElement;
import org.openqa.selenium.By;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;

import static org.openqa.selenium.htmlunit.HtmlUnitElementFinder.FindByClassName;
import static org.openqa.selenium.htmlunit.HtmlUnitElementFinder.FindByCssSelector;
import static org.openqa.selenium.htmlunit.HtmlUnitElementFinder.FindByID;
import static org.openqa.selenium.htmlunit.HtmlUnitElementFinder.FindByLinkText;
import static org.openqa.selenium.htmlunit.HtmlUnitElementFinder.FindByName;
import static org.openqa.selenium.htmlunit.HtmlUnitElementFinder.FindByPartialLinkText;
import static org.openqa.selenium.htmlunit.HtmlUnitElementFinder.FindByTagName;
import static org.openqa.selenium.htmlunit.HtmlUnitElementFinder.FindByXPath;
import static org.openqa.selenium.htmlunit.HtmlUnitWebElementFinder.WebFindByCssSelector;
import static org.openqa.selenium.htmlunit.HtmlUnitWebElementFinder.WebFindById;
import static org.openqa.selenium.htmlunit.HtmlUnitWebElementFinder.WebFindByLinkText;
import static org.openqa.selenium.htmlunit.HtmlUnitWebElementFinder.WebFindByPartialLinkText;
import static org.openqa.selenium.htmlunit.HtmlUnitWebElementFinder.WebFindByXPath;
import static org.openqa.selenium.htmlunit.HtmlUnitWebElementFinder.WebFindByName;
import static org.openqa.selenium.htmlunit.HtmlUnitWebElementFinder.WebFindByTagName;
import static org.openqa.selenium.htmlunit.HtmlUnitWebElementFinder.WebFindByClassName;

/**
 * Used for converting By locators to custom ones
 */
public class ElementConverter {

    /**
     * Interface for Element converters
     */
    interface Converter {
        Class<? extends By> getMappedClass();

        Class<? extends HtmlUnitElementFinder.ElementObject> getTargetClass();
    }

    /**
     * Converter for HtmlUnit elements
     */
    public enum HtmlUnitElementConverter implements Converter {
        ID(By.ById.class, FindByID.class),
        NAME(By.ByName.class, FindByName.class),
        LINK_TEXT(By.ByLinkText.class, FindByLinkText.class),
        PARTIAL_LINK_TEXT(By.ByPartialLinkText.class, FindByPartialLinkText.class),
        CLASS_NAME(By.ByClassName.class, FindByClassName.class),
        CSS_SELECTOR(By.ByCssSelector.class, FindByCssSelector.class),
        TAG_NAME(By.ByTagName.class, FindByTagName.class),
        XPATH(By.ByXPath.class, FindByXPath.class);

        private final Class<? extends By> mappedClass;
        private final Class<? extends HtmlUnitElementFinder.ElementObject> targetClass;

        HtmlUnitElementConverter(Class<? extends By> mappedClass, Class<? extends HtmlUnitElementFinder.ElementObject> targetClass) {
            this.mappedClass = mappedClass;
            this.targetClass = targetClass;
        }

        public static HtmlUnitElementFinder.ElementObject getElementObject(HtmlUnitDriver driver, By locator) {
            HtmlUnitElementConverter object = (HtmlUnitElementConverter) getConverter(locator, HtmlUnitElementConverter.values());

            try {
                return object.getTargetClass().getConstructor(HtmlUnitDriver.class).newInstance(driver);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Cannot convert By instance to custom implementation", e);
            }
        }

        @Override
        public Class<? extends By> getMappedClass() {
            return mappedClass;
        }

        @Override
        public Class<? extends HtmlUnitElementFinder.ElementObject> getTargetClass() {
            return targetClass;
        }
    }

    /**
     * Converter for HtmlUnit elements
     */
    public enum HtmlUnitWebElementConverter implements Converter {
        ID(By.ById.class, WebFindById.class),
        NAME(By.ByName.class, WebFindByName.class),
        LINK_TEXT(By.ByLinkText.class, WebFindByLinkText.class),
        PARTIAL_LINK_TEXT(By.ByPartialLinkText.class, WebFindByPartialLinkText.class),
        CLASS_NAME(By.ByClassName.class, WebFindByClassName.class),
        CSS_SELECTOR(By.ByCssSelector.class, WebFindByCssSelector.class),
        TAG_NAME(By.ByTagName.class, WebFindByTagName.class),
        XPATH(By.ByXPath.class, WebFindByXPath.class);

        private final Class<? extends By> mappedClass;
        private final Class<? extends HtmlUnitElementFinder.ElementObject> targetClass;

        HtmlUnitWebElementConverter(Class<? extends By> mappedClass, Class<? extends HtmlUnitElementFinder.ElementObject> targetClass) {
            this.mappedClass = mappedClass;
            this.targetClass = targetClass;
        }

        public static HtmlUnitElementFinder.ElementObject getElementObject(HtmlUnitDriver driver, DomElement element, By locator) {
            HtmlUnitWebElementConverter object = (HtmlUnitWebElementConverter) getConverter(locator, HtmlUnitWebElementConverter.values());

            try {
                return object.getTargetClass().getConstructor(HtmlUnitDriver.class, DomElement.class).newInstance(driver, element);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return HtmlUnitElementConverter.getElementObject(driver, locator);
            }
        }

        @Override
        public Class<? extends By> getMappedClass() {
            return mappedClass;
        }

        @Override
        public Class<? extends HtmlUnitElementFinder.ElementObject> getTargetClass() {
            return targetClass;
        }
    }

    private static Converter getConverter(By locator, Converter[] array) {
        return Arrays.stream(array)
                .filter(Objects::nonNull)
                .filter(f -> locator.getClass().equals(f.getMappedClass()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find element object"));
    }
}
