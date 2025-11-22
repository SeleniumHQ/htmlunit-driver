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

    /**
     * Creates a new {@link InputKeysContainer} containing the specified character sequences.
     * <p>
     * This constructor does not trim characters past an ENTER or RETURN key if present.
     *
     * @param sequences one or more sequences of characters to include in the container
     */
    public InputKeysContainer(final CharSequence... sequences) {
        this(false, sequences);
    }

    /**
     * Creates a new {@link InputKeysContainer} containing the specified character sequences,
     * with the option to trim content after the first ENTER or RETURN key.
     * <p>
     * If {@code trimPastEnterKey} is {@code true} and the character sequences contain an ENTER
     * or RETURN key, the container will truncate the content at the first occurrence of that key.
     *
     * @param trimPastEnterKey if {@code true}, truncate content after the first ENTER/RETURN key
     * @param sequences one or more sequences of characters to include in the container
     */
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

    /**
     * Returns whether a submit key (ENTER or RETURN) was found in the input sequences.
     *
     * @return {@code true} if a submit key was found; {@code false} otherwise
     */
    public boolean wasSubmitKeyFound() {
        return submitKeyFound_;
    }

    /**
     * Sets whether the input sequences should be capitalized.
     *
     * @param capitalize {@code true} to enable capitalization; {@code false} to disable
     */
    public void setCapitalization(final boolean capitalize) {
        capitalize_ = capitalize;
    }
}
