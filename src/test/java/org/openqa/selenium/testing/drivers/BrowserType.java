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

import java.util.Arrays;
import java.util.logging.Logger;

public enum BrowserType {

  CHROME("chrome"),
  EDGE("edge"),
  FIREFOX("ff"),
  HTML_UNIT("htmlunit"),
  IE("ie"),
  NONE("none"), // For those cases where you don't actually want a browser
  OPERA("opera"),
  OPERA_BLINK("operablink"),
  SAFARI("safari");

  private static final Logger log = Logger.getLogger(BrowserType.class.getName());

  private final String name;

  BrowserType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static BrowserType detect() {
    String browserName = System.getProperty("selenium.browser");
    if (browserName == null) {
      return HTML_UNIT;
    }

    try {
      return BrowserType.getByName(browserName);
    } catch (IllegalArgumentException e) {
      log.severe(e.getMessage());
      return null;
    }
  }

  public static BrowserType getByName(String name) {
    return Arrays.stream(BrowserType.values())
            .filter(f -> f.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cannot locate matching browser for: " + name));
  }

}
