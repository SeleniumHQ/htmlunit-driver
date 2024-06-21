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

package org.openqa.selenium.htmlunit.options;

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.htmlunit.options.BrowserVersionTrait.SYSTEM_TIMEZONE;

import java.util.TimeZone;

import org.htmlunit.BrowserVersion;
import org.junit.Test;

/**
 * @author Scott Babcock
 */
public class BrowserVersionTraitTest {

    @Test
    public void encodeAndDecodeTimeZone() {
        final TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        final Object encoded = SYSTEM_TIMEZONE.encode(timeZone);
        final TimeZone decoded = (TimeZone) SYSTEM_TIMEZONE.decode(encoded);
        assertEquals(timeZone, decoded);
    }

    static void verify(final BrowserVersion expect, final BrowserVersion actual) {
        for (final BrowserVersionTrait trait : BrowserVersionTrait.values()) {
            assertEquals("Browser version trait mismatch for: "
                    + trait.getCapabilityKey(), trait.obtain(expect), trait.obtain(actual));
        }
    }
}
