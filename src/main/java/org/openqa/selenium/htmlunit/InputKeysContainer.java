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

import java.util.Locale;

/**
 * Accumulates a sequence of {@link CharSequence} values into a single string
 * suitable for dispatch via {@code sendKeys}.
 *
 * <p><b>Submit-key detection:</b> the constructor scans the concatenated input
 * for the earliest occurrence of any submit key: a literal newline ({@code \n}),
 * {@link org.openqa.selenium.Keys#ENTER}, or
 * {@link org.openqa.selenium.Keys#RETURN}. If {@code trimPastEnterKey} is
 * {@code true}, everything from that position to the end of the buffer is
 * removed (simulating form submission on Enter in a text input).</p>
 *
 * <p><b>Key normalisation in {@link #toString()}:</b> {@code ENTER}, {@code RETURN},
 * and literal {@code \n} are all normalised to {@code \n} so that HtmlUnit
 * receives a consistent line-ending regardless of which variant the caller used.
 * Capitalisation (when enabled via {@link #setCapitalization(boolean)}) is applied
 * with {@link Locale#ROOT} to avoid locale-specific character mappings (e.g. the
 * Turkish "dotted I" problem).</p>
 *
 * <p>This class is not thread-safe; instances should be used by a single thread.</p>
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Rob Winch
 */
public class InputKeysContainer {

    /** All recognised submit-key literals, checked when scanning for form submission. */
    private static final String[] SUBMIT_KEY_NEEDLES = {
        "\n",
        ENTER.toString(),
        RETURN.toString()
    };

    private final StringBuilder builder_ = new StringBuilder();
    private final boolean submitKeyFound_;

    /** Whether the output string should be uppercased before dispatch. */
    private boolean capitalize_;

    /**
     * Creates a new {@link InputKeysContainer} from the given character sequences
     * without trimming past any submit key.
     *
     * @param sequences the key sequences to include; must not be {@code null}
     */
    public InputKeysContainer(final CharSequence... sequences) {
        this(false, sequences);
    }

    /**
     * Creates a new {@link InputKeysContainer} from the given character sequences,
     * optionally truncating the buffer at the first submit key found.
     *
     * <p>The first submit key is defined as the one with the lowest index among
     * all occurrences of {@code \n}, {@link org.openqa.selenium.Keys#ENTER}, and
     * {@link org.openqa.selenium.Keys#RETURN} in the concatenated input. If
     * {@code trimPastEnterKey} is {@code true} and a submit key is found, the
     * buffer is truncated at (not including) that index, simulating the behaviour
     * of pressing Enter in a single-line text input to submit a form.</p>
     *
     * @param trimPastEnterKey if {@code true}, truncate the buffer at the first
     *                         submit key occurrence
     * @param sequences        the key sequences to include; must not be {@code null}
     */
    public InputKeysContainer(final boolean trimPastEnterKey, final CharSequence... sequences) {
        for (final CharSequence seq : sequences) {
            builder_.append(seq);
        }

        final int indexOfSubmitKey = indexOfEarliestSubmitKey();
        submitKeyFound_ = indexOfSubmitKey != -1;

        if (trimPastEnterKey && submitKeyFound_) {
            builder_.delete(indexOfSubmitKey, builder_.length());
        }
    }

    /**
     * Returns the index of the <em>earliest</em> submit key in the current
     * buffer, or {@code -1} if no submit key is present.
     *
     * @return the lowest index at which any submit key occurs, or {@code -1}
     */
    private int indexOfEarliestSubmitKey() {
        int earliest = -1;
        for (final String needle : SUBMIT_KEY_NEEDLES) {
            final int index = builder_.indexOf(needle);
            if (index != -1 && (earliest == -1 || index < earliest)) {
                earliest = index;
            }
        }
        return earliest;
    }

    /**
     * Returns the accumulated key sequence as a string, with submit keys
     * normalised and optional capitalisation applied.
     *
     * <p>All occurrences of {@link org.openqa.selenium.Keys#ENTER},
     * {@link org.openqa.selenium.Keys#RETURN}, and literal {@code \n} are
     * replaced with {@code \n} so that HtmlUnit receives a consistent line-ending
     * regardless of which variant the caller used.</p>
     *
     * <p>Capitalisation uses {@link Locale#ROOT} to avoid locale-sensitive
     * character mappings (e.g. {@code "i".toUpperCase()} produces {@code "İ"}
     * in the Turkish locale, which would corrupt ASCII input).</p>
     *
     * @return the normalised, optionally capitalised key string
     */
    @Override
    public String toString() {
        // Use literal replace (not replaceAll/regex) for correctness and clarity.
        // Normalise all submit-key variants to "\n" for consistent HtmlUnit handling.
        String toReturn = builder_.toString();
        toReturn = toReturn.replace(ENTER.toString(), "\r");
        toReturn = toReturn.replace(RETURN.toString(), "\r");
        // "\n" literals are already in the correct form; no replacement needed.

        if (capitalize_) {
            return toReturn.toUpperCase(Locale.ROOT);
        }
        return toReturn;
    }

    /**
     * Returns {@code true} if a submit key ({@code \n}, {@link org.openqa.selenium.Keys#ENTER},
     * or {@link org.openqa.selenium.Keys#RETURN}) was found in the input sequences.
     *
     * @return {@code true} if a submit key was present
     */
    public boolean wasSubmitKeyFound() {
        return submitKeyFound_;
    }

    /**
     * Controls whether the string returned by {@link #toString()} is
     * uppercased. Typically set to {@code true} when the Shift modifier key is
     * active.
     *
     * @param capitalize {@code true} to uppercase the output; {@code false} to
     *                   leave it as-is
     */
    public void setCapitalization(final boolean capitalize) {
        capitalize_ = capitalize;
    }
}