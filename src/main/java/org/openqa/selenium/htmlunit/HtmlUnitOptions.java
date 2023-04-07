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

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.htmlunit.CookieManager;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.InvalidCookieDomainException;
import org.openqa.selenium.UnableToSetCookieException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.logging.HtmlUnitLogs;
import org.openqa.selenium.logging.Logs;

/**
 * Manages driver options.
 *
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public class HtmlUnitOptions implements WebDriver.Options {
    private final HtmlUnitLogs logs_;
    private final HtmlUnitDriver driver_;
    private final HtmlUnitTimeouts timeouts_;

    public HtmlUnitOptions(final HtmlUnitDriver driver) {
        driver_ = driver;
        logs_ = new HtmlUnitLogs(getWebClient());
        timeouts_ = new HtmlUnitTimeouts(getWebClient());
    }

    @Override
    public Logs logs() {
        return logs_;
    }

    @Override
    public void addCookie(final Cookie cookie) {
        final Page page = window().lastPage();
        if (!(page instanceof HtmlPage)) {
            throw new UnableToSetCookieException("You may not set cookies on a page that is not HTML");
        }

        final String domain = getDomainForCookie();
        verifyDomain(cookie, domain);

        getWebClient().getCookieManager().addCookie(
                new org.htmlunit.util.Cookie(
                        domain,
                        cookie.getName(),
                        cookie.getValue(),
                        cookie.getPath(),
                        cookie.getExpiry(),
                        cookie.isSecure(),
                        cookie.isHttpOnly(),
                        cookie.getSameSite()));
    }

    private void verifyDomain(final Cookie cookie, String expectedDomain) {
        String domain = cookie.getDomain();
        if (domain == null) {
            return;
        }

        if ("".equals(domain)) {
            throw new InvalidCookieDomainException("Domain must not be an empty string. Consider using null instead");
        }

        // Line-noise-tastic
        if (domain.matches(".*[^:]:\\d+$")) {
            domain = domain.replaceFirst(":\\d+$", "");
        }

        expectedDomain = expectedDomain.startsWith(".") ? expectedDomain : "." + expectedDomain;
        domain = domain.startsWith(".") ? domain : "." + domain;

        if (!expectedDomain.endsWith(domain)) {
            throw new InvalidCookieDomainException(
                    String.format("You may only add cookies that would be visible to the current domain: %s => %s",
                            domain, expectedDomain));
        }
    }

    @Override
    public Cookie getCookieNamed(final String name) {
        final Set<Cookie> allCookies = getCookies();
        for (final Cookie cookie : allCookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    @Override
    public void deleteCookieNamed(final String name) {
        final CookieManager cookieManager = getWebClient().getCookieManager();

        final URL url = getRawUrl();
        final Set<org.htmlunit.util.Cookie> rawCookies = getWebClient().getCookies(url);
        for (final org.htmlunit.util.Cookie cookie : rawCookies) {
            if (name.equals(cookie.getName())) {
                cookieManager.removeCookie(cookie);
            }
        }
    }

    @Override
    public void deleteCookie(final Cookie cookie) {
        getWebClient().getCookieManager().removeCookie(convertSeleniumCookieToHtmlUnit(cookie));
    }

    @Override
    public void deleteAllCookies() {
        final CookieManager cookieManager = getWebClient().getCookieManager();

        final URL url = getRawUrl();
        final Set<org.htmlunit.util.Cookie> rawCookies = getWebClient().getCookies(url);
        for (final org.htmlunit.util.Cookie cookie : rawCookies) {
            cookieManager.removeCookie(cookie);
        }
    }

    @Override
    public Set<Cookie> getCookies() {
        final URL url = getRawUrl();

        // The about:blank URL (the default in case no navigation took place)
        // does not have a valid 'hostname' part and cannot be used for creating
        // cookies based on it - return an empty set.

        if (!url.toString().startsWith("http")) {
            return Collections.emptySet();
        }

        final Set<Cookie> result = new HashSet<>();
        for (final org.htmlunit.util.Cookie c : getWebClient().getCookies(url)) {
            result .add(
                    new Cookie.Builder(c.getName(), c.getValue())
                    .domain(c.getDomain())
                    .path(c.getPath())
                    .expiresOn(c.getExpires())
                    .isSecure(c.isSecure())
                    .isHttpOnly(c.isHttpOnly())
                    .sameSite(c.getSameSite())
                    .build());
        }

        return Collections.unmodifiableSet(result);
    }

    private org.htmlunit.util.Cookie convertSeleniumCookieToHtmlUnit(final Cookie cookie) {
        return new org.htmlunit.util.Cookie(
                cookie.getDomain(),
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getExpiry(),
                cookie.isSecure(),
                cookie.isHttpOnly(),
                cookie.getSameSite());
    }

    private String getDomainForCookie() {
        final URL current = getRawUrl();
        return current.getHost();
    }

    private WebClient getWebClient() {
        return driver_.getWebClient();
    }

    @Override
    public WebDriver.Timeouts timeouts() {
        return timeouts_;
    }

    @Override
    public HtmlUnitWindow window() {
        return driver_.getCurrentWindow();
    }

    private URL getRawUrl() {
        return Optional.ofNullable(window().lastPage()).map(Page::getUrl).orElse(null);
    }
}
