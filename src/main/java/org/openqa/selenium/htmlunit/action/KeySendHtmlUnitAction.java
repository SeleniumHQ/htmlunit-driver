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
 * An {@link HtmlUnitAction} representing a complete keystroke (press +
 * release). Sends the key value directly through the driver's keyboard.
 *
 * <p>Consecutive {@link KeySendHtmlUnitAction}s are merged by concatenating
 * their values, allowing multi-character type sequences to be dispatched in
 * a single {@code sendKeys} call.</p>
 */
public final class KeySendHtmlUnitAction implements HtmlUnitAction {
    private final String value_;

    /**
     * Creates a new key-send action for the given key value.
     *
     * @param value the key value to send; must not be {@code null}
     */
    public KeySendHtmlUnitAction(final String value) {
        value_ = value;
    }

    @Override
    public void process(final HtmlUnitDriver driver) {
        driver.getKeyboard().sendKeys(value_);
    }

    /**
     * Merges with a preceding {@link KeySendHtmlUnitAction} by concatenating
     * the two key values into a single action.
     */
    @Override
    public HtmlUnitAction join(final HtmlUnitAction previousAction) {
        if (previousAction instanceof KeySendHtmlUnitAction keySendAction) {
            return new KeySendHtmlUnitAction(keySendAction.value_ + value_);
        }
        return this;
    }
}