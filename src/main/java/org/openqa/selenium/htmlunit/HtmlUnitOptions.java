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

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.InvalidCookieDomainException;
import org.openqa.selenium.UnableToSetCookieException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.logging.HtmlUnitLogs;
import org.openqa.selenium.logging.Logs;

import java.net.URL;
import java.util.Optional;
import java.util.Set;

/**
 * Manages driver options
 */
public class HtmlUnitOptions implements WebDriver.Options {
    private final HtmlUnitLogs logs;
    private final HtmlUnitDriver driver;
    private final HtmlUnitTimeouts timeouts;
    private final HtmlUnitWindow window;

    public HtmlUnitOptions(HtmlUnitDriver driver) {
        this.driver = driver;
        this.logs = new HtmlUnitLogs(getWebClient());
        this.timeouts = new HtmlUnitTimeouts(getWebClient());
        this.window = driver.getWindowManager();
    }

    @Override
    public Logs logs() {
        return logs;
    }

    @Override
    public void addCookie(Cookie cookie) {
        Page page = window.lastPage();
        if (!(page instanceof HtmlPage)) {
            throw new UnableToSetCookieException("You may not set cookies on a page that is not HTML");
        }

        String domain = getDomainForCookie();
        verifyDomain(cookie, domain);

        getWebClient().getCookieManager().addCookie(
                new com.gargoylesoftware.htmlunit.util.Cookie(domain, cookie.getName(),
                        cookie.getValue(),
                        cookie.getPath(), cookie.getExpiry(), cookie.isSecure()));
    }

    private void verifyDomain(Cookie cookie, String expectedDomain) {
        String domain = cookie.getDomain();
        if (domain == null) {
            return;
        }

        if ("".equals(domain)) {
            throw new InvalidCookieDomainException(
                    "Domain must not be an empty string. Consider using null instead");
        }

        // Line-noise-tastic
        if (domain.matches(".*[^:]:\\d+$")) {
            domain = domain.replaceFirst(":\\d+$", "");
        }

        expectedDomain = expectedDomain.startsWith(".") ? expectedDomain : "." + expectedDomain;
        domain = domain.startsWith(".") ? domain : "." + domain;

        if (!expectedDomain.endsWith(domain)) {
            throw new InvalidCookieDomainException(
                    String.format(
                            "You may only add cookies that would be visible to the current domain: %s => %s",
                            domain, expectedDomain));
        }
    }

    @Override
    public Cookie getCookieNamed(String name) {
        Set<Cookie> allCookies = getCookies();
        for (Cookie cookie : allCookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    @Override
    public void deleteCookieNamed(String name) {
        CookieManager cookieManager = getWebClient().getCookieManager();

        URL url = getRawUrl();
        Set<com.gargoylesoftware.htmlunit.util.Cookie> rawCookies = getWebClient().getCookies(url);
        for (com.gargoylesoftware.htmlunit.util.Cookie cookie : rawCookies) {
            if (name.equals(cookie.getName())) {
                cookieManager.removeCookie(cookie);
            }
        }
    }

    @Override
    public void deleteCookie(Cookie cookie) {
        getWebClient().getCookieManager().removeCookie(convertSeleniumCookieToHtmlUnit(cookie));
    }

    @Override
    public void deleteAllCookies() {
        CookieManager cookieManager = getWebClient().getCookieManager();

        URL url = getRawUrl();
        Set<com.gargoylesoftware.htmlunit.util.Cookie> rawCookies = getWebClient().getCookies(url);
        for (com.gargoylesoftware.htmlunit.util.Cookie cookie : rawCookies) {
            cookieManager.removeCookie(cookie);
        }
    }

    @Override
    public Set<Cookie> getCookies() {
        URL url = getRawUrl();

        // The about:blank URL (the default in case no navigation took place)
        // does not have a valid 'hostname' part and cannot be used for creating
        // cookies based on it - return an empty set.

        if (!url.toString().startsWith("http")) {
            return Sets.newHashSet();
        }

        return ImmutableSet.copyOf(Collections2.transform(
                getWebClient().getCookies(url),
                htmlUnitCookieToSeleniumCookieTransformer::apply));
    }

    private com.gargoylesoftware.htmlunit.util.Cookie convertSeleniumCookieToHtmlUnit(Cookie cookie) {
        return new com.gargoylesoftware.htmlunit.util.Cookie(
                cookie.getDomain(),
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getExpiry(),
                cookie.isSecure(),
                cookie.isHttpOnly()
        );
    }

    private final java.util.function.Function<? super com.gargoylesoftware.htmlunit.util.Cookie, @Nullable Cookie> htmlUnitCookieToSeleniumCookieTransformer =
            (Function<com.gargoylesoftware.htmlunit.util.Cookie, Cookie>) c -> new Cookie.Builder(c.getName(), c.getValue())
                    .domain(c.getDomain())
                    .path(c.getPath())
                    .expiresOn(c.getExpires())
                    .isSecure(c.isSecure())
                    .isHttpOnly(c.isHttpOnly())
                    .build();

    private String getDomainForCookie() {
        URL current = getRawUrl();
        return current.getHost();
    }

    private WebClient getWebClient() {
        return driver.getWebClient();
    }

    @Override
    public WebDriver.Timeouts timeouts() {
        return timeouts;
    }

    @Override
    public WebDriver.ImeHandler ime() {
        throw new UnsupportedOperationException("Cannot input IME using HtmlUnit.");
    }

    @Override
    public WebDriver.Window window() {
        return window;
    }

    private URL getRawUrl() {
        return Optional.ofNullable(window.lastPage())
                .map(Page::getUrl)
                .orElse(null);
    }
}