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

package org.openqa.selenium.testing.drivers;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openqa.selenium.Platform.LINUX;
import static org.openqa.selenium.Platform.WINDOWS;
import static org.openqa.selenium.testing.Driver.ALL;
import static org.openqa.selenium.testing.Driver.CHROME;
import static org.openqa.selenium.testing.Driver.EDGE;
import static org.openqa.selenium.testing.Driver.FIREFOX;
import static org.openqa.selenium.testing.Driver.HTMLUNIT;
import static org.openqa.selenium.testing.Driver.IE;
import static org.openqa.selenium.testing.Driver.MARIONETTE;
import static org.openqa.selenium.testing.Driver.REMOTE;
import static org.openqa.selenium.testing.Driver.SAFARI;

import java.util.Arrays;
import java.util.Set;

import org.junit.runner.Description;
import org.openqa.selenium.Platform;
import org.openqa.selenium.testing.Ignore;
import org.openqa.selenium.testing.IgnoreList;
import org.openqa.selenium.testing.NativeEventsRequired;
import org.openqa.selenium.testing.NeedsLocalEnvironment;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Class that decides whether a test class or method should be ignored.
 */
public class TestIgnorance {

  private final Set<BrowserType> alwaysNativeEvents = ImmutableSet.of(BrowserType.CHROME, BrowserType.IE, BrowserType.OPERA);
  private final Set<BrowserType> neverNativeEvents = ImmutableSet.of(BrowserType.HTML_UNIT);
  private final IgnoreComparator ignoreComparator = new IgnoreComparator();
  private final Set<String> methods = Sets.newHashSet();
  private final Set<String> only = Sets.newHashSet();
  private final Set<String> ignoreMethods = Sets.newHashSet();
  private final Set<String> ignoreClasses = Sets.newHashSet();
  private BrowserType browser;

  public TestIgnorance(BrowserType browser) {
    setBrowser(browser);

    String onlyRun = System.getProperty("only_run");
    if (onlyRun != null) {
      only.addAll(Arrays.asList(onlyRun.split(",")));
    }

    String method = System.getProperty("method");
    if (method != null) {
      methods.addAll(Arrays.asList(method.split(",")));
    }

    String ignoreClass = System.getProperty("ignore_class");
    if (ignoreClass != null) {
      ignoreClasses.addAll(Arrays.asList(ignoreClass.split(",")));
    }

    String skip = System.getProperty("ignore_method");
    if (skip != null) {
      ignoreMethods.addAll(Arrays.asList(skip.split(",")));
    }
  }

  public boolean isIgnored(Description method) {
    boolean ignored = ignoreComparator.shouldIgnore(method.getTestClass().getAnnotation(IgnoreList.class)) ||
                      ignoreComparator.shouldIgnore(method.getTestClass().getAnnotation(Ignore.class)) ||
                      ignoreComparator.shouldIgnore(method.getAnnotation(IgnoreList.class)) ||
                      ignoreComparator.shouldIgnore(method.getAnnotation(Ignore.class));

    ignored |= isIgnoredBecauseOfJUnit4Ignore(method.getTestClass().getAnnotation(org.junit.Ignore.class));
    ignored |= isIgnoredBecauseOfJUnit4Ignore(method.getAnnotation(org.junit.Ignore.class));
    if (Boolean.getBoolean("ignored_only")) {
      ignored = !ignored;
    }

    ignored |= isIgnoredBecauseOfNativeEvents(method.getTestClass().getAnnotation(NativeEventsRequired.class));
    ignored |= isIgnoredBecauseOfNativeEvents(method.getAnnotation(NativeEventsRequired.class));

    ignored |= isIgnoredDueToEnvironmentVariables(method);

    ignored |= isIgnoredDueToBeingOnSauce(method);

    return ignored;
  }

  private boolean isIgnoredBecauseOfJUnit4Ignore(org.junit.Ignore annotation) {
    return annotation != null;
  }

  private boolean isIgnoredBecauseOfNativeEvents(NativeEventsRequired annotation) {
    if (annotation == null) {
      return false;
    }

    if (neverNativeEvents.contains(browser)) {
      return true;
    }

    if (alwaysNativeEvents.contains(browser)) {
      return false;
    }

    if (!Boolean.getBoolean("selenium.browser.native_events")) {
      return true;
    }

    // We only have native events on Linux and Windows.
    Platform platform = getEffectivePlatform();
    return !(platform.is(LINUX) || platform.is(WINDOWS));
  }

  private static Platform getEffectivePlatform() {
    return Platform.getCurrent();
  }

  private boolean isIgnoredDueToBeingOnSauce(Description method) {
    boolean isLocal = method.getAnnotation(NeedsLocalEnvironment.class) != null
                      || method.getTestClass().getAnnotation(NeedsLocalEnvironment.class) != null;
    return Boolean.getBoolean("local_only") && !isLocal;
  }

  private boolean isIgnoredDueToEnvironmentVariables(Description method) {
    return (!only.isEmpty() && !only.contains(method.getTestClass().getSimpleName())) ||
           (!methods.isEmpty() && !methods.contains(method.getMethodName())) ||
           ignoreClasses.contains(method.getTestClass().getSimpleName()) ||
           ignoreMethods.contains(method.getMethodName());
  }

  public void setBrowser(BrowserType browserType) {
    this.browser = checkNotNull(
            browserType,
        "Browser to use must be set. Do this by setting the 'selenium.browser' system property");
    addIgnoresForBrowser(browserType, ignoreComparator);
  }

  private void addIgnoresForBrowser(BrowserType browserType, IgnoreComparator comparator) {
    if (Boolean.getBoolean("selenium.browser.remote")) {
      comparator.addDriver(REMOTE);
    }

    switch (browserType) {
      case CHROME:
        comparator.addDriver(CHROME);
        break;

      case EDGE:
        comparator.addDriver(EDGE);
        break;

      case FIREFOX:
        if (Boolean.getBoolean("webdriver.firefox.marionette")) {
          comparator.addDriver(MARIONETTE);
        } else {
          comparator.addDriver(FIREFOX);
        }
        break;

      case HTML_UNIT:
        comparator.addDriver(HTMLUNIT);
        break;

      case IE:
        comparator.addDriver(IE);
        break;

      case NONE:
        comparator.addDriver(ALL);
        break;

      case OPERA:
        break;

      case OPERA_BLINK:
        comparator.addDriver(CHROME);
        break;

      case SAFARI:
        comparator.addDriver(SAFARI);
        break;

      default:
        throw new RuntimeException("Cannot determine which ignore to add ignores rules for");
    }
  }

}
