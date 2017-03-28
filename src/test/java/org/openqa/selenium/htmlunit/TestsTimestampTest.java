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

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Checks the timestamp of the Selenium test files, to ensure it is always updated. 
 */
@Ignore
@RunWith(Parameterized.class)
public class TestsTimestampTest {

  @Parameters(name="{1}")
  public static Iterable<Object[]> data() throws IOException {
    List<Path> list = new ArrayList<>();
    Path path = Paths.get(".");
    fill(list, path);
    return list.stream().map(i -> {
      String fileName = i.getFileName().toString();
      fileName = fileName.substring(0, fileName.length() - 5);
      return new Object[] {i, fileName}; 
    }).collect(Collectors.toList());
  }

  private static void fill(List<Path> list, Path path) throws IOException {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
      for (Path child : stream) {
        String fileName = child.getFileName().toString();
        switch (fileName) {
          case "target":
          case "main":
          case "testing":
          case "environment":
            break;

          default:
            if (!fileName.startsWith(".")) {
              if (Files.isDirectory(child)) {
                fill(list, child);
              }
              if (fileName.endsWith(".java")) {
                list.add(child);
              }
            }
        }
      }
    }
  }

  @Parameter(0)
  public Path path;

  @Parameter(1)
  public String fileName;

  private static final String SELENIUM_BASE
    = "https://github.com/SeleniumHQ/selenium/tree/master/java/client/test/";
  private static final String DRIVER_BASE
    = "https://github.com/SeleniumHQ/htmlunit-driver/tree/master/src/test/java/";

  private static WebDriver driver;

  @BeforeClass
  public static void init() throws MalformedURLException {
    driver = new HtmlUnitDriver();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  @AfterClass
  public static void close() {
    driver.quit();
  }

  @Test
  public void test() throws MalformedURLException, InterruptedException {
    Instant driverTime = getTimeOf(path, DRIVER_BASE);
    Instant seleniumTime = getTimeOf(path, SELENIUM_BASE);
    assumeNotNull(driverTime);
    assumeNotNull(seleniumTime);
    if (seleniumTime.isAfter(driverTime)) {
      fail(path.getFileName() + " is outdated");
    }
  }

  private static Instant getTimeOf(Path path, String base) {
    String url = base + path.subpath(4, path.getNameCount()).toString().replace('\\', '/');
    driver.get(url);
    try {
      WebElement e = driver.findElement(By.xpath("//span[@class='float-right']/relative-time"));
      String datetime = e.getAttribute("datetime");
      return Instant.parse(datetime);
    }
    catch (Exception e) {
      return null;
    }
  }
}
