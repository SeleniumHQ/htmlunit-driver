# ![HtmlUnitDriver Logo](https://github.com/SeleniumHQ/htmlunit-driver/blob/master/htmlunit_webdriver.png)

Version 4.51.0 / October 11, 2026

:heart: [Sponsor](https://github.com/sponsors/rbri)

[![Maven Central Version](https://img.shields.io/maven-central/v/org.seleniumhq.selenium/htmlunit3-driver)](https://central.sonatype.com/artifact/org.seleniumhq.selenium/htmlunit3-driver)

### Homepage

[htmlunit.org](https://www.htmlunit.org)

### News

**[Developer Blog](https://htmlunit.github.io/htmlunit-blog/)**

[HtmlUnit@mastodon](https://fosstodon.org/@HtmlUnit) | [HtmlUnit@bsky](https://bsky.app/profile/htmlunit.bsky.social) | [HtmlUnit@Twitter](https://twitter.com/HtmlUnit)


## Table of Contents
- [Overview](#overview)
- [Get it!](#get-it)
  - [Maven](#maven)
  - [Gradle](#gradle)
- [Getting Started](#getting-started)
  - [Simple](#simple)
  - [Customization](#customization)
  - [A complete example](#a-complete-example)
- [Vulnerabilities](#vulnerabilities)
- [Sponsoring](#sponsoring)
- [Contributing](#contributing)
- [Last CI build](#last-ci-build)
  - [Maven](#maven-1)
  - [Gradle](#gradle-1)
- [License](#license)


## Overview

**HtmlUnitDriver** is a [Selenium](https://www.selenium.dev/)
[WebDriver](https://www.selenium.dev/documentation/webdriver/) compatible driver for the
[HtmlUnit](https://www.htmlunit.org) headless browser.

HtmlUnit is a "GUI-less browser for Java programs". It models HTML documents, has fairly good
JavaScript support (which is constantly improving) and is able to work even with quite complex
AJAX libraries, simulating Chrome, Firefox or Edge depending on the configuration used.

Using HtmlUnit through the WebDriver API gives you
* **fast test execution** - there is no browser process to start, no window to render
* **no external dependencies** - a pure Java solution, no browser and no driver binary needs to be installed
* **easy CI integration** - runs everywhere a JVM runs, including containers without a display
* **the standard WebDriver API** - switch between HtmlUnitDriver and a real browser driver without changing your tests,
  including cookies, frames, windows, alerts and the Actions API
* **browser simulation** - Chrome, Edge, Firefox and Firefox ESR (`BrowserVersion`), with JavaScript support switched on or off
* **customization** of the underlying HtmlUnit `WebClient` via **HtmlUnitDriverOptions**
  (e.g. script error handling, CSS, timeouts, SSL, proxy) - see [options](docs/options.md)
* **Selenium 4 Grid support** via the **[HtmlUnit Remote](https://github.com/sbabcoc/htmlunit-remote)** project


#### Selenium and JDK compatibility

The driver uses the same version number as the Selenium release it is built for -
e.g. HtmlUnitDriver 4.51.0 is for Selenium 4.51.0.

> [!IMPORTANT]
> Selenium itself still runs on **JDK 11**, but starting with version 4.44.0
> **HtmlUnitDriver requires JDK 17 or higher**, because it is based on HtmlUnit 5.x.
> If you are bound to JDK 11, use HtmlUnitDriver 4.43.0 or older.

:point_right: **[Compatibility tables](docs/compatibility.md)** - which HtmlUnitDriver version matches
which Selenium release, which HtmlUnit version is used and which JDK is required.


## Get it!

* [Maven Central](https://central.sonatype.com/artifact/org.seleniumhq.selenium/htmlunit3-driver)
* [Release History](https://github.com/SeleniumHQ/htmlunit-driver/releases)
* [Compatibility tables](docs/compatibility.md) - Selenium / HtmlUnit / JDK versions

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit3-driver</artifactId>
    <version>4.51.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation group: 'org.seleniumhq.selenium', name: 'htmlunit3-driver', version: '4.51.0'
```


## Getting Started

### Simple

You can simply use one of the constructors from the **HtmlUnitDriver** class

```java
// simple case - no javascript support
WebDriver webDriver = new HtmlUnitDriver();
```

```java
// specify the browser - no javascript support
WebDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX);
```

```java
// simple case - javascript support enabled
WebDriver webDriver = new HtmlUnitDriver(true);
```

```java
// specify the browser - javascript support enabled
WebDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX, true);
```

### Customization

**HtmlUnit** offers many customization options. Similar to the other WebDriver implementations,
the **HtmlUnitDriverOptions** class can be used to customize your **HtmlUnit** driver.

```java
final HtmlUnitDriverOptions driverOptions = new HtmlUnitDriverOptions(BrowserVersion.FIREFOX);

// configure e.g.
driverOptions.setCapability(HtmlUnitOption.optThrowExceptionOnScriptError, false);

HtmlUnitDriver webDriver = new HtmlUnitDriver(driverOptions);
// use the driver
```

**NOTE**: Complete details for the **HtmlUnitDriverOptions** class can be found [here](docs/options.md).

### A complete example

```java
import org.htmlunit.BrowserVersion;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class HtmlUnitDriverExample {

    public static void main(String[] args) {
        final WebDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME, true);
        try {
            driver.get("https://www.htmlunit.org/");
            System.out.println("Title: " + driver.getTitle());

            for (final WebElement link : driver.findElements(By.tagName("a"))) {
                System.out.println(link.getText() + " -> " + link.getAttribute("href"));
            }
        }
        finally {
            driver.quit();
        }
    }
}
```

More about using HtmlUnit itself:
* [HtmlUnit - Getting Started](https://www.htmlunit.org/gettingStarted.html)
* [Selenium WebDriver documentation](https://www.selenium.dev/documentation/webdriver/)


## Vulnerabilities

HtmlUnitDriver is a thin layer on top of HtmlUnit; please check the HtmlUnit information:

[List of Vulnerabilities](https://github.com/HtmlUnit/htmlunit/blob/master/CVE.md)

[Security Policy](https://github.com/HtmlUnit/htmlunit/blob/master/SECURITY.md)


## Sponsoring

Constantly updating and maintaining the HtmlUnit and HtmlUnitDriver code base already takes a lot of time.

I would like to make 2 major extensions in the next few months
* [Add HTTP/2 support](https://github.com/HtmlUnit/htmlunit/issues/370)
* [Replace the Rhino based JavaScript engine](https://github.com/HtmlUnit/htmlunit/issues/755)

For doing this I need your [sponsoring](https://github.com/sponsors/rbri).


## Contributing

Pull Requests and all other Community Contributions are essential for open source software.
Every contribution - from bug reports to feature requests, typos to full new features - are greatly appreciated.

Please try to keep your pull requests small (don't bundle unrelated changes) and try to include test cases.

If your problem is caused by the browser simulation itself (JavaScript, CSS, DOM, ...),
please open the issue in the [HtmlUnit](https://github.com/HtmlUnit/htmlunit/issues) project.


## Last CI build

The latest builds are available from our
[Jenkins CI build server](https://jenkins.wetator.org/view/HtmlUnit%20Driver/)

[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnitDriver+-+Selenium+4)](https://jenkins.wetator.org/view/HtmlUnit%20Driver/job/HtmlUnitDriver%20-%20Selenium%204/)

Read on if you want to try the latest bleeding-edge snapshot.

### Maven

Add the snapshot repository and dependency to your `pom.xml`:

```xml
    <!-- ... -->
    <repository>
        <name>Central Portal Snapshots</name>
        <id>central-portal-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>

    <!-- ... -->
    <dependencies>
      <dependency>
          <groupId>org.seleniumhq.selenium</groupId>
          <artifactId>htmlunit3-driver</artifactId>
          <version>4.52.0-SNAPSHOT</version>
      </dependency>
      <!-- ... -->
    </dependencies>

    <!-- ... -->
```

### Gradle

Add the snapshot repository and dependency to your `build.gradle`:

```groovy
repositories {
  maven { url "https://central.sonatype.com/repository/maven-snapshots/" }
  // ...
}
// ...
dependencies {
    implementation group: 'org.seleniumhq.selenium', name: 'htmlunit3-driver', version: '4.52.0-SNAPSHOT'
  // ...
}
```


## License

This project is licensed under the Apache 2.0 License


## Development Tools

Special thanks to:

<a href="https://www.jetbrains.com/community/opensource/"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg" alt="JetBrains" width="42"></a>
<a href="https://www.jetbrains.com/idea/"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA_icon.svg" alt="IntelliJ IDEA" width="42"></a>  
**[JetBrains](https://www.jetbrains.com/)** for providing IntelliJ IDEA under their [open source development license](https://www.jetbrains.com/community/opensource/) and

<a href="https://www.eclipse.org/"><img src="https://www.eclipse.org/eclipse.org-common/themes/solstice/public/images/logo/eclipse-foundation-grey-orange.svg" alt="Eclipse Foundation" width="80"></a>  
Eclipse Foundation for their Eclipse IDE

<a href="https://www.syntevo.com/smartgit/"><img src="https://www.syntevo.com/assets/images/logos/smartgit-8c1aa1e2.svg" alt="SmartGit" width="54"></a>  
to **[Syntevo](https://www.syntevo.com/)** for their excellent [SmartGit](https://www.smartgit.dev/)!
