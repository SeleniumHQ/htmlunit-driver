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

package org.openqa.selenium.htmlunit.action;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * An {@link HtmlUnitAction} that represents a key-down (key-press) event.
 * Presses the specified key through the driver's keyboard.
 *
 * <p>Key-down actions are never merged with a preceding action.</p>
 */
public final class KeyDownHtmlUnitAction implements HtmlUnitAction {
    private final String value_;

    /**
     * Creates a new key-down action for the given key value.
     *
     * @param value the key to press; must not be {@code null}
     */
    public KeyDownHtmlUnitAction(final String value) {
        value_ = value;
    }

    @Override
    public void process(final HtmlUnitDriver driver) {
        driver.getKeyboard().pressKey(value_);
    }

    /** Key-down actions are never merged; always returns {@code this}. */
    @Override
    public HtmlUnitAction join(final HtmlUnitAction previousAction) {
        return this;
    }

    public String getValue() {
        return value_;
    }
}