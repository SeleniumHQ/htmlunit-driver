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

package org.openqa.selenium.htmlunit.junit;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.htmlunit.BrowserVersion;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.AlertsStandards;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.HtmlUnitNYI;

/**
 * The Browser Statement.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
class BrowserStatement extends Statement {

    private final Statement next_;
    private final boolean notYetImplemented_;
    private final FrameworkMethod method_;
    private final boolean realBrowser_;
    private final BrowserVersion browserVersion_;
    private final int tries_;

    BrowserStatement(final Statement next, final FrameworkMethod method, final boolean realBrowser,
            final boolean notYetImplemented, final int tries, final BrowserVersion browserVersion) {
        next_ = next;
        method_ = method;
        realBrowser_ = realBrowser;
        notYetImplemented_ = notYetImplemented;
        tries_ = tries;
        browserVersion_ = browserVersion;
    }

    @Override
    public void evaluate() throws Throwable {
        for (int i = 0; i < tries_; i++) {
            try {
                evaluateSolo();
                break;
            }
            catch (final Throwable t) {
                if (notYetImplemented_) {
                    throw t;
                }
                if (BrowserVersionClassRunner.maven_) {
                    System.out.println("Failed test "
                            + method_.getDeclaringClass().getName() + '.' + method_.getName()
                            + (tries_ != 1 ? " #" + (i + 1) : ""));
                }
                if (i == tries_ - 1) {
                    throw t;
                }
            }
        }
        assertAlerts();
    }

    private void assertAlerts() {
        final Alerts alerts = method_.getAnnotation(Alerts.class);
        if (alerts != null) {
            if (!BrowserVersionClassRunner.isDefined(alerts.value())) {
                assertFalse("Obsolete DEFAULT because all browser expectations are defined individually",
                        BrowserVersionClassRunner.isDefined(alerts.DEFAULT())
                        && BrowserVersionClassRunner.isDefined(alerts.CHROME())
                        && BrowserVersionClassRunner.isDefined(alerts.FF())
                        && BrowserVersionClassRunner.isDefined(alerts.FF_ESR())
                        && BrowserVersionClassRunner.isDefined(alerts.EDGE()));

                assertFalse("Obsolete DEFAULT because all browser expectations are defined individually",
                        BrowserVersionClassRunner.isDefined(alerts.DEFAULT())
                        && BrowserVersionClassRunner.isDefined(alerts.CHROME())
                        && BrowserVersionClassRunner.isDefined(alerts.FF())
                        && BrowserVersionClassRunner.isDefined(alerts.FF_ESR())
                        && BrowserVersionClassRunner.isDefined(alerts.EDGE()));

                assertNotEquals(BrowserVersion.CHROME, alerts.CHROME(), alerts.DEFAULT());
                assertNotEquals(BrowserVersion.FIREFOX, alerts.FF(), alerts.DEFAULT());
                assertNotEquals(BrowserVersion.FIREFOX_ESR, alerts.FF_ESR(), alerts.DEFAULT());
            }

            final HtmlUnitNYI nyiAlerts = method_.getAnnotation(HtmlUnitNYI.class);
            if (nyiAlerts != null) {
                if (BrowserVersionClassRunner.isDefined(alerts.CHROME())
                        && BrowserVersionClassRunner.isDefined(nyiAlerts.CHROME())) {
                    assertNotEquals(BrowserVersion.CHROME, alerts.CHROME(), nyiAlerts.CHROME());
                }
                else if (BrowserVersionClassRunner.isDefined(alerts.DEFAULT())
                        && BrowserVersionClassRunner.isDefined(nyiAlerts.CHROME())) {
                    assertNotEquals(BrowserVersion.CHROME, alerts.DEFAULT(), nyiAlerts.CHROME());
                }

                if (BrowserVersionClassRunner.isDefined(alerts.FF_ESR())
                        && BrowserVersionClassRunner.isDefined(nyiAlerts.FF_ESR())) {
                    assertNotEquals(BrowserVersion.FIREFOX_ESR, alerts.FF_ESR(), nyiAlerts.FF_ESR());
                }
                else if (BrowserVersionClassRunner.isDefined(alerts.DEFAULT())
                        && BrowserVersionClassRunner.isDefined(nyiAlerts.FF_ESR())) {
                    assertNotEquals(BrowserVersion.FIREFOX_ESR, alerts.DEFAULT(), nyiAlerts.FF_ESR());
                }

                if (BrowserVersionClassRunner.isDefined(alerts.FF())
                        && BrowserVersionClassRunner.isDefined(nyiAlerts.FF())) {
                    assertNotEquals(BrowserVersion.FIREFOX, alerts.FF(), nyiAlerts.FF());
                }
                else if (BrowserVersionClassRunner.isDefined(alerts.DEFAULT())
                        && BrowserVersionClassRunner.isDefined(nyiAlerts.FF())) {
                    assertNotEquals(BrowserVersion.FIREFOX, alerts.DEFAULT(), nyiAlerts.FF());
                }

                if (BrowserVersionClassRunner.isDefined(alerts.EDGE())
                        && BrowserVersionClassRunner.isDefined(nyiAlerts.EDGE())) {
                    assertNotEquals(BrowserVersion.EDGE, alerts.EDGE(), nyiAlerts.EDGE());
                }
                else if (BrowserVersionClassRunner.isDefined(alerts.DEFAULT())
                        && BrowserVersionClassRunner.isDefined(nyiAlerts.EDGE())) {
                    assertNotEquals(BrowserVersion.EDGE, alerts.DEFAULT(), nyiAlerts.EDGE());
                }
            }
        }

        final AlertsStandards alerts2 = method_.getAnnotation(AlertsStandards.class);
        if (alerts2 != null) {
            if (!BrowserVersionClassRunner.isDefined(alerts2.value())) {
                assertFalse("Obsolete DEFAULT because all browser expectations are defined individually",
                        BrowserVersionClassRunner.isDefined(alerts2.DEFAULT())
                        && BrowserVersionClassRunner.isDefined(alerts2.CHROME())
                        && BrowserVersionClassRunner.isDefined(alerts2.FF())
                        && BrowserVersionClassRunner.isDefined(alerts2.FF_ESR())
                        && BrowserVersionClassRunner.isDefined(alerts2.EDGE()));

                assertNotEquals(BrowserVersion.EDGE, alerts2.EDGE(), alerts2.DEFAULT());
                assertNotEquals(BrowserVersion.CHROME, alerts2.CHROME(), alerts2.DEFAULT());
                assertNotEquals(BrowserVersion.FIREFOX, alerts2.FF(), alerts2.DEFAULT());
                assertNotEquals(BrowserVersion.FIREFOX_ESR, alerts2.FF_ESR(), alerts2.DEFAULT());
            }
        }
    }

    private void assertNotEquals(final BrowserVersion browser, final String[] value1, final String[] value2) {
        if (value1.length != 0 && !BrowserRunner.EMPTY_DEFAULT.equals(value1[0])
                && value1.length == value2.length
                && Arrays.asList(value1).toString().equals(Arrays.asList(value2).toString())) {
            throw new AssertionError("Redundant alert for " + browser.getNickname() + " in "
                    + method_.getDeclaringClass().getSimpleName() + '.' + method_.getName() + "()");
        }
    }

    public void evaluateSolo() throws Throwable {
        Exception toBeThrown = null;
        try {
            next_.evaluate();
            if (notYetImplemented_) {
                final String errorMessage;
                if (browserVersion_.getNickname() == null) {
                    errorMessage = method_.getName() + " is marked as not implemented but already works";
                }
                else {
                    errorMessage = method_.getName() + " is marked as not implemented with "
                        + browserVersion_.getNickname() + " but already works";
                }
                toBeThrown = new Exception(errorMessage);
            }
        }
        catch (final Throwable e) {
            if (!notYetImplemented_) {
                throw e;
            }
        }
        if (toBeThrown != null) {
            throw toBeThrown;
        }
    }
}
