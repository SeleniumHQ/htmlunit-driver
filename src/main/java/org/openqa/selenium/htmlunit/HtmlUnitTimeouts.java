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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.htmlunit.WebClient;
import org.openqa.selenium.WebDriver;

/**
 * Class for timeouts managing.
 *
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public class HtmlUnitTimeouts implements WebDriver.Timeouts {
    private long implicitWait_ = 0;
    private long scriptTimeout_ = 0;
    private long pageLoadTimeout_ = 0;
    private WebClient webClient_ = null;

    public HtmlUnitTimeouts() {
        // nop
    }

    public HtmlUnitTimeouts(final WebClient webClient) {
        this.webClient_ = webClient;
    }

    @Override
    public Duration getImplicitWaitTimeout() {
        return Duration.ofMillis(implicitWait_);
    }

    @Deprecated
    @Override
    public WebDriver.Timeouts implicitlyWait(final long time, final TimeUnit unit) {
        implicitlyWait(Duration.ofMillis(TimeUnit.MILLISECONDS.convert(time, unit)));
        return this;
    }

    @Override
    public WebDriver.Timeouts implicitlyWait(final Duration duration) {
        this.implicitWait_ = Math.max(0, duration.toMillis());
        return this;
    }

    @Override
    public WebDriver.Timeouts scriptTimeout(final Duration duration) {
        this.scriptTimeout_ = duration.toMillis();
        return this;
    }

    @Deprecated
    @Override
    public WebDriver.Timeouts setScriptTimeout(final long time, final TimeUnit unit) {
        scriptTimeout(Duration.ofMillis(TimeUnit.MILLISECONDS.convert(time, unit)));
        return this;
    }

    @Override
    public Duration getScriptTimeout() {
        return Duration.ofMillis(scriptTimeout_);
    }

    @Deprecated
    @Override
    public WebDriver.Timeouts pageLoadTimeout(final long time, final TimeUnit unit) {
        pageLoadTimeout(Duration.ofMillis(TimeUnit.MILLISECONDS.convert(time, unit)));
        return this;
    }

    @Override
    public WebDriver.Timeouts pageLoadTimeout(final Duration duration) {
        this.pageLoadTimeout_ = duration.toMillis();
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
