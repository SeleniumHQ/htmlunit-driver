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
 * An {@link HtmlUnitAction} that represents a key-up (key-release) event.
 * Releases the specified key through the driver's keyboard.
 *
 * <p>When preceded by a {@link KeyDownHtmlUnitAction} for the same key, this
 * action merges into a {@link KeySendHtmlUnitAction}, which sends the key as
 * a complete keystroke rather than as separate press/release events.</p>
 */
public final class KeyUpHtmlUnitAction implements HtmlUnitAction {
    private final String value_;

    /**
     * Creates a new key-up action for the given key value.
     *
     * @param value the key to release; must not be {@code null}
     */
    public KeyUpHtmlUnitAction(final String value) {
        value_ = value;
    }

    @Override
    public void process(final HtmlUnitDriver driver) {
        driver.getKeyboard().releaseKey(value_);
    }

    /**
     * Merges with a preceding {@link KeyDownHtmlUnitAction} for the same key
     * into a {@link KeySendHtmlUnitAction}.
     *
     * <p>{@code value_} is guaranteed non-null by the constructor; no null
     * check is required here because {@code previousAction} is never
     * {@code null} (the merge loop only calls {@code join} when there are at
     * least two entries in the queue).</p>
     */
    @Override
    public HtmlUnitAction join(final HtmlUnitAction previousAction) {
        if (previousAction instanceof KeyDownHtmlUnitAction keyDownAction) {
            if (value_.equals(keyDownAction.getValue())) {
                return new KeySendHtmlUnitAction(value_);
            }
        }
        return this;
    }
}