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
 *
 * <p>Stores and manages timeout values used by HtmlUnit-based WebDriver
 * instances: implicit wait, asynchronous script execution timeout, and page
 * load timeout. When a {@link WebClient} is provided, the page load timeout
 * is propagated to the underlying client immediately upon change.</p>
 *
 * <p><b>Thread safety:</b> this class is not thread-safe. All timeout mutations
 * are expected to be performed from the WebDriver/test thread. Propagating
 * {@link #pageLoadTimeout(Duration)} to the shared {@link WebClient} is not
 * synchronised within this class; callers that share a {@link WebClient} across
 * threads must coordinate externally.</p>
 *
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public class HtmlUnitTimeouts implements WebDriver.Timeouts {

    /**
     * Default implicit-wait timeout: {@code 0} ms (no implicit wait).
     */
    private static final long DEFAULT_IMPLICIT_WAIT_MS = 0L;

    /**
     * Default asynchronous script execution timeout: {@code 30 000} ms (30 seconds).
     */
    private static final long DEFAULT_SCRIPT_TIMEOUT_MS = Duration.ofSeconds(30).toMillis();

    /**
     * Default page load timeout: {@code 300 000} ms (5 minutes).
     */
    private static final long DEFAULT_PAGE_LOAD_TIMEOUT_MS = Duration.ofMinutes(5).toMillis();

    /**
     * The amount of time (in milliseconds) the driver should wait when
     * searching for elements that are not immediately present.
     * Always {@code >= 0}.
     */
    private long implicitWait_ = DEFAULT_IMPLICIT_WAIT_MS;

    /**
     * The timeout (in milliseconds) for asynchronous script execution.
     * Always {@code >= 0}.
     */
    private long scriptTimeout_ = DEFAULT_SCRIPT_TIMEOUT_MS;

    /**
     * The amount of time (in milliseconds) to wait for a page load to complete.
     * Always {@code >= 0}. Also propagated to {@link #webClient_} whenever it
     * changes, so the two must remain consistent.
     */
    private long pageLoadTimeout_ = DEFAULT_PAGE_LOAD_TIMEOUT_MS;

    /**
     * The underlying HtmlUnit {@link WebClient} whose connection/page-load timeout
     * is kept in sync with {@link #pageLoadTimeout_}.
     * May be {@code null} when this instance was created without a client.
     */
    private final WebClient webClient_;

    /**
     * Constructs an {@link HtmlUnitTimeouts} instance without an associated
     * {@link WebClient}. All timeout fields are initialised to their defaults
     * and are ready to use immediately; no further configuration is required.
     *
     * <p>Changes to {@link #pageLoadTimeout(Duration)} will not be propagated
     * to any {@link WebClient} when this constructor is used.</p>
     */
    public HtmlUnitTimeouts() {
        webClient_ = null;
    }

    /**
     * Constructs an {@link HtmlUnitTimeouts} instance associated with the given
     * {@link WebClient}. All timeout fields are initialised to their defaults.
     *
     * <p>Changes to {@link #pageLoadTimeout(Duration)} are propagated to
     * {@code webClient} immediately. The initial default page load timeout is
     * also applied to the client at construction time so that the client and
     * this instance start in a consistent state.</p>
     *
     * @param webClient the {@link WebClient} whose page-load timeout this
     *                  instance will keep in sync; must not be {@code null}
     */
    public HtmlUnitTimeouts(final WebClient webClient) {
        webClient_ = webClient;
        // Synchronise the client with the default value so the two are
        // consistent from the very first use.
        applyPageLoadTimeoutToClient(pageLoadTimeout_);
    }

    @Override
    public Duration getImplicitWaitTimeout() {
        return Duration.ofMillis(implicitWait_);
    }

    /**
     * Sets the implicit wait timeout.
     *
     * @param duration the duration to wait; negative values are clamped to zero
     * @return this instance, for chaining
     */
    @Override
    public WebDriver.Timeouts implicitlyWait(final Duration duration) {
        implicitWait_ = Math.max(0L, duration.toMillis());
        return this;
    }

    /**
     * Sets the asynchronous script execution timeout.
     *
     * @param duration the maximum time to wait for a script to finish;
     *                 negative values are clamped to zero so that
     *                 {@link AsyncScriptExecutor} never receives a negative
     *                 timeout (which would cause {@link IllegalArgumentException}
     *                 deep in {@link java.util.concurrent.CountDownLatch#await})
     * @return this instance, for chaining
     */
    @Override
    public WebDriver.Timeouts scriptTimeout(final Duration duration) {
        scriptTimeout_ = Math.max(0L, duration.toMillis());
        return this;
    }

    @Override
    public Duration getScriptTimeout() {
        return Duration.ofMillis(scriptTimeout_);
    }

    /**
     * Sets the page load timeout and propagates it to the underlying
     * {@link WebClient} if one is associated with this instance.
     *
     * @param duration the maximum time to wait for a page load to complete;
     *                 negative values are clamped to zero
     * @return this instance, for chaining
     */
    @Override
    public WebDriver.Timeouts pageLoadTimeout(final Duration duration) {
        pageLoadTimeout_ = Math.max(0L, duration.toMillis());
        applyPageLoadTimeoutToClient(pageLoadTimeout_);
        return this;
    }

    @Override
    public Duration getPageLoadTimeout() {
        return Duration.ofMillis(pageLoadTimeout_);
    }

    /**
     * Propagates the given page load timeout (in milliseconds) to the
     * associated {@link WebClient}, if one is present.
     *
     * <p>The WebClient's {@link org.htmlunit.WebClientOptions#setTimeout} method
     * accepts an {@code int}. To prevent silent truncation from the {@code long}→
     * {@code int} narrowing conversion, the value is clamped to
     * {@link Integer#MAX_VALUE} before casting. Values larger than
     * {@code Integer.MAX_VALUE} ms (≈ 24.8 days) are therefore treated as the
     * maximum representable finite timeout rather than wrapping to a negative
     * number (which the earlier {@code Math.max(..., 0)} would then zero out,
     * inadvertently configuring an infinite wait).</p>
     *
     * @param timeoutMillis the page load timeout in milliseconds; must be {@code >= 0}
     */
    private void applyPageLoadTimeoutToClient(final long timeoutMillis) {
        if (webClient_ != null) {
            // Clamp to [0, Integer.MAX_VALUE] before the narrowing cast to avoid
            // silent wrap-around for values that exceed int range.
            final int clamped = (int) Math.min(timeoutMillis, Integer.MAX_VALUE);
            webClient_.getOptions().setTimeout(clamped);
        }
    }
}