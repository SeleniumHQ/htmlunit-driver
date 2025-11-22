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

import java.time.Duration;

import org.htmlunit.WebClient;
import org.openqa.selenium.WebDriver;

/**
 * Implements the {@link WebDriver.Timeouts} interface for HtmlUnit.
 * <p>
 * This class stores and manages timeout values used by HtmlUnit-based
 * WebDriver instances, including implicit wait time, script execution
 * timeout, and page load timeout. When a {@link WebClient} is provided,
 * these values may be applied directly to the underlying client.
 *
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public class HtmlUnitTimeouts implements WebDriver.Timeouts {
    /**
     * The amount of time (in milliseconds) the driver should wait when
     * searching for elements that are not immediately present.
     * <p>
     * Defaults to {@code 0}, meaning no implicit wait.
     */
    private long implicitWait_ = 0;

    /**
     * The timeout (in milliseconds) for asynchronous script execution.
     * <p>
     * Defaults to {@code 30_000} (30 seconds).
     */
    private long scriptTimeout_ = 30 * 1000;

    /**
     * The amount of time (in milliseconds) to wait for a page load to complete.
     * <p>
     * Defaults to {@code 300_000} (5 minutes).
     */
    private long pageLoadTimeout_ = 300 * 1000;

    /**
     * The underlying HtmlUnit {@link WebClient} associated with this timeout
     * configuration. May be {@code null} if the instance was constructed
     * without a client.
     */
    private WebClient webClient_ = null;

    /**
     * Default constructor for {@link HtmlUnitTimeouts}.
     * <p>
     * Creates an instance without an associated {@link WebClient}.
     * All timeouts will need to be configured later if required.
     */
    public HtmlUnitTimeouts() {
        // nop
    }

    /**
     * Constructs an {@link HtmlUnitTimeouts} instance associated with the given {@link WebClient}.
     *
     * @param webClient the {@link WebClient} whose timeouts this instance will manage
     */
    public HtmlUnitTimeouts(final WebClient webClient) {
        webClient_ = webClient;
    }

    @Override
    public Duration getImplicitWaitTimeout() {
        return Duration.ofMillis(implicitWait_);
    }

    @Override
    public WebDriver.Timeouts implicitlyWait(final Duration duration) {
        implicitWait_ = Math.max(0, duration.toMillis());
        return this;
    }

    @Override
    public WebDriver.Timeouts scriptTimeout(final Duration duration) {
        scriptTimeout_ = duration.toMillis();
        return this;
    }

    @Override
    public Duration getScriptTimeout() {
        return Duration.ofMillis(scriptTimeout_);
    }

    @Override
    public WebDriver.Timeouts pageLoadTimeout(final Duration duration) {
        pageLoadTimeout_ = duration.toMillis();
        setPageLoadTimeoutForWebClient(webClient_, pageLoadTimeout_);
        return this;
    }

    @Override
    public Duration getPageLoadTimeout() {
        return Duration.ofMillis(pageLoadTimeout_);
    }

    private static void setPageLoadTimeoutForWebClient(final WebClient webClient, final long timeout) {
        if (webClient != null) {
            webClient.getOptions().setTimeout(Math.max((int) timeout, 0));
        }
    }
}
