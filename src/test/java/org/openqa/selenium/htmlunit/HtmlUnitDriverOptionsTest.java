package org.openqa.selenium.htmlunit;

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.htmlunit.HtmlUnitOption.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.htmlunit.BrowserVersion;
import org.htmlunit.util.UrlUtils;
import org.junit.Test;
import org.openqa.selenium.remote.Browser;

/**
 * HtmlUnitDriverOptions tests.
 *
 * @author Scott Babcock
 */
public class HtmlUnitDriverOptionsTest {
    
    @Test
    public void newOptionsWithoutArguments() {
        HtmlUnitDriverOptions options = new HtmlUnitDriverOptions();
        verifyOptions(options, BrowserVersion.BEST_SUPPORTED);
    }

    @Test
    public void newOptionsWithChromeVersion() {
        HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.CHROME);
        verifyOptions(options, BrowserVersion.CHROME);
    }

    @Test
    public void newOptionsWithEdgeVersion() {
        HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.EDGE);
        verifyOptions(options, BrowserVersion.EDGE);
    }

    @Test
    public void newOptionsWithFirefoxVersion() {
        HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.FIREFOX);
        verifyOptions(options, BrowserVersion.FIREFOX);
    }

    @Test
    public void newOptionsWithFirefoxESRVersion() {
        HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.FIREFOX_ESR);
        verifyOptions(options, BrowserVersion.FIREFOX_ESR);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void newOptionsWithInternetExplorerVersion() {
        HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.INTERNET_EXPLORER);
        verifyOptions(options, BrowserVersion.INTERNET_EXPLORER);
    }
    
    @Test
    public void verifyEncodeAndDecode() {
        HtmlUnitDriverOptions options = new HtmlUnitDriverOptions();
        Map<String, Object> optionsMap = options.asMap();
        HtmlUnitDriverOptions decoded = new HtmlUnitDriverOptions(optionsMap);
        assertEquals("", options, decoded);
    }

    private void verifyOptions(final HtmlUnitDriverOptions options, final BrowserVersion browserVersion) {
        assertEquals("Browser name mismatch", Browser.HTMLUNIT.browserName(), options.getBrowserName());
        BrowserVersionTraitTest.verify(browserVersion, options.getWebClientVersion());

        Map<HtmlUnitOption, Object> nonDefault = getNonDefaultOptions(options);
        Set<HtmlUnitOption> expectKeySet = Set.of(WEB_CLIENT_VERSION, HOME_PAGE, PRINT_CONTENT_ON_FAILING_STATUS_CODE,
                THROW_EXCEPTION_ON_FAILING_STATUS_CODE, USE_INSECURE_SSL);

        assertEquals("Non-default value set mismatch", expectKeySet, nonDefault.keySet());

        BrowserVersion decodedVersion = (BrowserVersion) WEB_CLIENT_VERSION.decode(nonDefault.get(WEB_CLIENT_VERSION));
        BrowserVersionTraitTest.verify(browserVersion, decodedVersion);

        assertEquals("Mismatch for option: homePage", UrlUtils.URL_ABOUT_BLANK.toString(), nonDefault.get(HOME_PAGE));
        assertEquals("Mismatch for option: printContentOnFailingStatusCode", false,
                nonDefault.get(PRINT_CONTENT_ON_FAILING_STATUS_CODE));
        assertEquals("Mismatch for option: throwExceptionOnFailingStatusCode", false,
                nonDefault.get(THROW_EXCEPTION_ON_FAILING_STATUS_CODE));
        assertEquals("Mismatch for option: useInsecureSSL", true, nonDefault.get(USE_INSECURE_SSL));
    }
    
    @SuppressWarnings("unchecked")
    private static Map<HtmlUnitOption, Object> getNonDefaultOptions(final HtmlUnitDriverOptions options) {
        Map<HtmlUnitOption, Object> result = new HashMap<>();
        Map<String, Object> extraOptions = (Map<String, Object>) options.getExtraCapability(HtmlUnitDriverOptions.HTMLUNIT_OPTIONS);
        for (HtmlUnitOption option : HtmlUnitOption.values()) {
            if (extraOptions.containsKey(option.key)) {
                result.put(option, extraOptions.get(option.key));
            }
        }
        return result;
    }
}
