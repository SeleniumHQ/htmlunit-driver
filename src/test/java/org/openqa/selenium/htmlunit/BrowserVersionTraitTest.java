package org.openqa.selenium.htmlunit;

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.htmlunit.BrowserVersionTrait.*;

import java.util.TimeZone;

import org.htmlunit.BrowserVersion;
import org.junit.Test;

public class BrowserVersionTraitTest {
    
    @Test
    public void encodeAndDecodeTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        Object encoded = SYSTEM_TIMEZONE.encode(timeZone);
        TimeZone decoded = (TimeZone) SYSTEM_TIMEZONE.decode(encoded);
        assertEquals(timeZone, decoded);
    }
    
    static void verify(final BrowserVersion expect, final BrowserVersion actual) {
        for (BrowserVersionTrait trait : BrowserVersionTrait.values()) {
            assertEquals("Browser version trait mismatch for: " + trait.key, trait.obtain(expect), trait.obtain(actual));
        }
    }
}
