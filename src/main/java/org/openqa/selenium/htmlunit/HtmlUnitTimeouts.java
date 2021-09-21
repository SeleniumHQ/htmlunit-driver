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

import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Class for timeouts managing
 */
public class HtmlUnitTimeouts implements WebDriver.Timeouts {
    private long implicitWait = 0;
    private long scriptTimeout = 0;
    private long pageLoadTimeout = 0;
    private WebClient webClient = null;

    public HtmlUnitTimeouts() {
        //nop
    }

    public HtmlUnitTimeouts(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Duration getImplicitWaitTimeout() {
        return Duration.ofMillis(implicitWait);
    }

    @Deprecated
    @Override
    public WebDriver.Timeouts implicitlyWait(long time, TimeUnit unit) {
        implicitlyWait(Duration.ofMillis(TimeUnit.MILLISECONDS.convert(time, unit)));
        return this;
    }

    @Override
    public WebDriver.Timeouts implicitlyWait(Duration duration) {
        this.implicitWait = Math.max(0, duration.toMillis());
        return this;
    }

    @Override
    public WebDriver.Timeouts scriptTimeout(Duration duration) {
        this.scriptTimeout = duration.toMillis();
        return this;
    }

    @Deprecated
    @Override
    public WebDriver.Timeouts setScriptTimeout(long time, TimeUnit unit) {
        scriptTimeout(Duration.ofMillis(TimeUnit.MILLISECONDS.convert(time, unit)));
        return this;
    }

    @Override
    public Duration getScriptTimeout() {
        return Duration.ofMillis(scriptTimeout);
    }

    @Deprecated
    @Override
    public WebDriver.Timeouts pageLoadTimeout(long time, TimeUnit unit) {
        pageLoadTimeout(Duration.ofMillis(TimeUnit.MILLISECONDS.convert(time, unit)));
        return this;
    }

    @Override
    public WebDriver.Timeouts pageLoadTimeout(Duration duration) {
        this.pageLoadTimeout = duration.toMillis();
        setPageLoadTimeoutForWebClient(webClient, pageLoadTimeout);
        return this;
    }

    @Override
    public Duration getPageLoadTimeout() {
        return Duration.ofMillis(pageLoadTimeout);
    }

    private static void setPageLoadTimeoutForWebClient(WebClient webClient, long timeout) {
        if (webClient != null) {
            webClient.getOptions().setTimeout(Math.max((int) timeout, 0));
        }
    }
}
