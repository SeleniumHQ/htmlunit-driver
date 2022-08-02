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

import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.RETURN;

/**
 * Converts a group of character sequences to a string to be sent by sendKeys.
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Rob Winch
 */
public class InputKeysContainer {
    private final StringBuilder builder_ = new StringBuilder();
    private final boolean submitKeyFound_;
    private boolean capitalize_ = false;

    public InputKeysContainer(final CharSequence... sequences) {
        this(false, sequences);
    }

    public InputKeysContainer(final boolean trimPastEnterKey, final CharSequence... sequences) {
        for (final CharSequence seq : sequences) {
            builder_.append(seq);
        }

        final int indexOfSubmitKey = indexOfSubmitKey();
        submitKeyFound_ = indexOfSubmitKey != -1;

        // If inputting keys to an input element, and the string contains one of
        // ENTER or RETURN, break the string at that point and submit the form
        if (trimPastEnterKey && (indexOfSubmitKey != -1)) {
            builder_.delete(indexOfSubmitKey, builder_.length());
        }
    }

    private int indexOfSubmitKey() {
        final CharSequence[] terminators = {"\n", ENTER, RETURN};
        for (final CharSequence terminator : terminators) {
            final String needle = String.valueOf(terminator);
            final int index = builder_.indexOf(needle);
            if (index != -1) {
                return index;
            }
        }

        return -1;
    }

    @Override
    public String toString() {
        String toReturn = builder_.toString();
        toReturn = toReturn.replaceAll(ENTER.toString(), "\r");
        toReturn = toReturn.replaceAll(RETURN.toString(), "\r");
        if (capitalize_) {
            return toReturn.toUpperCase();
        }
        return toReturn;
    }

    public boolean wasSubmitKeyFound() {
        return submitKeyFound_;
    }

    public void setCapitalization(final boolean capitalize) {
        this.capitalize_ = capitalize;
    }
}
